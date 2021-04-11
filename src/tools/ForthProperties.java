package tools;

import java.awt.*;
import java.util.HashMap;

public class ForthProperties {
    private static HashMap<String, Object> map = new HashMap<>();

    static {
        putImgScale (new Point(120, 120));
        putBkColor (Color.BLACK);
    }

    public static Point getImgScale() {
        return (Point)map.get("imgscale");
    }

    public static Color getBkColor() {
        return (Color)map.get("bkcolor");
    }

    public static void putImgScale(Point p) {
        map.put("imgscale", p);
    }

    public static void putBkColor(Color c) {
        map.put("bkcolor", c);
    }
}
