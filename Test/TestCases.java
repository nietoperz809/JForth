import jforth.JForth;
import org.junit.Assert;
import org.junit.Test;
import tools.StringStream;

import java.util.Arrays;

import static jforth.PolynomParser.parsePolynom;

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

    @Test
    public void TestPolyMult()
    {
        String s = check ("x^2+x x^2+x *",
                ".");
        System.out.println(s);
        Assert.assertEquals("x^2+2x^3+x^4 OK\n> ", s);
    }

    @Test
    public void TestPolyDiv()
    {
        String s = check ("4x^5+3x^2 x^2-6 1 pick 1 pick / ",
                ". .\" +(\" mod . .\" )\"");
        System.out.println(s);
        Assert.assertEquals("3+24x+4x^3+(18+144x) OK\n> ", s);
    }

    @Test
    public void TestPolyDiv2()
    {
        String s = check ("-13x^7+3x^5 x^2-6 /mod ",
                ". sp .\" rest:\" .");
        System.out.println(s);
        Assert.assertEquals("-450x-75x^3-13x^5 rest:1-2700x OK\n> ", s);
    }

    @Test
    public void TestSimpleCalc()
    {
        String s = check ("12.0 7 / 100L * type ",
                ". sp .\" - \" .");
        System.out.println(s);
        Assert.assertEquals("BigInt - 100 OK\n> ", s);
    }

    @Test
    public void TestTrig()
    {
        String s = check ("10 dup dup",
                "sin . sp cos . sp tan .");
        System.out.println(s);
        Assert.assertEquals("-0.5440211108893698 -0.8390715290764524 0.6483608274590866 OK\n> ", s);
    }

    @Test
    public void TestPolyParser()
    {
        double[] poly = parsePolynom("8x^4+10.7+34x^2-7x+7x-9x^4");
        String s = Arrays.toString(poly);
        Assert.assertEquals("[10.7, 0.0, 34.0, 0.0, -1.0]", s);
    }

    @Test
    public void TestHex1()
    {
        String s = check ("hex 0a 14 *",
                ".");
        System.out.println(s);
        Assert.assertEquals("C8 OK\n> ", s);
    }

    @Test
    public void TestDefinedOp()
    {
        String s = check (": *+ * + ;",
                "5 6 7 *+ .");
        System.out.println(s);
        Assert.assertEquals("47 OK\n> ", s);
    }

    @Test
    public void TestVariable()
    {
        String s = check ("variable x " +
                        "3 x !",
                "x @ .");
        System.out.println(s);
        Assert.assertEquals("3 OK\n> ", s);
    }

    @Test
    public void TestConstant()
    {
        String s = check ("4711 constant bla",
                "bla .");
        System.out.println(s);
        Assert.assertEquals("4711 OK\n> ", s);
    }

    @Test
    public void TestTuck()
    {
        String s = check ("1 2 3 4 5 6 tuck",
                ". . . . . . .");
        System.out.println(s);
        Assert.assertEquals("6564321 OK\n> ", s);
    }

    @Test
    public void TestIf()
    {
        String s = check (": konto dup abs . 0< if \"soll\" . else \"haben\" . then ;",
                "0 konto -1 konto");
        System.out.println(s);
        Assert.assertEquals("0haben1soll OK\n> ", s);
    }

    @Test
    public void TestRecurse()
    {
        String s = check (": rtest dup 0< if sp \"stop\" . else 1 - dup sp . recurse then ;",
                "6 rtest");
        System.out.println(s);
        Assert.assertEquals(" 5 4 3 2 1 0 -1 stop OK\n> ", s);
    }

    @Test
    public void TestBeginUntilCompiled()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 begin dup . 1+ dup 5 = until ;",
                "test");
        System.out.println(s);
        Assert.assertEquals("01234 OK\n> ", s);
    }

    @Test
    public void TestBeginAgainCompiled()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 1 2 3 4 5 begin . again ;",
                "test");
        System.out.println(s);
        Assert.assertEquals("543210test word execution or stack error\n> ", s);
    }

    @Test
    public void TestBeginAgainIfThen()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 1 2 3 4 5 6 7 8 9 10 begin . dup 5 < if \"-\" . then again ;",
                "test");
        System.out.println(s);
        Assert.assertEquals("1098765-4-3-2-1-0test word execution or stack error\n> ", s);
    }

    @Test
    public void TestBeginAgainBreak()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 1 2 3 4 5 6 7 8 9 10 begin . dup 5 < if break then again ;",
                "test");
        System.out.println(s);
        Assert.assertEquals("1098765 OK\n> ", s);
    }

    @Test
    public void TestTwoWords()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 1 2 3 4 5 6 7 8 9 10 begin . dup 5 < if break then again ; " +
                        ": fump 10 spaces \"lala\" . ; " +
                        ": check test fump ;",
                    "check");
        System.out.println(s);
        Assert.assertEquals("1098765          lala OK\n> ", s);
    }

    @Test
    public void TestAddFractions()
    {
        String s = check ("1/2 1/8 1/32 1/128",
                "+ + + .");
        System.out.println(s);
        Assert.assertEquals("85/128 OK\n> ", s);
    }

    @Test
    public void TestStringOpAddMult()
    {
        String s = check ("\"lala\" \"dumm\"",
                "+ 2 * .");
        System.out.println(s);
        Assert.assertEquals("laladummlaladumm OK\n> ", s);
    }

    @Test
    public void TestDSCreate()
    {
        String s = check ("{1.2,2.3,3,4,4.5} type",
                ". .");
        System.out.println(s);
        Assert.assertEquals("DoubleSequence{1.2,2.3,3,4,4.5} OK\n> ", s);
    }

    @Test
    public void TestMToList()
    {
        String s = check ("{{1,2,3}{4,5}{6,7,8,9}} toList",
                ". . .");
        System.out.println(s);
        Assert.assertEquals("{6,7,8,9}{4,5,0,0}{1,2,3,0} OK\n> ", s);
    }

    @Test
    public void TestToMatrix()
    {
        String s = check ("{1,2,3} {4,5,6,7,8,9} {3,4,55,7,99} toMatrix",
                ".");
        System.out.println(s);
        Assert.assertEquals("{{3,4,55,7,99,0}{4,5,6,7,8,9}{1,2,3,0,0,0}} OK\n> ", s);
    }

}
