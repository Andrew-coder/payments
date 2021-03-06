package payments.controller.commands.user.card;

import org.apache.log4j.Logger;
import payments.controller.commands.CommandExecutor;
import payments.controller.validators.Errors;
import payments.controller.validators.RefillValidator;
import payments.model.dto.payment.RefillData;
import payments.model.entity.payment.Payment;
import payments.service.PaymentService;
import payments.service.HistoryService;
import payments.service.impl.PaymentServiceImpl;
import payments.service.impl.HistoryServiceImpl;
import payments.utils.constants.Attributes;
import payments.utils.constants.MessageKeys;
import payments.utils.constants.PagesPath;
import payments.utils.extractors.RequestParamExtractor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

public class RefillCardSubmitCommand extends CommandExecutor {
    private static final Logger logger = Logger.getLogger(RefillCardSubmitCommand.class);
    private PaymentService paymentService = PaymentServiceImpl.getInstance();
    private HistoryService historyService = HistoryServiceImpl.getInstance();
    private RefillValidator refillValidator;
    private RequestParamExtractor paramExtractor;

    public RefillCardSubmitCommand() {
        super(PagesPath.REFILL_CARD_PAGE);
        refillValidator = new RefillValidator();
        paramExtractor = new RequestParamExtractor();
    }

    @Override
    public String performExecute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Errors errors = new Errors();
        saveRefillDataToRequest(request);
        RefillData refillData = extractRefillDataFromRequest(request);
        errors.addErrors(refillValidator.validate(refillData).getErrors());
        if(errors.hasErrors()){
            processErrors(request, errors);
            request.getRequestDispatcher(PagesPath.REFILL_CARD_PAGE).forward(request, response);
            return PagesPath.FORWARD;
        }
        paymentService.refillCard(refillData);
        Payment payment = extractPaymentFromRefillData(refillData);
        historyService.saveRefillPayment(payment, refillData);
        logger.info(String.format("Card %s was successfully refilled",refillData.getCardNumber()));
        clearRefillDataFromRequest(request);
        request.setAttribute(Attributes.CONFIRM_MESSAGE, MessageKeys.SUCCESSFUL_PAYMENT);
        request.getRequestDispatcher(PagesPath.CONFIRMATION_PAGE).forward(request, response);
        return PagesPath.FORWARD;
    }

    private RefillData extractRefillDataFromRequest(HttpServletRequest request){
        RefillData.Builder builder = new RefillData.Builder()
                .setCardNumber(request.getParameter("card_number"))
                .setPin(request.getParameter("card_pin"))
                .setCvv(request.getParameter("card_cvv"))
                .setExpireDate(request.getParameter("card_expire"))
                .setIdRefilledCard(paramExtractor.extractLong(request, "id"))
                .setSum(paramExtractor.extractDouble(request, "sum"));
        return builder.build();
    }

    private Payment extractPaymentFromRefillData(RefillData data){
        return new Payment.Builder()
                .setSum(BigDecimal.valueOf(data.getSum()))
                .setCurrentDate()
                .build();
    }

    private void saveRefillDataToRequest(HttpServletRequest request){
        request.setAttribute(Attributes.PREVIOUS_CARD_NUMBER, request.getParameter("card_number"));
        request.setAttribute(Attributes.PREVIOUS_PIN, request.getParameter("card_pin"));
        request.setAttribute(Attributes.PREVIOUS_CVV, request.getParameter("card_cvv"));
        request.setAttribute(Attributes.PREVIOUS_EXPIRE_DATE, request.getParameter("card_expire"));
        request.setAttribute(Attributes.PREVIOUS_SUM, request.getParameter("sum"));
        request.setAttribute(Attributes.CARD, request.getParameter("id"));
    }

    private void clearRefillDataFromRequest(HttpServletRequest request){
        request.removeAttribute(Attributes.PREVIOUS_CARD_NUMBER);
        request.removeAttribute(Attributes.PREVIOUS_PIN);
        request.removeAttribute(Attributes.PREVIOUS_CVV);
        request.removeAttribute(Attributes.PREVIOUS_EXPIRE_DATE);
        request.removeAttribute(Attributes.PREVIOUS_SUM);
        request.removeAttribute(Attributes.CARD);
    }

    private void processErrors(HttpServletRequest request, Errors errors){
        logger.error("Wrong input data in refill info");
        request.setAttribute(Attributes.ERRORS, errors);
    }
}