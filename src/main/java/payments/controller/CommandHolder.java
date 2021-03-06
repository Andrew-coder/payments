package payments.controller;

import payments.controller.commands.*;
import payments.controller.commands.admin.AdminUnblockCardCommand;
import payments.controller.commands.admin.CardsAdministrationCommand;
import payments.controller.commands.admin.PaymentsAdministrationCommand;
import payments.controller.commands.admin.ViewPaymentAdminCommand;
import payments.controller.commands.login.LoginCommand;
import payments.controller.commands.login.LoginSubmitCommand;
import payments.controller.commands.user.*;
import payments.controller.commands.user.card.BlockCardCommand;
import payments.controller.commands.user.card.CardsCommand;
import payments.controller.commands.user.card.RefillCardCommand;
import payments.controller.commands.user.card.RefillCardSubmitCommand;
import payments.controller.commands.user.payments.AccountPaymentsCommand;
import payments.controller.commands.user.payments.CardPaymentsCommand;
import payments.controller.commands.user.payments.PaymentsCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * class which contains all commands, which are necessary to response user
 */
public class CommandHolder {
    public static final String NUMBER_BETWEEN_SLASHES_PATTERN = "/\\d+(?=/|$)";
    private Map<String, Command> commands = new HashMap<>();

    public CommandHolder() {
        fillCommands();
    }

    /**
     * method which convert url and find necessary command
     * @param key
     * @return command
     */
    Command findCommand(String key){
        String convertedKey = removeAllNumbersFromUrl(key);
        return commands.getOrDefault(convertedKey, new PageNotFoundCommand());
    }

    private void fillCommands(){
        commands.put("GET:/home", new HomeCommand());
        commands.put("GET:/login", new LoginCommand());
        commands.put("POST:/login", new LoginSubmitCommand());
        commands.put("GET:/logout", new LogoutCommand());
        commands.put("POST:/register", new RegisterSubmitCommand());
        commands.put("GET:/payments", new PaymentsCommand());
        commands.put("POST:/payments/card", new CardPaymentsCommand());
        commands.put("POST:/payments/account", new AccountPaymentsCommand());
        commands.put("GET:/cards", new CardsCommand());
        commands.put("GET:/cards/refill/id", new RefillCardCommand());
        commands.put("POST:/cards/refill", new RefillCardSubmitCommand());
        commands.put("POST:/cards/block", new BlockCardCommand());
        commands.put("GET:/admin", new AdminHomeCommand());
        commands.put("GET:/admin/cards", new CardsAdministrationCommand());
        commands.put("GET:/admin/payments", new PaymentsAdministrationCommand());
        commands.put("GET:/admin/payments/id", new ViewPaymentAdminCommand());
        commands.put("POST:/admin/cards/unblock", new AdminUnblockCardCommand());
    }

    /**
     * this method replaces all digits between slashes to "id"
     * this is necessary because search algorithm doesn't support regular expressions
     * @param url
     * @return converted url
     */
    private String removeAllNumbersFromUrl(String url){
        return url.replaceAll(NUMBER_BETWEEN_SLASHES_PATTERN, "/id");
    }
}