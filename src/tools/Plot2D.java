package tools;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

public class Plot2D {
    double[] xi;
    double[] yi;

    public Plot2D(double[] x, double[] y) {
        this.xi = x;
        this.yi = y;
    }

    public static double max(double[] t) {
        double maximum = t[0];
        for (int i = 1; i < t.length; i++) {
            if (t[i] > maximum) {
                maximum = t[i];
            }
        }
        return maximum;
    }

    public static double min(double[] t) {
        double minimum = t[0];
        for (int i = 1; i < t.length; i++) {
            if (t[i] < minimum) {
                minimum = t[i];
            }
        }
        return minimum;
    }

    private BufferedImage makeImg(int width, int height) {
        BufferedImage bi = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig2 = bi.createGraphics();

        ig2.setBackground(Color.WHITE);
        ig2.clearRect(0, 0, width, height);
        return bi;
    }

    BufferedImage imgbuff;

    private double round (double in)
    {
        return (double)Math.round(in * 100d) / 100d;
    }

    public Image paint() {
        imgbuff = makeImg(720, 480);
        Graphics2D g2 = (Graphics2D)imgbuff.getGraphics(); //(Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.black);
        int x0 = 70;
        int y0 = 10;
        int xm = 670;
        int ym = 410;
        int xspan = xm - x0;
        int yspan = ym - y0;
        double xmax = max(xi);
        double xmin = min(xi);
        double ymax = max(yi);
        double ymin = min(yi);
        g2.draw(new Line2D.Double(x0, ym, xm, ym));
        g2.draw(new Line2D.Double(x0, ym, x0, y0));
        for (int j = 0; j < 5; j++) {
            int interv = 4;
            g2.drawString("" + round (j * (xmax - xmin) / interv + xmin), j * xspan / interv + x0 -10, ym + 20);
            g2.drawString("" + round (j * (ymax - ymin) / interv + ymin), x0 - 20 - (int) (9 * Math.log10(ymax)),
                    ym - j * yspan / interv + y0 - 5);
            g2.draw(new Line2D.Double(j * xspan / interv + x0, ym, j * xspan / interv + x0, ym + 5));
            g2.draw(new Line2D.Double(x0 - 5, j * yspan / interv + y0, x0, j * yspan / interv + y0));
        }
        for (int i = 0; i < xi.length; i++) {
            int f = (int) ((xi[i] - xmin) * xspan / (xmax - xmin));
            int h = (int) (((ymax - ymin) - (yi[i] - ymin)) * yspan / (ymax - ymin));
            g2.drawString("o", x0 + f - 3, h + 14);
        }
        for (int i = 0; i < xi.length - 1; i++) {
            int f = (int) ((xi[i] - xmin) * xspan / (xmax - xmin));
            int f2 = (int) ((xi[i + 1] - xmin) * xspan / (xmax - xmin));
            int h = (int) (((ymax - ymin) - (yi[i] - ymin)) * yspan / (ymax - ymin));
            int h2 = (int) (((ymax - ymin) - (yi[i + 1] - ymin)) * yspan / (ymax - ymin));
            g2.draw(new Line2D.Double(f + x0, h + y0, f2 + x0, h2 + y0));
        }
        return imgbuff;
//        File outputfile =
//                new File("C:\\Users\\Administrator\\Desktop\\java\\JavaPlotter\\lib\\saved.png");
//        try {
//            ImageIO.write(imgbuff, "png", outputfile);
//        } catch (IOException e) {
//            System.out.println("fuck!");
//        }
    }

    public static void main(String args[]) {
        double[] r = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10};
        double[] t = {2.7, 2.8, 31.4, 38.1, 58.0, 76.2, 100.5, 130.0, 149.3, 180.09, 120};
        Plot2D applet = new Plot2D(r, t);
        applet.paint();
    }
}
