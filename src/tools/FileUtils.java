package tools;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FileUtils {
    /**
     * Delete a file
     * @param path Path to file
     * @return true if successful
     */
    public static boolean del(String path) {
        File f = new File(path);
        return f.delete();
    }

    /**
     * Get directory as string
     * @param path Path to directory
     * @return Directory string
     */
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

    /**
     * Save a String map
     * @param map Map to save
     * @param filename Name of new file
     * @throws Exception if smth. gone wrong
     */
    public static void saveMap (HashMap<String, Object> map, String filename) throws Exception {
        Properties properties = new Properties();
        for (Map.Entry<String,Object> entry : map.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        properties.store(new FileOutputStream(filename), null);
    }

    /**
     * Load s string map
     * @param filename path of stored map
     * @return A new map constructed from file
     * @throws Exception if smth. gone wrong
     */
    public static HashMap<String, Object> loadMap (String filename) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        Properties properties = new Properties();
        properties.load(new FileInputStream(filename));
        for (String key : properties.stringPropertyNames()) {
            map.put(key, properties.get(key).toString());
        }
        return map;
    }

    /**
     * Save ArrayList of Strings
     * @param as The list
     * @param filename Path to new files
     * @throws Exception if smth. gone wrong
     */
    public static void saveStrings(ArrayList<String> as, String filename) throws Exception {
        FileWriter fw = new FileWriter(filename);
        for (String str : as) {
            fw.write(str + "\n");
        }
        fw.close();
    }

    /**
     * Load a string list from file
     * @param fileName Path to file
     * @return A new string list created from that file
     * @throws Exception if smth. gone wrong
     */
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

    public static void saveObjectAsJson(String name, Object obj) throws IOException {
        String j1 = JsonWriter.objectToJson(obj);
        PrintWriter p = new PrintWriter(name + ".json");
        p.println(JsonWriter.formatJson(j1));
        p.close();
    }

    public static Object loadObjectFromJson(String name) throws IOException {
        byte[] b = Files.readAllBytes(Paths.get(name + ".json"));
        String s = new String(b);
        return JsonReader.jsonToJava(s);
    }

    public static void deepSave (String path, Object obj) throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ObjectOutputStream(baos).writeObject(obj);
        OutputStream outputStream = new FileOutputStream (path);
        baos.writeTo(outputStream);
    }

    public static Object deepLoad (String path) throws Exception
    {
        byte[] array = Files.readAllBytes(Paths.get(path));
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        return new ObjectInputStream(bais).readObject();
    }
}
