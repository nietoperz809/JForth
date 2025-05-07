package Catalano;

import org.w3c.dom.Element;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/*
    Accessing any data as byte[] in Java? It's really easy!
    Java has a very strict type system that forbids converting pointers (as you can do in C)
    But Java gives you the "ByteBuffer" to break that restriction!

    Example: to access int[] as bytes, do the following:
    int[] SomeIntArray = new int[]{1,2,3};
    1. Create a ByteBuffer of appropriate size:
        ByteBuffer bb = ByteBuffer.allocate(SomeIntArray.length*4);
    Because every 'int' has 4 bytes, ByteBuffer size must be 4 times of intsize.
    Next create an Int representation of our ByteBuffer:
        IntBuffer ib = bb.asIntBuffer();
    Now we can access our ByteBuffer as int[]! Just do it! Fill in our int[] array:
        ib.put (SomeIntArray);
 */

public class FastBitmap {
    private BufferedImage bufferedImage;
    private WritableRaster raster;
    private int[] pixels;
    private byte[] pixelsGRAY;
    private CoordinateSystem cSystem;
    private int strideX;
    private int strideY;
    private int size;
    //private long crc = -1;

    private void refresh() {
        this.raster = this.getRaster();
        if (this.isGrayscale()) {
            this.pixelsGRAY = ((DataBufferByte)this.raster.getDataBuffer()).getData();
            this.size = this.pixelsGRAY.length;
        }

        if (this.isRGB() || this.isARGB()) {
            this.pixels = ((DataBufferInt)this.raster.getDataBuffer()).getData();
            this.size = this.pixels.length;

//            ByteBuffer bb = ByteBuffer.allocate(this.size*4);
//            IntBuffer ib = bb.asIntBuffer();
//            ib.put(this.pixels);
//
//            CRC32 crc = new CRC32();
//            crc.update(bb.array(), 0, bb.array().length);
//            this.crc = crc.getValue();
        }
    }

    public FastBitmap() {
        this.cSystem = FastBitmap.CoordinateSystem.Matrix;
    }

    public FastBitmap(FastBitmap fastBitmap) {
        this.cSystem = FastBitmap.CoordinateSystem.Matrix;
        this.bufferedImage = fastBitmap.toBufferedImage();
        if (this.getType() == 5) {
            this.toRGB();
        }

        this.setCoordinateSystem(fastBitmap.getCoordinateSystem());
        this.refresh();
    }

    public FastBitmap(BufferedImage bufferedImage) {
        this.cSystem = FastBitmap.CoordinateSystem.Matrix;
        this.bufferedImage = bufferedImage;
        this.prepare();
        this.refresh();
    }

    public FastBitmap(Image image) {
        this.cSystem = FastBitmap.CoordinateSystem.Matrix;
        this.bufferedImage = (BufferedImage)image;
        this.prepare();
        this.refresh();
    }

    public FastBitmap(ImageIcon ico) {
        this.cSystem = FastBitmap.CoordinateSystem.Matrix;
        this.bufferedImage = (BufferedImage)ico.getImage();
        this.prepare();
        this.refresh();
    }

    public FastBitmap(String pathname) {
        this.cSystem = FastBitmap.CoordinateSystem.Matrix;

        try {
            this.bufferedImage = ImageIO.read(new File(pathname));
            this.prepare();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public FastBitmap(int width, int height) {
        this.cSystem = FastBitmap.CoordinateSystem.Matrix;
        this.bufferedImage = new BufferedImage(width, height, 1);
        this.setCoordinateSystem(FastBitmap.CoordinateSystem.Matrix);
        this.refresh();
    }

    public FastBitmap(int width, int height, ColorSpace colorSpace) {
        this.cSystem = FastBitmap.CoordinateSystem.Matrix;
        if (colorSpace == FastBitmap.ColorSpace.RGB) {
            this.bufferedImage = new BufferedImage(width, height, 1);
        } else if (colorSpace == FastBitmap.ColorSpace.Grayscale) {
            this.bufferedImage = new BufferedImage(width, height, 10);
        } else if (colorSpace == FastBitmap.ColorSpace.ARGB) {
            this.bufferedImage = new BufferedImage(width, height, 2);
        }

        this.setCoordinateSystem(FastBitmap.CoordinateSystem.Matrix);
        this.refresh();
    }

    public FastBitmap(int[][] image) {
        this.cSystem = FastBitmap.CoordinateSystem.Matrix;
        this.bufferedImage = new BufferedImage(image[0].length, image.length, 10);
        this.setCoordinateSystem(FastBitmap.CoordinateSystem.Matrix);
        this.refresh();
        this.matrixToImage(image);
    }

    public FastBitmap(int[][][] image) {
        this.cSystem = FastBitmap.CoordinateSystem.Matrix;
        if (image[0][0].length == 3) {
            this.bufferedImage = new BufferedImage(image[0].length, image.length, 1);
        } else {
            this.bufferedImage = new BufferedImage(image[0].length, image.length, 2);
        }

        this.setCoordinateSystem(FastBitmap.CoordinateSystem.Matrix);
        this.refresh();
        this.matrixToImage(image);
    }

    private void prepare() {
        if (this.getType() == 10) {
            this.refresh();
        } else if (this.getType() != 2 && this.getType() != 6) {
            this.toRGB();
        } else {
            this.toARGB();
        }

        this.setCoordinateSystem(FastBitmap.CoordinateSystem.Matrix);
    }


    public ColorSpace getColorSpace() {
        if (this.getType() == 10) {
            return FastBitmap.ColorSpace.Grayscale;
        } else {
            return this.getType() == 2 ? FastBitmap.ColorSpace.ARGB : FastBitmap.ColorSpace.RGB;
        }
    }

    public byte[] getGrayData() {
        return this.pixelsGRAY;
    }

    public void setGrayData(byte[] data) {
        this.pixelsGRAY = data;
    }

    public int[] getRGBData() {
        return this.pixels;
    }

    public int getSize() {
        return this.size;
    }

    public void setRGBData(int[] data) {
        this.pixels = data;
    }

    public void setImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
        this.refresh();
    }

    public void setImage(FastBitmap fastBitmap) {
        this.bufferedImage = fastBitmap.toBufferedImage();
        this.setCoordinateSystem(fastBitmap.getCoordinateSystem());
        this.refresh();
    }

    public CoordinateSystem getCoordinateSystem() {
        return this.cSystem;
    }

    public void setCoordinateSystem(CoordinateSystem coSystem) {
        this.cSystem = coSystem;
        if (coSystem == FastBitmap.CoordinateSystem.Matrix) {
            this.strideX = this.getWidth();
            this.strideY = 1;
        } else {
            this.strideX = 1;
            this.strideY = this.getWidth();
        }

    }

    public BufferedImage toBufferedImage() {
        BufferedImage b = new BufferedImage(this.getWidth(), this.getHeight(), this.getType());
        Graphics g = b.getGraphics();
        g.drawImage(this.bufferedImage, 0, 0, (ImageObserver)null);
        return b;
    }

    public Image toImage() {
        return Toolkit.getDefaultToolkit().createImage(this.bufferedImage.getSource());
    }

    public ImageIcon toIcon() {
        BufferedImage b = new BufferedImage(this.getWidth(), this.getHeight(), this.getType());
        Graphics g = b.getGraphics();
        g.drawImage(this.bufferedImage, 0, 0, (ImageObserver)null);
        ImageIcon ico = new ImageIcon(b);
        return ico;
    }

//    public void toGrayscale() {
//        (new Grayscale()).applyInPlace(this);
//        this.pixels = null;
//    }

    public void toARGB() {
        BufferedImage b = new BufferedImage(this.getWidth(), this.getHeight(), 2);
        Graphics g = b.getGraphics();
        g.drawImage(this.bufferedImage, 0, 0, (ImageObserver)null);
        this.bufferedImage = b;
        this.refresh();
        g.dispose();
    }

    public void toRGB() {
        BufferedImage b = new BufferedImage(this.getWidth(), this.getHeight(), 1);
        Graphics g = b.getGraphics();
        g.drawImage(this.bufferedImage, 0, 0, (ImageObserver)null);
        this.bufferedImage = b;
        this.refresh();
        g.dispose();
    }

    public double[] toArrayGrayAsDouble() {
        double[] array = new double[this.getHeight() * this.getWidth()];

        for(int i = 0; i < array.length; ++i) {
            array[i] = (double)this.getGray(i);
        }

        return array;
    }

    public int[] toArrayGrayAsInt() {
        int[] array = new int[this.getHeight() * this.getWidth()];

        for(int i = 0; i < array.length; ++i) {
            array[i] = this.getGray(i);
        }

        return array;
    }

    public float[] toArrayGrayAsFloat() {
        float[] array = new float[this.getHeight() * this.getWidth()];

        for(int i = 0; i < array.length; ++i) {
            array[i] = (float)this.getGray(i);
        }

        return array;
    }

    public int[][] toMatrixGrayAsInt() {
        int height = this.getHeight();
        int width = this.getWidth();
        int[][] image = new int[height][width];
        int idx = 0;

        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                image[i][j] = this.getGray(idx++);
            }
        }

        return image;
    }

    public double[][] toMatrixGrayAsDouble() {
        int height = this.getHeight();
        int width = this.getWidth();
        double[][] image = new double[height][width];
        int idx = 0;

        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                image[i][j] = (double)this.getGray(idx++);
            }
        }

        return image;
    }

    public float[][] toMatrixGrayAsFloat() {
        int height = this.getHeight();
        int width = this.getWidth();
        float[][] image = new float[height][width];
        int idx = 0;

        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                image[i][j] = (float)this.getGray(idx++);
            }
        }

        return image;
    }

    public int[][][] toMatrixRGBAsInt() {
        int height = this.getHeight();
        int width = this.getWidth();
        int[][][] image = new int[height][width][3];
        int idx = 0;

        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                image[i][j][0] = this.getRed(idx);
                image[i][j][1] = this.getGreen(idx);
                image[i][j][2] = this.getBlue(idx);
                ++idx;
            }
        }

        return image;
    }

    public double[][][] toMatrixRGBAsDouble() {
        int height = this.getHeight();
        int width = this.getWidth();
        double[][][] image = new double[height][width][3];
        int idx = 0;

        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                image[i][j][0] = (double)this.getRed(idx);
                image[i][j][1] = (double)this.getGreen(idx);
                image[i][j][2] = (double)this.getBlue(idx);
                ++idx;
            }
        }

        return image;
    }

    public float[][][] toMatrixRGBAsFloat() {
        int height = this.getHeight();
        int width = this.getWidth();
        float[][][] image = new float[height][width][3];
        int idx = 0;

        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                image[i][j][0] = (float)this.getRed(idx);
                image[i][j][1] = (float)this.getGreen(idx);
                image[i][j][2] = (float)this.getBlue(idx);
                ++idx;
            }
        }

        return image;
    }

    public void matrixToImage(int[][] image) {
        int idx = 0;

        for(int[] ints : image) {
            for(int y = 0; y < image[0].length; ++y) {
                this.setGray(idx++, ints[y]);
            }
        }

    }

    public void matrixToImage(float[][] image) {
        int idx = 0;

        for(int x = 0; x < image.length; ++x) {
            for(int y = 0; y < image[0].length; ++y) {
                this.setGray(idx++, (int)image[x][y]);
            }
        }

    }

    public void matrixToImage(double[][] image) {
        int idx = 0;

        for(int x = 0; x < image.length; ++x) {
            for(int y = 0; y < image[0].length; ++y) {
                this.setGray(idx++, (int)image[x][y]);
            }
        }

    }

    public void matrixToImage(int[][][] image) {
        int idx = 0;
        if (image[0][0].length == 3) {
            for(int x = 0; x < image.length; ++x) {
                for(int y = 0; y < image[0].length; ++y) {
                    this.setRGB(idx++, image[x][y][0], image[x][y][1], image[x][y][2]);
                }
            }
        } else {
            for(int x = 0; x < image.length; ++x) {
                for(int y = 0; y < image[0].length; ++y) {
                    this.setARGB(idx++, image[x][y][0], image[x][y][1], image[x][y][2], image[x][y][3]);
                }
            }
        }

    }

    public void matrixToImage(float[][][] image) {
        int idx = 0;
        if (image[0][0].length == 3) {
            for(int x = 0; x < image.length; ++x) {
                for(int y = 0; y < image[0].length; ++y) {
                    this.setRGB(idx++, (int)image[x][y][0], (int)image[x][y][1], (int)image[x][y][2]);
                }
            }
        } else {
            for(int x = 0; x < image.length; ++x) {
                for(int y = 0; y < image[0].length; ++y) {
                    this.setARGB(idx++, (int)image[x][y][0], (int)image[x][y][1], (int)image[x][y][2], (int)image[x][y][3]);
                }
            }
        }

    }

    public void matrixToImage(double[][][] image) {
        int idx = 0;
        if (image[0][0].length == 3) {
            for(int x = 0; x < image.length; ++x) {
                for(int y = 0; y < image[0].length; ++y) {
                    this.setRGB(idx++, (int)image[x][y][0], (int)image[x][y][1], (int)image[x][y][2]);
                }
            }
        } else {
            for(int x = 0; x < image.length; ++x) {
                for(int y = 0; y < image[0].length; ++y) {
                    this.setARGB(idx++, (int)image[x][y][0], (int)image[x][y][1], (int)image[x][y][2], (int)image[x][y][3]);
                }
            }
        }

    }

    public void Clear() {
        if (this.isGrayscale()) {
            int size = this.pixelsGRAY.length;

            for(int i = 0; i < size; ++i) {
                this.pixelsGRAY[i] = 0;
            }
        } else {
            int size = this.pixels.length;

            for(int i = 0; i < size; ++i) {
                this.pixels[i] = 0;
            }
        }

    }

    public Graphics getGraphics() {
        return this.bufferedImage.getGraphics();
    }

    public void createGraphics() {
        this.bufferedImage.createGraphics();
    }

    private WritableRaster getRaster() {
        return this.bufferedImage.getRaster();
    }

    private int getType() {
        return this.bufferedImage.getType();
    }

    public boolean isGrayscale() {
        return this.bufferedImage.getType() == 10;
    }

    public boolean isRGB() {
        return this.bufferedImage.getType() == 1;
    }

    public boolean isARGB() {
        return this.bufferedImage.getType() == 2;
    }

    public int getWidth() {
        return this.bufferedImage.getWidth();
    }

    public int getHeight() {
        return this.bufferedImage.getHeight();
    }

    public int[] getRGB(int offset) {
        int[] rgb = new int[3];
        rgb[0] = this.pixels[offset] >> 16 & 255;
        rgb[1] = this.pixels[offset] >> 8 & 255;
        rgb[2] = this.pixels[offset] & 255;
        return rgb;
    }

    public int[] getRGB(int x, int y) {
        int[] rgb = new int[3];
        rgb[0] = this.pixels[x * this.strideX + y * this.strideY] >> 16 & 255;
        rgb[1] = this.pixels[x * this.strideX + y * this.strideY] >> 8 & 255;
        rgb[2] = this.pixels[x * this.strideX + y * this.strideY] & 255;
        return rgb;
    }

    public int[] getRGB(IntPoint point) {
        return this.getRGB(point.x, point.y);
    }

    public int getPackedRGB(int offset) {
        return this.pixels[offset];
    }

    public int getPackedRGB(int x, int y) {
        return this.pixels[x * this.strideX + y * this.strideY];
    }

    public int getPackedRGB(IntPoint point) {
        return this.pixels[point.x * this.strideX + point.y * this.strideY];
    }

    public int[] getARGB(int x, int y) {
        int[] argb = new int[4];
        argb[0] = this.pixels[x * this.strideX + y * this.strideY] >> 24 & 255;
        argb[1] = this.pixels[x * this.strideX + y * this.strideY] >> 16 & 255;
        argb[2] = this.pixels[x * this.strideX + y * this.strideY] >> 8 & 255;
        argb[3] = this.pixels[x * this.strideX + y * this.strideY] & 255;
        return argb;
    }

    public int[] getARGB(IntPoint point) {
        return this.getARGB(point.x, point.y);
    }

    public void setRGB(int x, int y, Color color) {
        this.setRGB(x, y, color.r, color.g, color.b);
    }

    public void setRGB(int x, int y, int red, int green, int blue) {
        int a = this.pixels[x * this.strideX + y * this.strideY] >> 24 & 255;
        this.pixels[x * this.strideX + y * this.strideY] = a << 24 | red << 16 | green << 8 | blue;
    }

    public void setRGB(IntPoint point, Color color) {
        this.setRGB(point.x, point.y, color.r, color.g, color.b);
    }

    public void setRGB(IntPoint point, int red, int green, int blue) {
        this.setRGB(point.x, point.y, red, green, blue);
    }

    public void setRGB(int x, int y, int[] rgb) {
        this.pixels[x * this.strideX + y * this.strideY] = rgb[0] << 16 | rgb[1] << 8 | rgb[2];
    }

    public void setRGB(int offset, int red, int green, int blue) {
        int a = this.pixels[offset] >> 24 & 255;
        this.pixels[offset] = a << 24 | red << 16 | green << 8 | blue;
    }

    public void setRGB(int offset, int[] rgb) {
        int a = this.pixels[offset] >> 24 & 255;
        this.pixels[offset] = a << 24 | rgb[0] << 16 | rgb[1] << 8 | rgb[2];
    }

    public void setRGB(int offset, Color color) {
        int a = this.pixels[offset] >> 24 & 255;
        this.pixels[offset] = a << 24 | color.r << 16 | color.g << 8 | color.b;
    }

    public void setRGB(IntPoint point, int[] rgb) {
        this.pixels[point.x * this.strideX + point.y * this.strideY] = rgb[0] << 16 | rgb[1] << 8 | rgb[2];
    }

    public void setRGB(int offset, int color) {
        this.pixels[offset] = color;
    }

    public void setRGB(int x, int y, int color) {
        this.pixels[x * this.strideX + y * this.strideY] = color;
    }

    public void setRGB(IntPoint point, int color) {
        this.pixels[point.x * this.strideX + point.y * this.strideY] = color;
    }

    public void setARGB(int x, int y, int alpha, int red, int green, int blue) {
        this.pixels[x * this.strideX + y * this.strideY] = alpha << 24 | red << 16 | green << 8 | blue;
    }

    public void setARGB(IntPoint point, int alpha, int red, int green, int blue) {
        this.setARGB(point.x, point.y, alpha, red, green, blue);
    }

    public void setARGB(int x, int y, int[] rgb) {
        this.pixels[x * this.strideX + y * this.strideY] = rgb[0] << 24 | rgb[1] << 16 | rgb[2] << 8 | rgb[3];
    }

    public void setARGB(IntPoint point, int[] rgb) {
        this.pixels[point.x * this.getWidth() + point.y] = rgb[0] << 24 | rgb[1] << 16 | rgb[2] << 8 | rgb[3];
    }

    public void setARGB(int offset, int alpha, int red, int green, int blue) {
        this.pixels[offset] = alpha << 24 | red << 16 | green << 8 | blue;
    }

    public void setARGB(int offset, int[] argb) {
        this.pixels[offset] = argb[0] << 24 | argb[1] << 16 | argb[2] << 8 | argb[3];
    }

    public int getGray(int x, int y) {
        return this.pixelsGRAY[x * this.strideX + y * this.strideY] & 255;
    }

    public int getGray(IntPoint point) {
        return this.pixelsGRAY[point.x * this.getWidth() + point.y] & 255;
    }

    public int getGray(int offset) {
        return this.pixelsGRAY[offset] & 255;
    }

    public void setGray(int offset, int value) {
        this.pixelsGRAY[offset] = (byte)value;
    }

    public void setGray(int x, int y, int value) {
        this.pixelsGRAY[x * this.strideX + y * this.strideY] = (byte)value;
    }

    public void setGray(IntPoint point, int value) {
        this.pixelsGRAY[point.x * this.strideX + point.y * this.strideY] = (byte)value;
    }

    public int getAlpha(int x, int y) {
        return this.pixels[x * this.strideX + y * this.strideY] >> 24 & 255;
    }

    public int getAlpha(int offset) {
        return this.pixels[offset] >> 24 & 255;
    }

    public void setAlpha(int offset, int value) {
        this.pixels[offset] = this.pixels[offset] & 16777215 | value << 24;
    }

    public void setAlpha(int x, int y, int value) {
        this.pixels[x * this.strideX + y * this.strideY] = this.pixels[x * this.strideX + y * this.strideY] & 16777215 | value << 24;
    }

    public int getRed(int x, int y) {
        return this.pixels[x * this.strideX + y * this.strideY] >> 16 & 255;
    }

    public int getRed(IntPoint point) {
        return this.getRed(point.x, point.y);
    }

    public int getRed(int offset) {
        return this.pixels[offset] >> 16 & 255;
    }

    public void setRed(int offset, int value) {
        this.pixels[offset] = this.pixels[offset] & -16711681 | value << 16;
    }

    public void setRed(int x, int y, int value) {
        this.pixels[x * this.strideX + y * this.strideY] = this.pixels[x * this.strideX + y * this.strideY] & -16711681 | value << 16;
    }

    public void setRed(IntPoint point, int value) {
        this.setRed(point.x, point.y, value);
    }

    public int getGreen(int x, int y) {
        return this.pixels[x * this.strideX + y * this.strideY] >> 8 & 255;
    }

    public int getGreen(IntPoint point) {
        return this.getGreen(point.x, point.y);
    }

    public int getGreen(int offset) {
        return this.pixels[offset] >> 8 & 255;
    }

    public void setGreen(int offset, int value) {
        this.pixels[offset] = this.pixels[offset] & -65281 | value << 8;
    }

    public void setGreen(int x, int y, int value) {
        this.pixels[x * this.strideX + y * this.strideY] = this.pixels[x * this.strideX + y * this.strideY] & -65281 | value << 8;
    }

    public void setGreen(IntPoint point, int value) {
        this.setGreen(point.x, point.y, value);
    }

    public int getBlue(int x, int y) {
        return this.pixels[x * this.strideX + y * this.strideY] & 255;
    }

    public int getBlue(IntPoint point) {
        return this.getBlue(point.x, point.y);
    }

    public int getBlue(int offset) {
        return this.pixels[offset] & 255;
    }

    public void setBlue(int offset, int value) {
        this.pixels[offset] = this.pixels[offset] & -256 | value;
    }

    public void setBlue(int x, int y, int value) {
        this.pixels[x * this.strideX + y * this.strideY] = this.pixels[x * this.strideX + y * this.strideY] & -256 | value;
    }

    public void setBlue(IntPoint point, int value) {
        this.setBlue(point.x, point.y, value);
    }

    public int clampValues(int value) {
        if (value < 0) {
            return 0;
        } else {
            return value > 255 ? 255 : value;
        }
    }

    public int clampValues(int value, int min, int max) {
        if (value < min) {
            return min;
        } else {
            return value > max ? max : value;
        }
    }

    public void saveAsBMP(String pathname) {
        try {
            ImageIO.write(this.bufferedImage, "bmp", new File(pathname));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void saveAsPNG(String pathname) {
        try {
            ImageIO.write(this.bufferedImage, "png", new File(pathname));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void saveAsGIF(String pathname) {
        try {
            ImageIO.write(this.bufferedImage, "gif", new File(pathname));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void saveAsJPG(String pathname, float quality) {
        try {
            JPEGImageWriteParam params = new JPEGImageWriteParam((Locale)null);
            params.setCompressionMode(2);
            params.setCompressionQuality(quality);
            ImageWriter writer = (ImageWriter)ImageIO.getImageWritersByFormatName("jpg").next();
            writer.setOutput(new FileImageOutputStream(new File(pathname)));
            writer.write((IIOMetadata)null, new IIOImage(this.bufferedImage, (java.util.List)null, (IIOMetadata)null), params);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void saveAsJPG(String pathname, float quality, int xDpi, int yDpi) {
        try {
            ImageWriter writer = (ImageWriter)ImageIO.getImageWritersByFormatName("jpeg").next();
            IIOMetadata imageMetaData = writer.getDefaultImageMetadata(new ImageTypeSpecifier(this.bufferedImage), (ImageWriteParam)null);
            Element tree = (Element)imageMetaData.getAsTree("javax_imageio_jpeg_image_1.0");
            Element jfif = (Element)tree.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("Xdensity", Integer.toString(xDpi));
            jfif.setAttribute("Ydensity", Integer.toString(yDpi));
            jfif.setAttribute("resUnits", "1");
            imageMetaData.setFromTree("javax_imageio_jpeg_image_1.0", tree);
            JPEGImageWriteParam params = new JPEGImageWriteParam((Locale)null);
            params.setCompressionMode(2);
            params.setCompressionQuality(quality);
            writer.setOutput(new FileImageOutputStream(new File(pathname)));
            writer.write(imageMetaData, new IIOImage(this.bufferedImage, (List)null, imageMetaData), params);
            writer.dispose();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void saveAsJPG(String pathname) {
        try {
            ImageIO.write(this.bufferedImage, "jpg", new File(pathname));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static enum CoordinateSystem {
        Cartesian,
        Matrix;
    }

    public static enum ColorSpace {
        Grayscale,
        RGB,
        ARGB;
    }
}
