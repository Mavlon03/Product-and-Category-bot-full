package uz.pdp.utils;


import uz.pdp.Category;

import java.util.ArrayList;
import java.util.List;

public interface DB {
    List<TgUser> USERS = new ArrayList<>();
    List<ProductOrder> PRODUCT_ORDERS = new ArrayList<>();
    List<Product> PRODUCTS = new ArrayList<>();
    List<Category> CATEGORIES = new ArrayList<>();
}
