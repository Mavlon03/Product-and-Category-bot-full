package uz.pdp;
import uz.pdp.utils.BotController;

import static uz.pdp.utils.BotController.addCategories;
import static uz.pdp.utils.BotController.addProducts;

public class Main {
    static {
        addCategories();
        addProducts();
    }
    public static void main(String[] args) {
        BotController botController = new BotController();
        botController.start();
    }
}