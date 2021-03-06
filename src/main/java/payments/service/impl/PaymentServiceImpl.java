package payments.service.impl;

import org.apache.log4j.Logger;
import payments.dao.*;
import payments.model.dto.payment.AccountTransferData;
import payments.model.dto.payment.CardTransferData;
import payments.model.dto.payment.RefillData;
import payments.model.entity.BankAccount;
import payments.model.entity.Card;
import payments.model.entity.payment.PaymentTariff;
import payments.model.entity.payment.PaymentType;
import payments.service.PaymentService;
import payments.service.exception.ServiceException;
import payments.utils.constants.MessageKeys;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of payment service
 */
public class PaymentServiceImpl implements PaymentService {
    private static final Logger logger = Logger.getLogger(PaymentServiceImpl.class);
    private static final double ZERO = 0.0;
    private DaoFactory daoFactory = DaoFactory.getInstance();

    public PaymentServiceImpl() {
    }

    private PaymentServiceImpl(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    private static class InstanceHolder {
        private static final PaymentService instance = new PaymentServiceImpl(DaoFactory.getInstance());
    }

    public static PaymentService getInstance(){
        return InstanceHolder.instance;
    }

    @Override
    public Optional<Card> findById(long id) {
        try(ConnectionWrapper wrapper = daoFactory.getConnection()){
            CardDao cardDao = daoFactory.getCardDao(wrapper);
            return cardDao.findById(id);
        }
    }

    @Override
    public List<Card> findCardsByUser(long id) {
        try (ConnectionWrapper wrapper = daoFactory.getConnection()){
            CardDao cardDao = daoFactory.getCardDao(wrapper);
            return cardDao.findCardsByUser(id);
        }
    }

    @Override
    public List<Card> findAll() {
        try(ConnectionWrapper wrapper = daoFactory.getConnection()){
            CardDao cardDao = daoFactory.getCardDao(wrapper);
            return cardDao.findAll();
        }
    }

    @Override
    public void create(Card card) {
        try(ConnectionWrapper wrapper = daoFactory.getConnection()){
            CardDao cardDao = daoFactory.getCardDao(wrapper);
            cardDao.create(card);
        }
    }

    @Override
    public void blockCard(long id) {
        try(ConnectionWrapper wrapper = daoFactory.getConnection()){
            CardDao cardDao = daoFactory.getCardDao(wrapper);
            if(!isCardBlocked(id)){
                cardDao.blockCard(id);
            }
        }
    }

    @Override
    public void unblockCard(long id) {
        try(ConnectionWrapper wrapper = daoFactory.getConnection()){
            CardDao cardDao = daoFactory.getCardDao(wrapper);
            if(isCardBlocked(id)){
                cardDao.unblockCard(id);
            }
        }
    }

    @Override
    public void refillCard(RefillData data) {
        try(ConnectionWrapper wrapper = daoFactory.getConnection()){
            CardDao cardDao = daoFactory.getCardDao(wrapper);
            BankAccountDao accountDao = daoFactory.getBankAccountDao(wrapper);
            PaymentTariffDao tariffDao = daoFactory.getPaymentTariffDao(wrapper);
            Card card = findCardByIdOrThrowException(data.getIdRefilledCard(), cardDao);
            Optional<Card> sourceCard = cardDao.findCardByNumber(data.getCardNumber());
            if(sourceCard.isPresent()){
                Card senderCard = sourceCard.get();
                checkCardInfo(sourceCard, data);
                checkCardValidityDate(senderCard);
                checkCardIsNotBlocked(senderCard.getId(), cardDao);
                BankAccount recipientAccount = card.getAccount();
                BankAccount senderAccount = senderCard.getAccount();
                BigDecimal senderBalance = calculateBalanceByTariff(senderAccount, data.getSum(),
                        tariffDao, PaymentType.REFILL);
                senderAccount.setBalance(senderBalance);
                recipientAccount.setBalance(increaseAccountBalance(recipientAccount, data.getSum()));
                performTransaction(senderAccount, recipientAccount, wrapper, accountDao);
            }
            else {
                BankAccount account = card.getAccount();
                BigDecimal newBalance = increaseAccountBalance(account, data.getSum());
                account.setBalance(newBalance);
                accountDao.update(account);
            }
        }
    }

    @Override
    public void transferBetweenCards(CardTransferData data) {
        try(ConnectionWrapper wrapper = daoFactory.getConnection()){
            CardDao cardDao = daoFactory.getCardDao(wrapper);
            BankAccountDao accountDao = daoFactory.getBankAccountDao(wrapper);
            PaymentTariffDao tariffDao = daoFactory.getPaymentTariffDao(wrapper);
            Card senderCard =
                    findCardByNumberOrThrowException(data.getSenderCard(), cardDao);
            Card recipientCard =
                    findCardByNumberOrThrowException(data.getRecipientCard(), cardDao);
            checkCardValidityDate(senderCard);
            checkCardIsNotBlocked(senderCard.getId(), cardDao);
            BankAccount senderAccount = senderCard.getAccount();
            BankAccount recipientAccount  = recipientCard.getAccount();
            BigDecimal senderBalance = calculateBalanceByTariff(senderAccount, data.getSum(),
                    tariffDao, PaymentType.TRANSFER_WITHIN_THIS_BANK);
            senderAccount.setBalance(senderBalance);
            recipientAccount.setBalance(increaseAccountBalance(recipientAccount, data.getSum()));
            performTransaction(senderAccount, recipientAccount, wrapper, accountDao);
        }
    }

    @Override
    public void accountTransfer(AccountTransferData data) {
        try(ConnectionWrapper wrapper = daoFactory.getConnection()){
            CardDao cardDao = daoFactory.getCardDao(wrapper);
            BankAccountDao accountDao = daoFactory.getBankAccountDao(wrapper);
            PaymentTariffDao tariffDao = daoFactory.getPaymentTariffDao(wrapper);
            Card senderCard = findCardByNumberOrThrowException(data.getSenderCard(), cardDao);
            checkCardValidityDate(senderCard);
            checkCardIsNotBlocked(senderCard.getId(), cardDao);
            BankAccount senderAccount = senderCard.getAccount();
            BankAccount recipientAccount =
                    findAccountByNumberOrThrowException(data.getAccountNumber(), accountDao);
            BigDecimal senderBalance = calculateBalanceByTariff(senderAccount, data.getSum(),
                    tariffDao, PaymentType.TRANSFER_WITHIN_THIS_BANK);
            senderAccount.setBalance(senderBalance);
            recipientAccount.setBalance(increaseAccountBalance(recipientAccount, data.getSum()));
            performTransaction(senderAccount, recipientAccount, wrapper, accountDao);
        }
    }

    @Override
    public boolean isCardBlocked(long id) {
        try(ConnectionWrapper wrapper= daoFactory.getConnection()){
            CardDao cardDao = daoFactory.getCardDao(wrapper);
            return cardDao.findAllBlockedCards()
                    .stream()
                    .mapToLong(Card::getId)
                    .anyMatch(cardId -> cardId==id);
        }
    }

    private void checkCardIsNotBlocked(long id, CardDao cardDao){
        cardDao.findAllBlockedCards()
                .stream()
                .mapToLong(Card::getId)
                .filter(cardId -> cardId==id)
                .findFirst()
                .ifPresent(a->{throw new ServiceException(MessageKeys.CARD_IS_BLOCKED);});
    }

    private void performTransaction(BankAccount sender, BankAccount recipient,
                                    ConnectionWrapper wrapper, BankAccountDao accountDao){
        checkIsNotTheSameAccount(sender, recipient);
        wrapper.beginSerializableTransaction();
        accountDao.update(sender);
        accountDao.update(recipient);
        wrapper.commitTransaction();
    }

    private BigDecimal calculateBalanceByTariff(BankAccount account, double sum,
                                             PaymentTariffDao tariffDao, PaymentType type){
        PaymentTariff tariff = tariffDao.findByPaymentType(type)
                .orElseThrow(()->new ServiceException(MessageKeys.PAYMENT_TARIFF_NOT_FOUND));
        BigDecimal fixedRate = tariff.getFixedRate();
        BigDecimal currentBalance = account.getBalance();
        BigDecimal paymentRateValue = BigDecimal.valueOf(tariff.getPaymentRate())
                .multiply(BigDecimal.valueOf(sum));
        BigDecimal remainder = currentBalance
                .subtract(fixedRate)
                .subtract(paymentRateValue)
                .subtract(BigDecimal.valueOf(sum));
        if(remainder.doubleValue()<ZERO){
            logger.error(MessageKeys.NOT_ENOUGH_MONEY);
            throw new ServiceException(MessageKeys.NOT_ENOUGH_MONEY);
        }
        return remainder;
    }

    private Card findCardByIdOrThrowException(long id, CardDao cardDao){
        Optional<Card> card = cardDao.findById(id);
        if(!card.isPresent()){
            logger.error(MessageKeys.CARD_NOT_EXIST);
            throw new ServiceException(MessageKeys.CARD_NOT_EXIST);
        }
        return card.get();
    }

    private Card findCardByNumberOrThrowException(String number, CardDao cardDao){
        Optional<Card> card = cardDao.findCardByNumber(number);
        if(!card.isPresent()){
            logger.error(MessageKeys.CARD_NOT_EXIST);
            throw new ServiceException(MessageKeys.CARD_NOT_EXIST);
        }
        return card.get();
    }

    private BankAccount findAccountByNumberOrThrowException(String number, BankAccountDao accountDao){
        Optional<BankAccount> account = accountDao.findBankAccountByNumber(number);
        if(!account.isPresent()){
            logger.error(MessageKeys.ACCOUNT_NOT_EXIST);
            throw new ServiceException(MessageKeys.ACCOUNT_NOT_EXIST);
        }
        return account.get();
    }

    private void checkCardInfo(Optional<Card> card, RefillData data){
        card.filter(c -> c.getPin().equals(data.getPin()))
                .filter(c -> c.getCvv().equals(data.getCvv()))
                .filter(c -> compareDates(c.getExpireDate().toString(), data.getExpireDate()))
                .orElseThrow(() -> new ServiceException(MessageKeys.WRONG_CARD_DATA));
    }

    private void checkCardValidityDate(Card card){
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date today = new Date();
        try{
            Date todayWithZeroTime = formatter.parse(formatter.format(today));
            if(todayWithZeroTime.after(card.getExpireDate())){
                logger.error(MessageKeys.CARD_VALIDITY_EXPIRED);
                throw new ServiceException(MessageKeys.CARD_VALIDITY_EXPIRED);
            }
        }
        catch (ParseException ex){}
    }

    private void checkIsNotTheSameAccount(BankAccount sender, BankAccount recipient){
        if(sender.getAccountNumber().equals(recipient.getAccountNumber())){
            logger.error(MessageKeys.TRANSFER_TO_THE_SAME_ACCOUNT);
            throw new ServiceException(MessageKeys.TRANSFER_TO_THE_SAME_ACCOUNT);
        }
    }

    private boolean compareDates(String str1, String str2){
        try{
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date1 = format.parse(str1);
            Date date2 = format.parse(str2);
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(date1);
            cal2.setTime(date2);
            return cal1.equals(cal2);
        }
        catch (ParseException ex){}
        return false;
    }

    private BigDecimal increaseAccountBalance(BankAccount account, double sum){
        return account.getBalance().add(BigDecimal.valueOf(sum));
    }

    public void setDaoFactory(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }
}