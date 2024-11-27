package uz.pdp.utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private Long product_Id;
    private String name;
    private Integer price;
    private String imagePath;
    private Long category_Id;
}