import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
;

import java.io.File;


public class MainClass { //Test3
    public static void main(String[] args)
    {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            disableWarning();
            telegramBotsApi.registerBot(new UnitoBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public static void disableWarning() {
        System.err.close();
        System.setErr(System.out);
    }
}
