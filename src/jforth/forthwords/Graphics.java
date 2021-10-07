package jforth.forthwords;

import jforth.Canvas;
import jforth.PrimitiveWord;
import jforth.SerializableImage;
import jforth.WordsList;
import tools.Utilities;

import java.util.function.Consumer;

final class Graphics {
    private static final Canvas canvas = new Canvas();

    private static String markInfo (String txt)
    {
        return "(Graphics) "+txt;
    }

    private static void add(WordsList _fw, String name, String info,
                            Consumer<int[]> f) {
        _fw.add(new PrimitiveWord
                (
                        name, markInfo(info),
                        (dStack, vStack) ->
                        {
                            try {
                                int[] arr = Utilities.readDoubleSequence(dStack).asIntArray();
                                f.accept(arr);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));
    }

    static void fill(WordsList _fw) {
        add(_fw, "gcolor", "set drawing color", canvas::setColor);
        add(_fw, "gcircle", "draw circle", canvas::circle);
        add(_fw, "gdisc", "draw disc", canvas::disc);
        add(_fw, "grect", "draw rectangle", canvas::square);
        add(_fw, "gbox", "draw box", canvas::box);
        add(_fw, "gline", "draw line", canvas::line);
        add(_fw, "gdrawto", "draw line from prev x/y to new x/y", canvas::drawto);
        add(_fw, "gplot", "plot x/y", canvas::plot);
        add(_fw, "gclear", "clear canvas", canvas::clear);

        _fw.add(new PrimitiveWord
                (
                        "gstamp", markInfo("draw image to canvas"),
                        (dStack, vStack) ->
                        {
                            try {
                                int[] arr = Utilities.readDoubleSequence(dStack).asIntArray();
                                Object o = dStack.pop();
                                SerializableImage img = (SerializableImage)o;
                                canvas.stamp(img.getImage(), arr);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "gout", markInfo("put canvas onto stack"),
                        (dStack, vStack) ->
                        {
                            try {
                                SerializableImage img = new SerializableImage(canvas.getImage());
                                dStack.push(img);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));
    }
}

/*
500 500 imgscale
{100,100,255} pclear
{255,100,100} pcolor
{300,300,200,200} pdisc
pout .

500 500 imgscale 0 1024 0.01 seq dup cos(x)+sin(2*x) swap f= swap plot .

 */