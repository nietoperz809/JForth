package jforth;

import java.util.TreeMap;

public class Roman {
    private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();

    // M̅

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
//        map.put(1000000, "m");
//        map.put(900000, "dcccc");
//        map.put(500000, "d");
//        map.put(400000, "cccc");
//        map.put(100000, "c");
//        map.put(90000, "lxxxx");
//        map.put(50000, "l");
//        map.put(40000, "xxxx");
//        map.put(10000, "x");
//        map.put(9000, "ix");
//        map.put(5000, "v");
//        map.put(4000, "iv");
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
        int l =  map.floorKey(number);
        if ( number == l ) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number-l);
    }
}
