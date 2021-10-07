/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jforth;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * @author Administrator
 */
public class Chargen
{
    private final HashMap<Character, Image> map = new HashMap<>();

    private final int setbit;
    private final int clrbit;

    /**
     * Constructor, fills the char map
     */
    public Chargen (Color bk, Color fg)
    {
        setbit = fg.getRGB();
        clrbit = bk.getRGB();

        map.put('0', getImage(0x980));
        map.put('1', getImage(0x988));
        map.put('2', getImage(0x990));
        map.put('3', getImage(0x998));
        map.put('4', getImage(0x9a0));
        map.put('5', getImage(0x9a8));
        map.put('6', getImage(0x9b0));
        map.put('7', getImage(0x9b8));
        map.put('8', getImage(0x9c0));
        map.put('9', getImage(0x9c8));

        map.put('a', getImage(0x808));
        map.put('b', getImage(0x810));
        map.put('c', getImage(0x818));
        map.put('d', getImage(0x820));
        map.put('e', getImage(0x828));
        map.put('f', getImage(0x830));
        map.put('g', getImage(0x838));
        map.put('h', getImage(0x840));
        map.put('i', getImage(0x848));
        map.put('j', getImage(0x850));
        map.put('k', getImage(0x858));
        map.put('l', getImage(0x860));
        map.put('m', getImage(0x868));
        map.put('n', getImage(0x870));
        map.put('o', getImage(0x878));
        map.put('p', getImage(0x880));
        map.put('q', getImage(0x888));
        map.put('r', getImage(0x890));
        map.put('s', getImage(0x898));
        map.put('t', getImage(0x8a0));
        map.put('u', getImage(0x8a8));
        map.put('v', getImage(0x8b0));
        map.put('w', getImage(0x8b8));
        map.put('x', getImage(0x8c0));
        map.put('y', getImage(0x8c8));
        map.put('z', getImage(0x8d0));

        map.put('A', getImage(0x008));
        map.put('B', getImage(0x010));
        map.put('C', getImage(0x018));
        map.put('D', getImage(0x020));
        map.put('E', getImage(0x028));
        map.put('F', getImage(0x030));
        map.put('G', getImage(0x038));
        map.put('H', getImage(0x040));
        map.put('I', getImage(0x048));
        map.put('J', getImage(0x050));
        map.put('K', getImage(0x058));
        map.put('L', getImage(0x060));
        map.put('M', getImage(0x068));
        map.put('N', getImage(0x070));
        map.put('O', getImage(0x078));
        map.put('P', getImage(0x080));
        map.put('Q', getImage(0x088));
        map.put('R', getImage(0x090));
        map.put('S', getImage(0x098));
        map.put('T', getImage(0x0a0));
        map.put('U', getImage(0x0a8));
        map.put('V', getImage(0x0b0));
        map.put('W', getImage(0x0b8));
        map.put('X', getImage(0x0c0));
        map.put('Y', getImage(0x0c8));
        map.put('Z', getImage(0x0d0));

        map.put('[', getImage(0x8d8));
        //map.put ()); // Pound
        map.put(']', getImage(0x8e8));
        map.put('^', getImage(0x8f0));
        map.put(' ', getImage(0x900));
        map.put('!', getImage(0x908));
        map.put('"', getImage(0x910));
        map.put('#', getImage(0x918));
        map.put('$', getImage(0x920));
        map.put('%', getImage(0x928));
        map.put('&', getImage(0x930));
        map.put('\'', getImage(0x938));
        map.put('(', getImage(0x940));
        map.put(')', getImage(0x948));
        map.put('*', getImage(0x950));
        map.put('+', getImage(0x958));
        map.put(',', getImage(0x960));
        map.put('-', getImage(0x968));
        map.put('.', getImage(0x970));
        map.put('/', getImage(0x978));

        map.put('’', getImage(39 * 8));
        map.put('‘', getImage(39 * 8));
        map.put('”', getImage(34 * 8));
        map.put('“', getImage(34 * 8));
        map.put('–', getImage(0x968));
        map.put('—', getImage(0x968));
        map.put(':', getImage(58 * 8));
        map.put(';', getImage(59 * 8));
        map.put('=', getImage(61 * 8));
        map.put('?', getImage(63 * 8));

        map.put((char) 256, getImage(0x298)); // dummy heart
    }

    private Image getImage (int idx)
    {
        BufferedImage img = new BufferedImage(8, 8, TYPE_INT_ARGB);
        for (int rows = 0; rows < 8; rows++)
        {
            int c = ChargenData.c64Chargen[idx++];
            int i = 128;
            for (int lines = 0; lines < 8; lines++)
            {
                if ((c & i) == i)
                {
                    img.setRGB(lines, rows, setbit);
                }
                else
                {
                    img.setRGB(lines, rows, clrbit);
                }
                i >>>= 1;
            }
        }
        return img;
    }

// --Commented out by Inspection START (1/16/2018 7:40 AM):
//    /**
//     * Prints string array to bitmap
//     *
//     * @param img
//     * @param arr
//     * @param x
//     * @param y
//     */
//    public void printImg (BufferedImage img, String[] arr, int x, int y)
//    {
//        for (String str : arr)
//        {
//            printImg(img, str, x, y);
//            y += 8;
//        }
//    }
// --Commented out by Inspection STOP (1/16/2018 7:40 AM)

    /**
     * Prints String into bitmap
     *
     * @param img Destination bitmap
     * @param s   String to print
     * @param x   start position x
     * @param y   start position y
     */
    public void printImg (BufferedImage img, CharSequence s, int x, int y, int resize)
    {
        int xstart = x;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (c == '\n')
            {
                y += resize;
                x = xstart;
            }
            else
            {
                printImg(img, c, x, y);
                x += resize;
            }
        }
    }

    private void printImg (BufferedImage img, char c, int x, int y)
    {
        Image i = map.get(c);
        if (i == null)
        {
            i = map.get((char) 256);
        }
        Graphics g = img.getGraphics();
        g.drawImage(i, x, y, 16,16,null); // img double sized
    }
}
