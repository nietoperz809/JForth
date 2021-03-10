package jforth;

import java.util.TreeMap;

public class Roman {
    private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();

    static {
        map.put(1000000, "M̅");
        map.put(900000, "D̅C̅C̅C̅C̅");
        map.put(500000, "D̅");
        map.put(400000, "C̅C̅C̅C̅");
        map.put(100000, "C̅");
        map.put(90000, "L̅X̅X̅X̅X̅");
        map.put(50000, "L̅");
        map.put(40000, "X̅X̅X̅X̅");
        map.put(10000, "X̅");
        map.put(9000, "I̅X̅");
        map.put(5000, "V̅");
        map.put(4000, "I̅V̅");
/////////////////////////////////////
        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");
    }

    public static String toRoman(int number) {
        try {
            int l =  map.floorKey(number);
            if ( number == l ) {
                return map.get(number);
            }
            return map.get(l) + toRoman(number-l);
        } catch (Exception e) {
            return "";
        }
    }

    public static int toArab(String number) throws Exception {
        if (number.isEmpty()) return 0;
        //////////////////////////////
        if (number.startsWith("M̅")) return 1000000 + toArab(number.substring(2));
        if (number.startsWith("D̅C̅C̅C̅C̅")) return 900000 + toArab(number.substring(10));
        if (number.startsWith("D̅")) return 500000 + toArab(number.substring(8));
        if (number.startsWith("C̅C̅C̅C̅")) return 400000 + toArab(number.substring(8));
        if (number.startsWith("C̅")) return 100000 + toArab(number.substring(2));
        if (number.startsWith("L̅X̅X̅X̅X̅")) return 90000 + toArab(number.substring(10));
        if (number.startsWith("L̅")) return 50000 + toArab(number.substring(2));
        if (number.startsWith("X̅X̅X̅X̅")) return 40000 + toArab(number.substring(8));
        if (number.startsWith("X̅")) return 10000 + toArab(number.substring(2));
        if (number.startsWith("I̅X̅")) return 9000 + toArab(number.substring(4));
        if (number.startsWith("V̅")) return 5000 + toArab(number.substring(2));
        if (number.startsWith("I̅V̅")) return 4000 + toArab(number.substring(4));
        //////////////////////////////
        if (number.startsWith("M")) return 1000 + toArab(number.substring(1));
        if (number.startsWith("CM")) return 900 + toArab(number.substring(2));
        if (number.startsWith("D")) return 500 + toArab(number.substring(1));
        if (number.startsWith("CD")) return 400 + toArab(number.substring(2));
        if (number.startsWith("C")) return 100 + toArab(number.substring(1));
        if (number.startsWith("XC")) return 90 + toArab(number.substring(2));
        if (number.startsWith("L")) return 50 + toArab(number.substring(1));
        if (number.startsWith("XL")) return 40 + toArab(number.substring(2));
        if (number.startsWith("X")) return 10 + toArab(number.substring(1));
        if (number.startsWith("IX")) return 9 + toArab(number.substring(2));
        if (number.startsWith("V")) return 5 + toArab(number.substring(1));
        if (number.startsWith("IV")) return 4 + toArab(number.substring(2));
        if (number.startsWith("I")) return 1 + toArab(number.substring(1));
        throw new Exception("Not roman");
    }
}

//    public static int ToArabic(String number) throws Exception {
//        if (number.isEmpty()) return 0;
//        if (number.startsWith("M")) return 1000 + ToArabic(number.substring(1));
//        if (number.startsWith("CM")) return 900 + ToArabic(number.substring(2));
//        if (number.startsWith("D")) return 500 + ToArabic(number.substring(1));
//        if (number.startsWith("CD")) return 400 + ToArabic(number.substring(2));
//        if (number.startsWith("C")) return 100 + ToArabic(number.substring(1));
//        if (number.startsWith("XC")) return 90 + ToArabic(number.substring(2));
//        if (number.startsWith("L")) return 50 + ToArabic(number.substring(1));
//        if (number.startsWith("XL")) return 40 + ToArabic(number.substring(2));
//        if (number.startsWith("X")) return 10 + ToArabic(number.substring(1));
//        if (number.startsWith("IX")) return 9 + ToArabic(number.substring(2));
//        if (number.startsWith("V")) return 5 + ToArabic(number.substring(1));
//        if (number.startsWith("IV")) return 4 + ToArabic(number.substring(2));
//        if (number.startsWith("I")) return 1 + ToArabic(number.substring(1));
//        throw new Exception("Not roman");
//    }

