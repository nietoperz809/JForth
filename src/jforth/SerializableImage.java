package jforth;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;

public class SerializableImage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final byte[] bytes;
    final int width;
    final int height;

    public SerializableImage (BufferedImage img) throws IOException {
        width = img.getWidth();
        height = img.getHeight();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write (img, "png", baos);
        bytes = baos.toByteArray();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public BufferedImage getImage() throws IOException {
        InputStream is = new ByteArrayInputStream(bytes);
        return ImageIO.read(is);
    }

    public SerializableImage resizeImage (double factor) throws IOException {
        int nwidth = (int) (factor*width);
        int nheight = (int) (factor*height);
        BufferedImage resizedImage = new BufferedImage(nwidth, nheight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage (getImage(), 0, 0, nwidth, nheight, null);
        g.dispose();

        return new SerializableImage(resizedImage);
    }

    public SerializableImage rotate90 () throws IOException {
        BufferedImage src = getImage();
        BufferedImage dest = new BufferedImage (height, width, src.getType());
        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.translate((height - width) / 2, (height - width) / 2);
        graphics2D.rotate(Math.PI / 2, height / 2, width / 2);
        graphics2D.drawRenderedImage(src, null);
        return new SerializableImage (dest);
    }

    public SerializableImage mirror () throws IOException {
        BufferedImage src = getImage();
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-src.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage ret = op.filter(src, null);
        return new SerializableImage (ret);
    }

    public SerializableImage neg () throws IOException {
        BufferedImage img = getImage();
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int p = img.getRGB(x,y);
                int a = (p>>24)&0xff;
                int r = (p>>16)&0xff;
                int g = (p>>8)&0xff;
                int b = p&0xff;

                //subtract RGB from 255
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;

                //set new RGB value
                p = (a<<24) | (r<<16) | (g<<8) | b;
                img.setRGB(x, y, p);
            }
        }
        return new SerializableImage (img);
    }
}
