package uz.pdp.utils;

import com.pengrad.telegrambot.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class TgUser {
    private Long chatId;
    private String contact;
    private String phone;
    private Location location;
    private TgState state = TgState.START;
    private Long tempId;
    private Integer counter = 0;
    private Integer messageId;

}