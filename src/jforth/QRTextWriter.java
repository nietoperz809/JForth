package jforth;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRTextWriter {

    public String render (String txt) throws Exception
    {
        final int x = 100;
        final int y = 100;
        QRCodeWriter w = new QRCodeWriter();
        BitMatrix matrix = w.encode (txt, BarcodeFormat.QR_CODE, x, y);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.getWidth(); i++)
        {
            for (int j = 0; j < matrix.getWidth(); j++)
            {
                sb.append (matrix.get(i,j) ? '\u3000' : " ");
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
