package jforth;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Canvas //extends JPanel
{
    BufferedImage off_Image;
    Graphics g_off;

    int oldx, oldy;
    Dimension cDim = new Dimension (1024, 1024);
    Color cCol = new Color (255, 255, 255);

    public Canvas() {
        create(new int[]{cCol.getRed(), cCol.getGreen(), cCol.getBlue()});
    }

    public void setCanvasDim (int x, int y) {
        cDim = new Dimension(x,y);
    }

    /**
     * Draw an image
     *
     * @param img Image
     * @param arr x,y,width, height
     */
    public void stamp(BufferedImage img, int[] arr) {
        g_off.drawImage(img, arr[0], arr[1], arr[2], arr[3], null);
    }

    public void setDrawColor(int[] arr) {
        g_off.setColor(new Color(arr[0], arr[1], arr[2]));
    }

    public BufferedImage getImage() {
        return off_Image;
    }

    public void createXY(int[] xy) {
        cDim = new Dimension(xy[0],xy[1]);
        off_Image = new BufferedImage(cDim.width, cDim.height, BufferedImage.TYPE_INT_RGB);
        g_off = off_Image.createGraphics();
        g_off.setColor(cCol);
        g_off.fillRect(0, 0, off_Image.getWidth(), off_Image.getHeight());
    }

    public void create(int[] rgb) {
        off_Image = new BufferedImage(cDim.width, cDim.height, BufferedImage.TYPE_INT_RGB);
        g_off = off_Image.createGraphics();
        cCol = new Color(rgb[0], rgb[1], rgb[2]);
        assert g_off != null;
        g_off.setColor(cCol);
        g_off.fillRect(0, 0, off_Image.getWidth(), off_Image.getHeight());
    }

    public void print(int x, int y, String txt) {
        new Chargen(Color.black, Color.white).printImg(off_Image, txt, x, y, 16);
    }

    public void circle(int[] arr) //int x, int y, int rad1, int rad2)
    {
        g_off.drawOval(arr[0], arr[1], arr[2], arr[3]);
    }

    public void disc(int[] arr) //(int x, int y, int rad1, int rad2)
    {
        g_off.fillOval(arr[0], arr[1], arr[2], arr[3]);
    }

    public void square(int[] arr) //(int x, int y, int width, int height)
    {
        g_off.drawRect(arr[0], arr[1], arr[2], arr[3]);
    }

    public void box(int[] arr) //(int x, int y, int width, int height)
    {
        g_off.fillRect(arr[0], arr[1], arr[2], arr[3]);
    }

    public void line(int[] arr) //(int x1, int y1, int x2, int y2)
    {
        g_off.drawLine(arr[0], arr[1], arr[2], arr[3]);
    }

    public void drawto(int[] arr) //(int x, int y)
    {
        g_off.drawLine(oldx, oldy, arr[0], arr[1]);
        oldx = arr[0];
        oldy = arr[1];
    }

    public void plot(int[] arr) //(int x, int y)
    {
        g_off.drawRect(arr[0], arr[1], 1, 1);
        oldx = arr[0];
        oldy = arr[1];
    }
}
