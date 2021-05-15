package jforth;

public class SizedString {
    private final String str;
    private final int size = 100;

    public String getStr() {
        return str;
    }

    public int getSize() {
        return size;
    }

    public SizedString(String s) {
        str = s;
    }

    @Override
    public String toString() {
        return "Sz{" +
                str +
                "/" + size + '}';
    }
}
