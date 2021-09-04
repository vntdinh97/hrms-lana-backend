package com.hrms.hrms.Utils;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;

public class Helper {
    public static String getMonthForInt(int num) {
        String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return monthNames[num];
    }

    public static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
}
