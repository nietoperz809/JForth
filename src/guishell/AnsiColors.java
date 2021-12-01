package guishell;

import java.awt.*;
import java.util.HashMap;

public class AnsiColors {
    static HashMap<String,Color> map = new HashMap<>();
    static HashMap<Color,String> revmap = new HashMap<>();

    static {
        map.put("\u001B[0m", Color.WHITE);
        map.put("\u001B[30m", Color.BLACK);
        map.put("\u001B[31m", Color.RED);
        map.put("\u001B[32m", Color.GREEN);
        map.put("\u001B[33m", Color.YELLOW);
        map.put("\u001B[34m", Color.BLUE);
        map.put("\u001B[35m", Color.MAGENTA);
        map.put("\u001B[36m", Color.CYAN);
        map.put("\u001B[37m", Color.WHITE);
        for (String key : map.keySet()) {
            revmap.put(map.get(key), key);
        }
    }

    public static Color getColor (String in, Color def) {
        Color ret = map.get(in);
        if (ret == null)
            return def;
        return ret;
    }

    public static String getCode (Color in) {
        return revmap.get(in);
    }
}

