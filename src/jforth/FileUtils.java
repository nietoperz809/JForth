package jforth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class FileUtils {
    public static boolean del(String path) {
        File f = new File(path);
        return f.delete();
    }

    public static String dir(String path) {
        StringBuilder sb = new StringBuilder();
        path = path.replace('/', '\\');
        File[] filesInFolder = new File(path).listFiles();
        if (filesInFolder == null) {
            return "";
        }
        Arrays.sort(filesInFolder, (f1, f2) ->
        {
            if (f2.isDirectory()) {
                return 1;
            }
            return -1;
        });
        for (final File fileEntry : filesInFolder) {
            String formatted;
            if (fileEntry.isDirectory()) {
                formatted = String.format("<%s>\n", fileEntry.getName());
            } else {
                formatted = String.format("%-15s = %d\n", fileEntry.getName(), fileEntry.length());
            }
            sb.append(formatted);
        }
        return sb.toString();
    }

    public static void saveStrings(ArrayList<String> as, String filename) throws Exception {
        FileWriter fw = new FileWriter(filename);
        for (String str : as) {
            fw.write(str + "\n");
        }
        fw.close();
    }

    public static ArrayList<String> loadStrings(String fileName) throws Exception {
        ArrayList<String> ret = new ArrayList<>();
        BufferedReader file = new BufferedReader(new FileReader(fileName));
        while (file.ready()) {
            String s = file.readLine();
            if (s == null) {
                break;
            }
            s = s.trim();
            if (!s.isEmpty()) {
                ret.add(s);
            }
        }
        file.close();
        return ret;
    }

//    public static void saveObject(String name, Object obj) throws IOException {
//        String j1 = JsonWriter.objectToJson(obj);
//        PrintWriter p = new PrintWriter(name + ".json");
//        p.println(JsonWriter.formatJson(j1));
//        p.close();
//    }
//
//    public static Object loadObject(String name) throws IOException {
//        byte[] b = Files.readAllBytes(Paths.get(name + ".json"));
//        String s = new String(b);
//        return JsonReader.jsonToJava(s);
//    }

}
