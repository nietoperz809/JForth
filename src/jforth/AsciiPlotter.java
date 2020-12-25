package jforth;

import com.indvd00m.ascii.render.Region;
import com.indvd00m.ascii.render.Render;
import com.indvd00m.ascii.render.api.ICanvas;
import com.indvd00m.ascii.render.api.IContextBuilder;
import com.indvd00m.ascii.render.api.IRender;
import com.indvd00m.ascii.render.elements.Rectangle;
import com.indvd00m.ascii.render.elements.plot.Axis;
import com.indvd00m.ascii.render.elements.plot.AxisLabels;
import com.indvd00m.ascii.render.elements.plot.Plot;
import com.indvd00m.ascii.render.elements.plot.api.IPlotPoint;
import com.indvd00m.ascii.render.elements.plot.misc.PlotPoint;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AsciiPlotter {
    private final int width;
    private final int height;

    public AsciiPlotter (Point dim)
    {
        this.width = dim.x;
        this.height = dim.y;
    }

    public AsciiPlotter(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public String plot (DoubleSequence xvals, DoubleSequence yvals) throws Exception
    {
        if (xvals.length() != yvals.length())
            throw new Exception ("Sequences have different lengths");
        List<IPlotPoint> points = new ArrayList<>();
        for (int s = 0; s< xvals.length(); s++)
        {
            points.add (new PlotPoint(xvals.pick(s), yvals.pick(s)));
        }
        IRender render = new Render();
        IContextBuilder builder = render.newBuilder();
        builder.width(width).height(height);
        builder.element(new Rectangle(0, 0, width, height));
        builder.layer(new Region(1, 1, width-2, height-2));
        builder.element(new Axis(points, new Region(0, 0, width-2, height-2)));
        builder.element(new AxisLabels(points, new Region(0, 0, width-2, height-2)));
        builder.element(new Plot(points, new Region(0, 0, width-2, height-2)));
        ICanvas canvas = render.render(builder.build());
        return canvas.getText();
    }
}
