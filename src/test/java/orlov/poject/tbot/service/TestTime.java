package orlov.poject.tbot.service;

import java.time.LocalDateTime;
import java.util.*;

public class TestTime {
    public static void main(String[] args) {
        long l = System.currentTimeMillis();
//        while (System.currentTimeMillis() - l < 1000 * 60) {
//            try {
//                Thread.sleep(5000);
//                System.out.println(new Date(System.currentTimeMillis()));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        if (System.currentTimeMillis() - l > 1000 * 60){
            System.out.println("БОЛЬШЕ!!!");
        } else {
            System.out.println("МЕНЬШУ");
        }
    }
}
