package jforth;

import org.alcibiade.asciiart.coord.TextBoxSize;
import org.alcibiade.asciiart.image.rasterize.DashRasterizer;
import org.alcibiade.asciiart.image.rasterize.Rasterizer;
import org.alcibiade.asciiart.raster.CharacterRaster;
import org.alcibiade.asciiart.raster.ExtensibleCharacterRaster;
import org.alcibiade.asciiart.raster.RasterContext;

import java.awt.*;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class BigPrint
{
    private static BufferedImage textToImg (String text)
    {
        int fontsize = 20;
        int margin = 10;
        BufferedImage img = new BufferedImage(1, 1, TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        Font font = new Font("C64 Pro", Font.PLAIN, fontsize);
//        Font font = new Font("Monospaced", Font.PLAIN, fontsize);
        FontMetrics metrics = g.getFontMetrics(font);
        int height = metrics.getHeight();
        int stringWidth = metrics.stringWidth(text);
        int totalwidth = stringWidth+margin+margin;
        img = new BufferedImage (totalwidth, height, TYPE_INT_ARGB);
        g = img.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0, totalwidth, height);
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString(text, margin, fontsize);
        return img;
    }

    private static String imgToText (BufferedImage img, int numchars)
    {
        CharacterRaster raster = new ExtensibleCharacterRaster();
        Rasterizer ri = new DashRasterizer();
        ri.rasterize(img, new RasterContext(raster), new TextBoxSize(numchars*20, 10));
        return raster.toString();
    }

    public static String toBigString (String text)
    {
        BufferedImage img = textToImg(text);
        return imgToText(img, text.length());
    }
}
