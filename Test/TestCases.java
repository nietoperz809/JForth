import jforth.JForth;
import org.junit.Assert;
import org.junit.Test;
import tools.StringStream;

/**
 * Created by Administrator on 4/15/2017.
 */
public class TestCases
{
    private String check (String prg, String call)
    {
        StringStream _ss = new StringStream();
        JForth _forth = new JForth();
        _forth.setPrintStream(_ss.getPrintStream());

        _forth.singleShot(prg);
        _ss.clear();
        _forth.singleShot (call);
        return _ss.toString();
    }

    @Test
    public void TestPrg2()
    {
        String s = check (": test 10 0 do i . loop ;", "test");
        System.out.println(s);
        Assert.assertEquals("0123456789 OK\n> ", s);
    }

    @Test
    public void TestPrg3()
    {
        String s = check (": test variable hello hello ! hello @ length hello @ ;",
                "{1,2,3,4} test . .");
        System.out.println(s);
        Assert.assertEquals("{1,2,3,4}4 OK\n> ", s);
    }

    @Test
    public void TestPrg4()
    {
        String s = check (": test variable hello hello ! hello @ length fact ;",
                "{1,2,3,4} test .");
        System.out.println(s);
        Assert.assertEquals("24 OK\n> ", s);
    }

    @Test
    public void TestPrg5()
    {
        String s = check (": test variable hello hello ! hello @ length fact 0 ;",
                "{1,2,3,4} test . .");
        System.out.println(s);
        Assert.assertEquals("024 OK\n> ", s);
    }

    @Test
    public void TestPrg6()
    {
        String s = check (": test variable hello hello ! hello @ length 24 0 do i . loop ;",
                "{1,2,3,4} test .");
        System.out.println(s);
        Assert.assertEquals("012345678910111213141516171819202122234 OK\n> ", s);
    }

    @Test
    public void TestPrg7()
    {
        String s = check (": test variable hello hello ! hello @ length fact 0 do i . loop ;",
                "{1,2,3} test");
        System.out.println(s);
        Assert.assertEquals("012345 OK\n> ", s);
    }

    @Test
    public void TestPrg_Permute()
    {
        String result = "1 -> {1,4,3,2}\r\n" +
                "2 -> {2,1,3,4}\r\n" +
                "3 -> {3,1,4,2}\r\n" +
                "4 -> {4,1,3,2}\r\n" +
                "5 -> {1,2,4,3}\r\n" +
                "6 -> {2,4,1,3}\r\n" +
                "7 -> {3,2,1,4}\r\n" +
                "8 -> {4,2,1,3}\r\n" +
                "9 -> {1,3,4,2}\r\n" +
                "10 -> {2,3,1,4}\r\n" +
                "11 -> {3,4,1,2}\r\n" +
                "12 -> {4,3,1,2}\r\n" +
                "13 -> {1,4,2,3}\r\n" +
                "14 -> {2,1,4,3}\r\n" +
                "15 -> {3,1,2,4}\r\n" +
                "16 -> {4,1,2,3}\r\n" +
                "17 -> {1,2,3,4}\r\n" +
                "18 -> {2,4,3,1}\r\n" +
                "19 -> {3,2,4,1}\r\n" +
                "20 -> {4,2,3,1}\r\n" +
                "21 -> {1,3,2,4}\r\n" +
                "22 -> {2,3,4,1}\r\n" +
                "23 -> {3,4,2,1}\r\n" +
                "24 -> {4,3,2,1}\r\n" +
                " OK\n> ";
        String s = check (": test variable hello hello ! " +
                        "hello @ length fact 0 do i 1 + . " +
                        "sp \"->\" . sp hello @ i permute . cr loop ;\n",
                "{1,2,3,4} test");
        System.out.println(s);
        Assert.assertEquals(result, s);
    }

    @Test
    public void TestStringRev()
    {
        String s = check ("\"hello\" rev toString",
                ".");
        System.out.println(s);
        Assert.assertEquals("olleh OK\n> ", s);
    }

    @Test
    public void TestSortString  ()
    {
        String s = check ("\"thequickbrownfoxjumpsoverthelazydog\" sort unique toString",
                ".");
        System.out.println(s);
        Assert.assertEquals("abcdefghijklmnopqrstuvwxyz OK\n> ", s);
    }

    @Test
    public void TestPrimFac  ()
    {
        String s = check ("{2,3,5,7,11,13,17,19,23} prod factor 8 factor",
                ". .");
        System.out.println(s);
        Assert.assertEquals("{2,2,2}{2,3,5,7,11,13,17,19,23} OK\n> ", s);
    }

    @Test
    public void TestSub()
    {
        String s = check ("12 11 - 12L 11 - 12L 11.0 - 12.0 11 -",
                ". . . .");
        System.out.println(s);
        Assert.assertEquals("1.0111 OK\n> ", s);
    }

    @Test
    public void TestBitsBig()
    {
        String s = check ("2 77 pow dup toBits toBig",
                ". sp .");
        System.out.println(s);
        Assert.assertEquals("151115727451828646838272 151115727451828646838272 OK\n> ", s);
    }

}
