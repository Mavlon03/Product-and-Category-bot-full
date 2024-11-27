package uz.pdp.utils;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import uz.pdp.Category;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static uz.pdp.utils.BotService.*;

public class BotController {


    public void start() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        BotService.telegramBot.setUpdatesListener(updates -> {

            for (Update update : updates) {
                executorService.execute(() -> {
                    try {
                        if (update.message() != null || update.callbackQuery() != null) {
                            handleUpdates(update);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void handleUpdates(Update update) {
        if (update.message() != null) {
            handleMessage(update.message());
        } else if (update.callbackQuery() != null) {
            handleCallbackQuery(update.callbackQuery());
        }
    }

    private void handleMessage(Message message) {
        Long chatId = message.chat().id();
        TgUser tgUser = getOrCreateUser(chatId);

        if (message.text() != null && message.text().equals("/start")) {
            acceptStartWelcomeMessage(tgUser);
        } else if (message.contact() != null && tgUser.getState().equals(TgState.SHARING_CONTACT)) {
            acceptAndShareLocation(tgUser, message.contact());
        } else if (message.location() != null && tgUser.getState().equals(TgState.SHARING_LOCATION)) {
            acceptLocationAndChooseMenu(tgUser, message.location());
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.from().id();
        TgUser tgUser = getOrCreateUser(chatId);
        String data = callbackQuery.data();
        if (tgUser.getState().equals(TgState.CHOOSING_TYPE)){
            acceptMenuAndBeginWorking(tgUser, callbackQuery);
        }else if (tgUser.getState().equals(TgState.SHOWING_PR)){
            BotService.acceptCatShowPr(tgUser,data);
        }else if (tgUser.getState().equals(TgState.CHOOSING_PRODUCT)){
            BotService.acceptProduct(tgUser,data);
        }else if (tgUser.getState().equals(TgState.CHOOSING_COUNT)){
            BotService.acceptProductCount(tgUser,data);
        }
    }




    public static void addCategories() {
        DB.CATEGORIES.add(new Category(1L, "Ichimliklar"));
        DB.CATEGORIES.add(new Category(2L, "Yeguliklar"));
        DB.CATEGORIES.add(new Category(3L, "Kiyguliklar"));
    }

    public static void addProducts() {


        DB.PRODUCTS.add(new Product(1L, "coca-cola", 5000,"coca-cola.jpg", 1L));
        DB.PRODUCTS.add(new Product(2L, "pepsi", 4500,"pepsi.jpg", 1L));
        DB.PRODUCTS.add(new Product(3L, "fanta", 4800,"fanta.jpg", 1L));

        DB.PRODUCTS.add(new Product(4L, "burger", 12000,"burger.jpg", 2L));
        DB.PRODUCTS.add(new Product(5L, "pitsa", 20000,"pitsa.jpg", 2L));
        DB.PRODUCTS.add(new Product(6L, "lavash", 15000,"lavash.jpg", 2L));

        DB.PRODUCTS.add(new Product(7L, "futbolka", 30000,"futbolka.jpg", 3L));
        DB.PRODUCTS.add(new Product(8L, "krasofka", 50000,"krasofka.jpg", 3L));
        DB.PRODUCTS.add(new Product(9L, "shim", 45000,"shim.jpg", 3L));
    }
}