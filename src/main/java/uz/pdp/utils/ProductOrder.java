package uz.pdp.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductOrder {
    private Long chatId;
    private Long productId;
    private int quantity;
}