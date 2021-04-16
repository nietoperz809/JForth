package tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class SerializableImage implements Serializable {
    private static final long serialVersionUID = 1L;
    private byte[] bytes;

    public SerializableImage (BufferedImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write (img, "png", baos);
        bytes = baos.toByteArray();
    }

    public BufferedImage getImage() throws IOException {
        InputStream is = new ByteArrayInputStream(bytes);
        return ImageIO.read(is);
    }
}
