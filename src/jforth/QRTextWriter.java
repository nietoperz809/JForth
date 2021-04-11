package jforth;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.*;
import java.awt.image.BufferedImage;

public class QRTextWriter {

    public Image render (String txt) throws Exception
    {
        if (txt.isEmpty())
            return null;
        final int x = 800;
        final int y = 800;
        QRCodeWriter w = new QRCodeWriter();
        BitMatrix m = w.encode(txt, BarcodeFormat.QR_CODE, x, y);
        BufferedImage _img = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
        _img.createGraphics();
        Graphics2D graphics = (Graphics2D) _img.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, x, y);
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < x; i++)
        {
            for (int j = 0; j < y; j++)
            {
                if (m.get(i, j))
                {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        return _img;
    }
}
