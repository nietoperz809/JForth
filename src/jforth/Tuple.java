package jforth;

public class Tuple
{
    public double a, b;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(a).append("|").append(b);
        return sb.toString();
    }

    public Tuple (double a, double b)
    {
        this.a = a;
        this.b = b;
    }

    private Tuple (String in) throws Exception
    {
        String[] parts = in.split("\\|");
        a = Double.parseDouble(parts[0]);
        b = Double.parseDouble(parts[1]);
    }

    public static Tuple parseTuple (String in)
    {
        try {
            return new Tuple(in);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) throws Exception
    {
        Tuple t = new Tuple ("s|3");
    }
}
