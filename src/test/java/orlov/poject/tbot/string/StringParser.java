package orlov.poject.tbot.string;

public class StringParser {
    public static void main(String[] args) {
        String date = "20.12.2020";
        String time = "11:00";

        String day = date.substring(0, date.indexOf(".", 0));
        System.out.println(day);
        String month = date.substring(date.indexOf(".") + 1, date.lastIndexOf("."));
        System.out.println(month);
        String year = date.substring(date.lastIndexOf(".") + 1);
        System.out.println(year);
        String hours = time.substring(0, time.indexOf(":"));
        System.out.println(hours);
        String minutes = time.substring(time.lastIndexOf(":") + 1);
        System.out.println(minutes);
    }
}
