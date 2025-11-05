package jforth;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializableImage implements Serializable {
    private static final long serialVersionUID = 1L;

    transient BufferedImage image;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageIO.write(image, "png", out); // png is lossless
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        image = ImageIO.read(in);
    }


    public SerializableImage (@org.jetbrains.annotations.NotNull BufferedImage img) throws IOException {
        image = img;
    }

    public BufferedImage getImage() {
        return image;
    }

    public SerializableImage resizeImage (double factor) throws IOException {
        int nwidth = (int) (factor*image.getWidth());
        int nheight = (int) (factor*image.getHeight());
        BufferedImage resizedImage = new BufferedImage(nwidth, nheight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage (image, 0, 0, nwidth, nheight, null);
        g.dispose();
        return new SerializableImage(resizedImage);
    }

    public SerializableImage rotate90 () throws IOException {
        int height = image.getHeight();
        int width = image.getWidth();
        BufferedImage dest = new BufferedImage (height, width, image.getType());
        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.translate((height - width) / 2, (height - width) / 2);
        graphics2D.rotate(Math.PI / 2, height / 2, width / 2);
        graphics2D.drawRenderedImage(image, null);
        return new SerializableImage (dest);
    }

    public SerializableImage mirror () throws IOException {
        int height = image.getHeight();
        int width = image.getWidth();
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-width, 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage ret = op.filter(image, null);
        return new SerializableImage (ret);
    }

    public SerializableImage neg () throws IOException {
        int height = image.getHeight();
        int width = image.getWidth();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = image.getRGB(x,y);
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
                image.setRGB(x, y, p);
            }
        }
        return new SerializableImage (image);
    }
}
