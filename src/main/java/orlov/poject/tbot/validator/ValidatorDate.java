package orlov.poject.tbot.validator;

import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDateTime;

@Service
public class ValidatorDate {

    public static LocalDateTime getDate(String date, String time) throws ParseException {
        int day = Integer.parseInt(date.substring(0, date.indexOf(".", 0)));
        int month = Integer.parseInt(date.substring(date.indexOf(".") + 1, date.lastIndexOf(".")));
        int year = Integer.parseInt(date.substring(date.lastIndexOf(".") + 1));
        int hours = Integer.parseInt(time.substring(0, time.indexOf(":")));
        int minutes = Integer.parseInt(time.substring(time.lastIndexOf(":") + 1));
        return LocalDateTime.of(year, month, day, hours, minutes).minusMinutes(1).plusHours(2);
    }


}
