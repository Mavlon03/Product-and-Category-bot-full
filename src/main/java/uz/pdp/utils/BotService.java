package uz.pdp.utils;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import uz.pdp.Category;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class BotService {
    private static Product selectedProduct = null;

    public static TelegramBot telegramBot = new TelegramBot("7449264666:AAF8u0tmTIVTKcQDET8G-9joMuoI2G6AIac");

    public static TgUser getOrCreateUser(Long chatId) {
        for (TgUser tgUser : DB.USERS) {
            if (tgUser.getChatId().equals(chatId)) {
                return tgUser;
            }
        }
        TgUser tgUser = new TgUser();
        tgUser.setChatId(chatId);
        DB.USERS.add(tgUser);
        return tgUser;
    }

    public static void


    acceptStartWelcomeMessage(TgUser tgUser) {
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(),
                """
                        Assalomu aleykum
                        Botimizga xush kelibsiz!
                        Iltimos kontaktingizni yuboring:
                        """);

        sendMessage.replyMarkup(generateContactButton());
        telegramBot.execute(sendMessage);
        tgUser.setState(TgState.SHARING_CONTACT);
    }

    private static ReplyKeyboardMarkup generateContactButton() {
        KeyboardButton keyboardButton = new KeyboardButton("Kontakt yuborish");
        keyboardButton.requestContact(true);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardButton);
        replyKeyboardMarkup.resizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public static void acceptAndShareLocation(TgUser tgUser, Contact contact) {
        String phone = PhoneNumber.fix(contact.phoneNumber());
        tgUser.setPhone(phone);

        SendMessage sendMessage = new SendMessage(tgUser.getChatId(), """
                Lokatsiya yuboring:
                """);
        sendMessage.replyMarkup(generateLocationButton());
        telegramBot.execute(sendMessage);
        tgUser.setState(TgState.SHARING_LOCATION);
    }

    private static ReplyKeyboardMarkup generateLocationButton() {
        KeyboardButton keyboardButton = new KeyboardButton("Lokatsiyani yuborish");
        keyboardButton.requestLocation(true);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardButton);
        replyKeyboardMarkup.resizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public static void acceptLocationAndChooseMenu(TgUser tgUser, Location location) {
        tgUser.setLocation(location);
        SendMessage sendMessage1 = new SendMessage(tgUser.getChatId(),
                """
                        Lokatsiya qabul qilindi.
                        """);
        sendMessage1.replyMarkup(new ReplyKeyboardRemove());
        telegramBot.execute(sendMessage1);
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(),
                "Lokatsiya qabul qilindi. Kategoriyani tanlang:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        for (Category category : DB.CATEGORIES) {
            inlineKeyboardMarkup.addRow(new InlineKeyboardButton(category.getName()).callbackData("cat_" + category.getCategory_Id()));
        }

        sendMessage.replyMarkup(inlineKeyboardMarkup);
        telegramBot.execute(sendMessage);
        tgUser.setState(TgState.CHOOSING_TYPE);
    }

    public static void acceptMenuAndBeginWorking(TgUser tgUser, CallbackQuery callbackQuery) {
        String data = callbackQuery.data();

        if (data.startsWith("cat_")) {
            Long categoryId = Long.parseLong(data.split("_")[1]);
            tgUser.setTempId(categoryId);
            tgUser.setState(TgState.SHOWING_PR);
        } else if (data.startsWith("prod_")) {
            Long productId = Long.parseLong(data.split("_")[1]);
            showProductDetails(tgUser, productId);
            tgUser.setState(TgState.COUNT_PRODUCT);
        }
    }



    private static void showProductDetails(TgUser tgUser, Long productId) {
        for (Product product : DB.PRODUCTS) {
            if (product.getProduct_Id().equals(productId)) {
                ProductOrder order = getOrCreateOrder(tgUser.getChatId(), product.getProduct_Id());
                double totalPrice = product.getPrice() * order.getQuantity();
                SendMessage sendMessage = new SendMessage(tgUser.getChatId(),
                        product.getName() + " - " + product.getPrice() + " so'm\nSoni: " + order.getQuantity() + "\nUmumiy narxi: " + totalPrice + " so'm");

//                InlineKeyboardMarkup inlineKeyboardMarkup = generateButtons(product, order);

//                sendMessage.replyMarkup(inlineKeyboardMarkup);
                telegramBot.execute(sendMessage);
            }
        }
    }

    private static @NotNull InlineKeyboardMarkup generateButtons(TgUser  tgUser) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("+").callbackData("plus"),
                new InlineKeyboardButton(String.valueOf(tgUser.getCounter())).callbackData("count"),
                new InlineKeyboardButton("-").callbackData("minus")
        );
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Tasdiqlash").callbackData("tasdiq"));

        return inlineKeyboardMarkup;
    }



    private static ProductOrder getOrCreateOrder(Long chatId, Long productId) {
        for (ProductOrder order : DB.PRODUCT_ORDERS) {
            if (order.getChatId().equals(chatId) && order.getProductId().equals(productId)) {
                return order;
            }
        }
        ProductOrder newOrder = new ProductOrder(chatId, productId, 1);
        DB.PRODUCT_ORDERS.add(newOrder);
        return newOrder;
    }



    public static void acceptCatShowPr(TgUser tgUser, String data) {
        SendMessage sendMessage = new SendMessage(tgUser.getChatId(),"Product tanlang");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        for (Product product : DB.PRODUCTS) {
            if (product.getCategory_Id().equals(tgUser.getTempId())){
                inlineKeyboardMarkup.addRow(new InlineKeyboardButton(product.getName() + " - " +product.getPrice()).callbackData("product:" + product.getProduct_Id()));
            }
        }
        sendMessage.replyMarkup(inlineKeyboardMarkup);
        telegramBot.execute(sendMessage);
        tgUser.setState(TgState.CHOOSING_PRODUCT);

    }

    public static void acceptProduct(TgUser tgUser, String data) {
        String[] split = data.split(":");
        String id = split[1].trim();
        long chosenProductId = Long.parseLong(id);
        for (Product product : DB.PRODUCTS) {
            if (product.getProduct_Id().equals(chosenProductId)){
                selectedProduct = product;
            }
        }
        tgUser.setTempId(chosenProductId);

        byte[] photoBytes = getPhoto(selectedProduct);
        if (photoBytes != null) {
            SendPhoto sendPhoto = new SendPhoto(tgUser.getChatId(), photoBytes);
            sendPhoto.replyMarkup(generateButtons(tgUser));
            SendResponse sendResponse = telegramBot.execute(sendPhoto);
            tgUser.setMessageId(sendResponse.message().messageId());
        } else {
            SendMessage sendMessage = new SendMessage(tgUser.getChatId(), "Mahsulot rasmi topilmadi.");
            telegramBot.execute(sendMessage);
        }
        tgUser.setState(TgState.CHOOSING_COUNT);
    }

    @SneakyThrows
    private static byte[] getPhoto(Product selectedProduct) {
        if (selectedProduct != null){
            InputStream inputStream = new FileInputStream(selectedProduct.getImagePath());
            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();
            return bytes;
        }
        else {
            System.out.println("File not found" + selectedProduct.getImagePath());
        }
        return null;
    }

    public static void acceptProductCount(TgUser tgUser, String data) {
        if (data.equals("plus")) {
            tgUser.setCounter(tgUser.getCounter() + 1);
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup(tgUser.getChatId(), tgUser.getMessageId());
            editMessageReplyMarkup.replyMarkup(generateButtonsCount(tgUser));
            telegramBot.execute(editMessageReplyMarkup);
        } else if (data.equals("minus")) {
            if (tgUser.getCounter() > 1) { // Mahsulot soni 1 dan kam bo'lmasligi uchun
                tgUser.setCounter(tgUser.getCounter() - 1);
                EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup(tgUser.getChatId(), tgUser.getMessageId());
                editMessageReplyMarkup.replyMarkup(generateButtonsCount(tgUser));
                telegramBot.execute(editMessageReplyMarkup);
            }
        } else if (data.equals("tasdiq")) {
            if (selectedProduct != null) {
                // Umumiy narxni hisoblash
                int totalPrice = selectedProduct.getPrice() * tgUser.getCounter();

                // Tasdiq xabarini yuborish
                String confirmationMessage = String.format("""
                    ✅ Mahsulot tasdiqlandi:
                    🛒 Mahsulot: %s
                    📦 Soni: %d
                    💰 Umumiy narx: %d so'm
                    """, selectedProduct.getName(), tgUser.getCounter(), totalPrice);

                SendMessage sendMessage = new SendMessage(tgUser.getChatId(), confirmationMessage);
                telegramBot.execute(sendMessage);

                // Tasdiqlangandan keyin foydalanuvchi holatini yangilash
                tgUser.setCounter(1); // Default qiymatga qaytarish
                tgUser.setState(TgState.CHOOSING_PRODUCT);
            } else {
                SendMessage sendMessage = new SendMessage(tgUser.getChatId(), "Xatolik yuz berdi. Mahsulot topilmadi.");
                telegramBot.execute(sendMessage);
            }
        }
    }


    private static InlineKeyboardMarkup generateButtonsCount(TgUser tgUser) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("+").callbackData("plus"),
                new InlineKeyboardButton(String.valueOf(tgUser.getCounter())).callbackData("count"),
                new InlineKeyboardButton("-").callbackData("minus")
        );
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Tasdiqlash").callbackData("tasdiq"));

        return inlineKeyboardMarkup;
    }
}