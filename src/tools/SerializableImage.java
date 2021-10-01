package tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class SerializableImage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final byte[] bytes;
    int width, height;

    public SerializableImage (BufferedImage img) throws IOException {
        width = img.getWidth();
        height = img.getHeight();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write (img, "png", baos);
        bytes = baos.toByteArray();
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
//        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
//        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
//        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
//        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);

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

}
