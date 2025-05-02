// Catalano Imaging Library
// The Catalano Framework
//
// Copyright © Diego Catalano, 2012-2016
// diego.catalano at live.com
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//

package Catalano;

//import Catalano.Math.Matrix;

/**
 * Convert between different color spaces supported.
 * 
 * RGB -> CIE-L*A*B* -> RGB
 * RGB -> CIE-L*c*h -> RGB
 * RGB -> CMYK -> RGB
 * RGB -> IHS -> RGB
 * RGB -> HLS -> RGB
 * RGB -> HunterLAB -> RGB
 * RGB -> HSV -> RGB
 * RGB -> RGChromaticity
 * RGB -> XYZ -> RGB
 * RGB -> YCbCr -> RGB
 * RGB -> YCC -> RGB
 * RGB -> YCoCg -> RGB
 * RGB -> YES -> RGB
 * RGB -> YIQ -> RGB
 * RGB -> YUV -> RGB
 * XYZ -> CIE-L*A*B* -> XYZ
 * XYZ -> HunterLAB -> XYZ
 * XYZ -> LMS -> XYZ
 * XYZ -> xyY -> XYZ
 * 
 * @author Diego Catalano
 */
public class ColorConverter {
    
    //HPE forward
    private static final double[][] hpe_f = new double[][]{
        {0.38971, 0.68898,-0.07868},
        {-0.22981, 1.18340, 0.04641},
        {0.00000, 0.00000, 1.00000}
    };
    
    //HPE backward
    private static final double[][] hpe_b = new double[][]{
        {1.91020, -1.11212, 0.20191},
        {0.37095, 0.62905, -0.00001},
        {0.00000, 0.00000, 1.00000}
    };
    
    //Bradford forward
    private static final double[][] bradford_f = new double[][]{
        {0.8951000,0.2664000,-0.1614000},
        {-0.7502000,1.7135000,0.0367000},
        {0.0389000,-0.0685000,1.0296000}
    };
    
    //Bradford backward
    private static final double[][] bradford_b = new double[][]{
        {0.9869929,-0.1470543,0.1599627},
        {0.4323053,0.5183603,0.0492912},
        {-0.0085287,0.0400428,0.9684867}
    };
    
    //VonKries forward
    private static final double[][] vonkries_f = new double[][]{
        {0.4002, 0.7076, -0.0808},
        {-0.2263, 1.1653, 0.0457},
        {0, 0, 0.9182}
    };
    
    //VonKries backward
    private static final double[][] vonkries_b = new double[][]{
        {1.86007, -1.12948, 0.21990},
        {0.36122, 0.63880, -0.00001},
        {0.00000, 0.00000, 1.08909}
    };
    
    //CAT97 forward
    private static final double[][] cat97_f = new double[][]{
        {0.8562, 0.3372, -0.1934},
        {-0.8360, 1.8327, 0.0033},
        {0.0357, -0.00469, 1.0112}
    };
    
    //CAT97 backward
    private static final double[][] cat97_b = new double[][]{
        {0.9838112, -0.1805292, 0.1887508},
        {0.4488317, 0.4632779, 0.0843307},
        {-0.0326513, 0.0085222, 0.9826514}
    };
    
    //CAT02 forward
    private static final double[][] cat02_f = new double[][]{
        {0.7328, 0.4296, -0.1624},
        {-0.7036, 1.6975, 0.0061},
        {0.0030, 0.0136, 0.9834}
    };
    
    //CAT02 backward
    private static final double[][] cat02_b = new double[][]{
        {1.0961238, -0.2788690, 0.1827452},
        {0.4543690, 0.4735332, 0.0720978},
        {-0.0096276, -0.0056980, 1.0153256}
    };
    
    /**
     * LMS Transformation matrix.
     */
    public static enum LMS{
        
        /**
         * Hunt-Pointer-Estevez.
         */
        HPE,
        
        /**
         * Bradford.
         */
        Bradford,
        
        /**
         * Von Kries.
         */
        VonKries,
        
        /**
         * CIECAM97s.
         */
        CAT97,
        
        /**
         * CIECAM02.
         */
        CAT02
    }
    
    public static enum YCbCrColorSpace {ITU_BT_601,ITU_BT_709_HDTV};
    
    //Used in CIE-LAB conversions
    private static double k = 903.2962962962963; //24389/27
    private static double e = 0.0088564516790356; //216/24389

    /**
     * Don't let anyone instantiate this class.
     */
    private ColorConverter() {}
    
    /**
     * RGB -> CMYK
     * @param color Color.
     * @return CMYK color space. Normalized.
     */
    public static double[] RGBtoCMYK(Color color){
        return RGBtoCMYK(color.r, color.g, color.b);
    }
    
    /**
     * RGB -> CMYK
     * @param rgb RGB values.
     * @return CMYK color space. Normalized.
     */
    public static double[] RGBtoCMYK(int[] rgb){
        return RGBtoCMYK(rgb[0], rgb[1], rgb[2]);
    }
    
    /**
     * RGB -> CMYK
     * @param red Values in the range [0..255].
     * @param green Values in the range [0..255].
     * @param blue Values in the range [0..255].
     * @return CMYK color space. Normalized.
     */
    public static double[] RGBtoCMYK(int red, int green, int blue){
        double[] cmyk = new double[4];
        
        double r = red / 255f;
        double g = green / 255f;
        double b = blue / 255f;
        
        double k = 1.0f - Math.max(r, Math.max(g, b));
        double c = (1f-r-k) / (1f-k);
        double m = (1f-g-k) / (1f-k);
        double y = (1f-b-k) / (1f-k);
        
        cmyk[0] = c;
        cmyk[1] = m;
        cmyk[2] = y;
        cmyk[3] = k;
        
        return cmyk;
    }
    
    /**
     * CMYK -> RGB
     * @param cmyk CMYK values.
     * @return RGB color space.
     */
    public static int[] CMYKtoRGB(double[] cmyk){
        return CMYKtoRGB(cmyk[0], cmyk[1], cmyk[2], cmyk[3]);
    }
    
    /**
     * CMYK -> RGB
     * @param c Cyan.
     * @param m Magenta.
     * @param y Yellow.
     * @param k Black.
     * @return RGB color space.
     */
    public static int[] CMYKtoRGB(double c, double m, double y, double k){
        int[] rgb = new int[3];
        
        rgb[0] = (int)(255 * (1-c) * (1-k));
        rgb[1] = (int)(255 * (1-m) * (1-k));
        rgb[2] = (int)(255 * (1-y) * (1-k));
        
        return rgb;
    }
    
    /**
     * RGB -> IHS
     * @param color Color.
     * @return IHS color space. Normalized.
     */
    public static double[] RGBtoIHS(Color color){
        return RGBtoCMYK(color.r, color.g, color.b);
    }
    
    /**
     * RGB -> IHS
     * @param rgb RGB values.
     * @return RGB color space.
     */
    public static double[] RGBtoIHS(int[] rgb){
        return RGBtoIHS(rgb[0], rgb[1], rgb[2]);
    }
    
    /**
     * RGB -> IHS
     * @param red Values in the range [0..255].
     * @param green Values in the range [0..255].
     * @param blue Values in the range [0..255].
     * @return IHS color space. Normalized.
     */
    public static double[] RGBtoIHS(int red, int green, int blue){
        
        double r = red / 255f;
        double g = green / 255f;
        double b = blue / 255f;
        
        double i = r+g+b;
        
        double h;
        if(b == Math.min(Math.min(r, g), b)){
            h = (g-b) / (i-3*b);
        }
        else if (r == Math.min(Math.min(r, g), b)){
            h = (b-r) / (i-3*r) + 1;
        }
        else{
            h = (r-g) / (i-3*g) + 2;
        }
        
        double s;
        if(h >= 0 && h <= 1){
            s = (i-3*b) / i;
        }
        else if(h >= 1 && h <= 2){
            s = (i-3*r) / i;
        }
        else{
            s = (i-3*g) / i;
        }
        
        return new double[] {i,h,s};
        
    }
    
    /**
     * IHS -> RGB
     * @param ihs IHS vector.
     * @return RGB color space.
     */
    public static double[] IHStoRGB(double[] ihs){
        
        if(ihs[1] >= 0 && ihs[1] <= 1){
            double r = ihs[0] * (1 + 2*ihs[2]-3*ihs[2]*ihs[1]) / 3;
            double g = ihs[0] * (1 - ihs[2]+3*ihs[2]*ihs[1]) / 3;
            double b = ihs[0] * (1 - ihs[2]) / 3;
            return new double[] {r*255,g*255,b*255};
        }
        else if(ihs[1] >= 1 && ihs[1] <= 2){
            double r = ihs[0] * (1 - ihs[2]) / 3;
            double g = ihs[0] * (1 + 2*ihs[2] - 3*ihs[2]*(ihs[1] - 1)) / 3;
            double b = ihs[0] * (1 - ihs[2] + 3*ihs[2]*(ihs[1] - 1)) / 3;
            return new double[] {r*255,g*255,b*255};
        }
        else{
            double r = ihs[0] * (1 - ihs[2] + 3*ihs[2]*(ihs[1] - 2)) / 3;
            double g = ihs[0] * (1 - ihs[2]) / 3;
            double b = ihs[0] * (1 + 2*ihs[2] - 3*ihs[2]*(ihs[1] - 2)) / 3;
            return new double[] {r*255,g*255,b*255};
        }
    }
    
    /**
     * RGB -> YUV.
     * @param color Color.
     * @return YUV color space.
     */
    public static double[] RGBtoYUV(Color color){
        return RGBtoYUV(color.r, color.g, color.b);
    }
    
    /**
     * RGB -> YUV.
     * Y in the range [0..1].
     * U in the range [-0.5..0.5].
     * V in the range [-0.5..0.5].
     * @param red Values in the range [0..255].
     * @param green Values in the range [0..255].
     * @param blue Values in the range [0..255].
     * @return YUV color space.
     */
    public static double[] RGBtoYUV(int red, int green, int blue){
        
        double r = (double)red / 255;
        double g = (double)green / 255;
        double b = (double)blue / 255;
        
        double[] yuv = new double[3];
        double y,u,v;
        
        y = (double)(0.299 * r + 0.587 * g + 0.114 * b);
        u = (double)(-0.14713 * r - 0.28886 * g + 0.436 * b);
        v = (double)(0.615 * r - 0.51499 * g - 0.10001 * b);
        
        yuv[0] = y;
        yuv[1] = u;
        yuv[2] = v;
        
        return yuv;
    }
    
    /**
     * YUV -> RGB.
     * @param y Luma. In the range [0..1].
     * @param u Chrominance. In the range [-0.5..0.5].
     * @param v Chrominance. In the range [-0.5..0.5].
     * @return RGB color space.
     */
    public static int[] YUVtoRGB(double y, double u, double v){
        int[] rgb = new int[3];
        double r,g,b;
        
        r = (double)((y + 0.000 * u + 1.140 * v) * 255);
        g = (double)((y - 0.396 * u - 0.581 * v) * 255);
        b = (double)((y + 2.029 * u + 0.000 * v) * 255);
        
        rgb[0] = (int)r;
        rgb[1] = (int)g;
        rgb[2] = (int)b;
        
        return rgb;
    }
    
    /**
     * RGB -> YIQ.
     * @param color Color.
     * @return YIQ color space.
     */
    public static double[] RGBtoYIQ(Color color){
        return RGBtoYIQ(color.r, color.g, color.b);
    }
    
    /**
     * RGB -> YIQ.
     * @param red Values in the range [0..255].
     * @param green Values in the range [0..255].
     * @param blue Values in the range [0..255].
     * @return YIQ color space.
     */
    public static double[] RGBtoYIQ(int red, int green, int blue){
        double[] yiq = new double[3];
        double y,i,q;
        
        double r = (double)red / 255;
        double g = (double)green / 255;
        double b = (double)blue / 255;
        
        y = (double)(0.299 * r + 0.587 * g + 0.114 * b);
        i = (double)(0.596 * r - 0.275 * g - 0.322 * b);
        q = (double)(0.212 * r - 0.523 * g + 0.311 * b);
        
        yiq[0] = y;
        yiq[1] = i;
        yiq[2] = q;
        
        return yiq;
    }
    
    /**
     * YIQ -> RGB.
     * @param y Luma. Values in the range [0..1].
     * @param i In-phase. Values in the range [-0.5..0.5].
     * @param q Quadrature. Values in the range [-0.5..0.5].
     * @return RGB color space.
     */
    public static int[] YIQtoRGB(double y, double i, double q){
        int[] rgb = new int[3];
        int r,g,b;
        
        r = (int)((y + 0.956 * i + 0.621 * q) * 255);
        g = (int)((y - 0.272 * i - 0.647 * q) * 255);
        b = (int)((y - 1.105 * i + 1.702 * q) * 255);
        
        r = Math.max(0,Math.min(255,r));
        g = Math.max(0,Math.min(255,g));
        b = Math.max(0,Math.min(255,b));
        
        rgb[0] = r;
        rgb[1] = g;
        rgb[2] = b;
        
        return rgb;
    }
    
    public static double[] RGBtoYCbCr(Color color, YCbCrColorSpace colorSpace){
        return RGBtoYCbCr(color.r, color.g, color.b, colorSpace);
    }
    
    public static double[] RGBtoYCbCr(int red, int green, int blue, YCbCrColorSpace colorSpace){
        
        double r = (double)red / 255;
        double g = (double)green / 255;
        double b = (double)blue / 255;
        
        double[] YCbCr = new double[3];
        double y,cb,cr;
        
        if (colorSpace == YCbCrColorSpace.ITU_BT_601) {
            y = (double)(0.299 * r + 0.587 * g + 0.114 * b);
            cb = (double)(-0.169 * r - 0.331 * g + 0.500 * b);
            cr = (double)(0.500 * r - 0.419 * g - 0.081 * b);
        }
        else{
            y = (double)(0.2215 * r + 0.7154 * g + 0.0721 * b);
            cb = (double)(-0.1145 * r - 0.3855 * g + 0.5000 * b);
            cr = (double)(0.5016 * r - 0.4556 * g - 0.0459 * b);
        }
        
        YCbCr[0] = (double)y;
        YCbCr[1] = (double)cb;
        YCbCr[2] = (double)cr;
        
        return YCbCr;
    }
    
    public static int[] YCbCrtoRGB(double y, double cb, double cr, YCbCrColorSpace colorSpace){
        int[] rgb = new int[3];
        double r,g,b;
        
        if (colorSpace == YCbCrColorSpace.ITU_BT_601) {
            r = (double)(y + 0.000 * cb + 1.403 * cr) * 255;
            g = (double)(y - 0.344 * cb - 0.714 * cr) * 255;
            b = (double)(y + 1.773 * cb + 0.000 * cr) * 255;
        }
        else{
            r = (double)(y + 0.000 * cb + 1.5701 * cr) * 255;
            g = (double)(y - 0.1870 * cb - 0.4664 * cr) * 255;
            b = (double)(y + 1.8556 * cb + 0.000 * cr) * 255;
        }
        
        rgb[0] = (int)r;
        rgb[1] = (int)g;
        rgb[2] = (int)b;
        
        return rgb;
    }
    
    /**
     * Rg-Chromaticity space is already known to remove ambiguities due to illumination or surface pose.
     * @param color Color.
     * @return Normalized RGChromaticity. Range[0..1].
     */
    public static double[] RGChromaticity(Color color){
        return RGChromaticity(color.r, color.g, color.b);
    }
    
    /**
     * Rg-Chromaticity space is already known to remove ambiguities due to illumination or surface pose.
     * @param red Red coefficient.
     * @param green Green coefficient.
     * @param blue Blue coefficient.
     * @return Normalized RGChromaticity. Range[0..1].
     */
    public static double[] RGChromaticity(int red, int green, int blue){
        double[] color = new double[5];
        
        double sum = red + green + blue;
        
        //red
        color[0] = red / sum;
        
        //green
        color[1] = green / sum;
        
        //blue
        color[2] = 1 - color[0] - color[1];
        
        double rS = color[0] - 0.333;
        double gS = color[1] - 0.333;
        
        //saturation
        color[3] = Math.sqrt(rS * rS + gS * gS);
        
        //hue
        color[4] = Math.atan(rS / gS);
        
        return color;
    }
    
    /**
     * RGB -> HSV.
     * Adds (hue + 360) % 360 for represent hue in the range [0..359].
     * @param color Color.
     * @return HSV color space.
     */
    public static double[] RGBtoHSV(Color color){
        return RGBtoHSV(color.r, color.g, color.b);
    }
    
    /**
     * RGB -> HSV.
     * Adds (hue + 360) % 360 for represent hue in the range [0..359].
     * @param red Red coefficient. Values in the range [0..255].
     * @param green Green coefficient. Values in the range [0..255].
     * @param blue Blue coefficient. Values in the range [0..255].
     * @return HSV color space.
     */
    public static double[] RGBtoHSV(int red, int green, int blue){
        double[] hsv = new double[3];
        double r = red / 255f;
        double g = green / 255f;
        double b = blue / 255f;
        
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double delta = max - min;
        
        // Hue
        if (max == min){
            hsv[0] = 0;
        }
        else if (max == r){
            hsv[0] = ((g - b) / delta) * 60f;
        }
        else if (max == g){
            hsv[0] = ((b - r) / delta + 2f) * 60f;
        }
        else if (max == b){
            hsv[0] = ((r - g) / delta + 4f) * 60f;
        }
        
        // Saturation
        if (delta == 0)
            hsv[1] = 0;
        else
            hsv[1] = delta / max;
        
        //Value
        hsv[2] = max;
        
        return hsv;
    }
    
    /**
     * HSV -> RGB.
     * @param hue Hue.
     * @param saturation Saturation. In the range[0..1].
     * @param value Value. In the range[0..1].
     * @return RGB color space. In the range[0..255].
     */
    public static int[] HSVtoRGB(double hue, double saturation, double value){
        int[] rgb = new int[3];
        
        double hi = (double)Math.floor(hue / 60.0) % 6;
        double f =  (double)((hue / 60.0) - Math.floor(hue / 60.0));
        double p = (double)(value * (1.0 - saturation));
        double q = (double)(value * (1.0 - (f * saturation)));
        double t = (double)(value * (1.0 - ((1.0 - f) * saturation)));
        
        if (hi == 0){
            rgb[0] = (int)(value * 255);
            rgb[1] = (int)(t * 255);
            rgb[2] = (int)(p * 255);
        }
        else if (hi == 1){
            rgb[0] = (int)(q * 255);
            rgb[1] = (int)(value * 255);
            rgb[2] = (int)(p * 255);
        }
        else if (hi == 2){
            rgb[0] = (int)(p * 255);
            rgb[1] = (int)(value * 255);
            rgb[2] = (int)(t * 255);
        }
        else if (hi == 3){
            rgb[0] = (int)(p * 255);
            rgb[1] = (int)(value * 255);
            rgb[2] = (int)(q * 255);
        }
        else if (hi == 4){
            rgb[0] = (int)(t * 255);
            rgb[1] = (int)(value * 255);
            rgb[2] = (int)(p * 255);
        }
        else if (hi == 5){
            rgb[0] = (int)(value * 255);
            rgb[1] = (int)(p * 255);
            rgb[2] = (int)(q * 255);
        }
        
        return rgb;
    }
    
}
