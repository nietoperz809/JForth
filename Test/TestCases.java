import jforth.JForth;
import jforth.Utilities;
import org.junit.Assert;
import org.junit.Test;
import tools.StringStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import static jforth.PolynomialParser.parsePolynomial;

/**
 * Created by Administrator on 4/15/2017.
 */
public class TestCases
{
    private static final String EP = " OK\nJFORTH> ";

    private String check (String prg, String call)
    {
        StringStream _ss = new StringStream();
        JForth _forth = new JForth(_ss.getPrintStream());
//        _forth.setPrintStream(_ss.getPrintStream());
        _forth.singleShot(prg);
        if (call != null)
        {
            _ss.clear();
            _forth.singleShot(call);
        }
        return _ss.toString();
    }

    @Test
    public void TestConversion()
    {
        String s = check ("hex a0 dec", ".");
        System.out.println(s);
        Assert.assertEquals("160"+EP, s);
        s = check ("dec 65535 hex", ".");
        System.out.println(s);
        Assert.assertEquals("FFFF"+EP, s);
        s = check ("bin 10101010 hex", ".");
        System.out.println(s);
        Assert.assertEquals("AA"+EP, s);
        s = check ("hex 73 bin", ".");
        System.out.println(s);
        Assert.assertEquals("1110011"+EP, s);
        s = check ("3 setbase 10 10 20 + +", ".");
        System.out.println(s);
        Assert.assertEquals("110"+EP, s);
    }

    @Test
    public void TestCrossProduct()
    {
        String s = check ("{1,2,3} {4,5,6} crossP", ".");
        System.out.println(s);
        Assert.assertEquals("{-3,6,-3}"+EP, s);
    }

    @Test
    public void TestRoll()
    {
        String s = check ("1 2 3 4 2 roll", ". . . .");
        System.out.println(s);
        Assert.assertEquals("2431"+EP, s);
    }

    @Test
    public void TestDotProduct()
    {
        String s = check ("{1,2,3} {4,5,6} dotP", ".");
        System.out.println(s);
        Assert.assertEquals("32"+EP, s);
    }

    @Test
    public void TestImmediate()
    {
        String s = check ("10 0 do i .", "loop");
        System.out.println(s);
        Assert.assertEquals("0123456789"+EP, s);
    }

    @Test
    public void TestPlusLoop()
    {
        String s = check (": test 10 0 do i . 2 +loop 10 0 do i . 3 +loop ;", "test");
        System.out.println(s);
        Assert.assertEquals("024680369"+EP, s);
    }


    @Test
    public void TestPrg2()
    {
        String s = check (": test 10 0 do i . loop ;", "test");
        System.out.println(s);
        Assert.assertEquals("0123456789"+EP, s);
    }

    @Test
    public void TestPrg3()
    {
        String s = check (": test variable hello hello ! hello @ length hello @ ;",
                "{1,2,3,4} test . .");
        System.out.println(s);
        Assert.assertEquals("{1,2,3,4}4"+EP, s);
    }

    @Test
    public void TestPrg4()
    {
        String s = check (": test variable hello hello ! hello @ length fact ;",
                "{1,2,3,4} test .");
        System.out.println(s);
        Assert.assertEquals("24"+EP, s);
    }

    @Test
    public void TestPrg5()
    {
        String s = check (": test variable hello hello ! hello @ length fact 0 ;",
                "{1,2,3,4} test . .");
        System.out.println(s);
        Assert.assertEquals("024"+EP, s);
    }

    @Test
    public void TestPrg6()
    {
        String s = check (": test variable hello hello ! hello @ length 24 0 do i . loop ;",
                "{1,2,3,4} test .");
        System.out.println(s);
        Assert.assertEquals("012345678910111213141516171819202122234"+EP, s);
    }

    @Test
    public void TestPrg7()
    {
        String s = check (": test variable hello hello ! hello @ length fact 0 do i . loop ;",
                "{1,2,3} test");
        System.out.println(s);
        Assert.assertEquals("012345"+EP, s);
    }

    @Test
    public void TestPrg_Permute()
    {
        String result = "1 -JFORTH> {1,4,3,2}\r\n" +
                "2 -JFORTH> {2,1,3,4}\r\n" +
                "3 -JFORTH> {3,1,4,2}\r\n" +
                "4 -JFORTH> {4,1,3,2}\r\n" +
                "5 -JFORTH> {1,2,4,3}\r\n" +
                "6 -JFORTH> {2,4,1,3}\r\n" +
                "7 -JFORTH> {3,2,1,4}\r\n" +
                "8 -JFORTH> {4,2,1,3}\r\n" +
                "9 -JFORTH> {1,3,4,2}\r\n" +
                "10 -JFORTH> {2,3,1,4}\r\n" +
                "11 -JFORTH> {3,4,1,2}\r\n" +
                "12 -JFORTH> {4,3,1,2}\r\n" +
                "13 -JFORTH> {1,4,2,3}\r\n" +
                "14 -JFORTH> {2,1,4,3}\r\n" +
                "15 -JFORTH> {3,1,2,4}\r\n" +
                "16 -JFORTH> {4,1,2,3}\r\n" +
                "17 -JFORTH> {1,2,3,4}\r\n" +
                "18 -JFORTH> {2,4,3,1}\r\n" +
                "19 -JFORTH> {3,2,4,1}\r\n" +
                "20 -JFORTH> {4,2,3,1}\r\n" +
                "21 -JFORTH> {1,3,2,4}\r\n" +
                "22 -JFORTH> {2,3,4,1}\r\n" +
                "23 -JFORTH> {3,4,2,1}\r\n" +
                "24 -JFORTH> {4,3,2,1}\r\n" +
                " OK\nJFORTH> ";
        String s = check (": test variable hello hello ! " +
                        "hello @ length fact 0 do i 1 + . " +
                        "sp \"-JFORTH>\" . sp hello @ i permute . cr loop ;\n",
                "{1,2,3,4} test");
        System.out.println(s);
        Assert.assertEquals(result, s);
    }

    @Test
    public void TestStringRev()
    {
        String s = check ("\"hello\" rev toStr",
                ".");
        System.out.println(s);
        Assert.assertEquals("olleh"+EP, s);
    }

    @Test
    public void TestSortString  ()
    {
        String s = check ("\"thequickbrownfoxjumpsoverthelazydog\" sort unique toStr",
                ".");
        System.out.println(s);
        Assert.assertEquals("abcdefghijklmnopqrstuvwxyz"+EP, s);
    }

    @Test
    public void TestPrimFac  ()
    {
        String s = check ("{2,3,5,7,11,13,17,19,23} prod factor 8 factor",
                ". .");
        System.out.println(s);
        Assert.assertEquals("{2,2,2}{2,3,5,7,11,13,17,19,23}"+EP, s);
    }

    @Test
    public void TestSub()
    {
        String s = check ("12 11 - 12L 11 - 12L 11.0 - 12.0 11 -",
                ". . . .");
        System.out.println(s);
        Assert.assertEquals("1111"+EP, s);
    }

    @Test
    public void TestBitsBig()
    {
        String s = check ("2 77 pow dup toBits toBig",
                ". sp .");
        System.out.println(s);
        Assert.assertEquals("151115727451828646838272 151115727451828646838272"+EP, s);
    }

    @Test
    public void TestPolyMult()
    {
        String s = check ("x^2+x x^2+x *",
                ".");
        System.out.println(s);
        Assert.assertEquals("x^2+2x^3+x^4"+EP, s);
    }

    @Test
    public void TestPolyDiv()
    {
        String s = check ("4x^5+3x^2 x^2-6 1 pick 1 pick / ",
                ". .\" +(\" mod . .\" )\"");
        System.out.println(s);
        Assert.assertEquals("3+24x+4x^3+(18+144x)"+EP, s);
    }

    @Test
    public void TestPolyDiv2()
    {
        String s = check ("-13x^7+3x^5 x^2-6 /mod ",
                ". sp .\" rest:\" .");
        System.out.println(s);
        Assert.assertEquals("-450x-75x^3-13x^5 rest:1-2700x"+EP, s);
    }

    @Test
    public void TestSimpleCalc()
    {
        String s = check ("12.0 7 / 100L * dup type ",
                ". sp .\" - \" .");
        System.out.println(s);
        Assert.assertEquals("BigInt - 100"+EP, s);
    }

    @Test
    public void TestTrig()
    {
        String s = check ("10 dup dup",
                "sin . sp cos . sp tan .");
        System.out.println(s);
        Assert.assertEquals("-0.5440211108893698 -0.8390715290764524 0.6483608274590866"+EP, s);
    }

    @Test
    public void TestPolyParser()
    {
        double[] poly = parsePolynomial("8x^4+10.7+34x^2-7x+7x-9x^4", 10);
        String s = Arrays.toString(poly);
        Assert.assertEquals("[10.7, 0.0, 34.0, 0.0, -1.0]", s);
    }

    @Test
    public void TestHex1()
    {
        String s = check ("hex 0a 14 *",
                ".");
        System.out.println(s);
        Assert.assertEquals("C8"+EP, s);
    }

    @Test
    public void TestDefinedOp()
    {
        String s = check (": *+ * + ;",
                "5 6 7 *+ .");
        System.out.println(s);
        Assert.assertEquals("47"+EP, s);
    }

    @Test
    public void TestVariable()
    {
        String s = check ("variable x " +
                        "3 x !",
                "x @ .");
        System.out.println(s);
        Assert.assertEquals("3"+EP, s);
    }

    @Test
    public void TestConstant()
    {
        String s = check ("4711 constant bla",
                "bla .");
        System.out.println(s);
        Assert.assertEquals("4711"+EP, s);
    }

    @Test
    public void TestTuck()
    {
        String s = check ("1 2 3 4 5 6 tuck",
                ". . . . . . .");
        System.out.println(s);
        Assert.assertEquals("6564321"+EP, s);
    }

    @Test
    public void TestIf()
    {
        String s = check (": konto dup abs . 0< if \"soll\" . else \"haben\" . then ;",
                "0 konto -1 konto");
        System.out.println(s);
        Assert.assertEquals("0haben1soll"+EP, s);
    }

    @Test
    public void TestRecurse()
    {
        String s = check (": rtest dup 0< if sp \"stop\" . else 1 - dup sp . recurse then ;",
                "6 rtest");
        System.out.println(s);
        Assert.assertEquals(" 5 4 3 2 1 0 -1 stop"+EP, s);
    }

    @Test
    public void TestBeginUntilCompiled()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 begin dup . 1+ dup 5 = until ;",
                "test");
        System.out.println(s);
        Assert.assertEquals("01234"+EP, s);
    }

    @Test
    public void TestBeginUntilImmediate()
    {
        String s = check ("0 begin dup . 6 + dup 99 >",
                "until");
        System.out.println(s);
        Assert.assertEquals("06121824303642485460667278849096"+EP, s);
    }

    @Test
    public void TestBeginAgainCompiled()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 1 2 3 4 5 begin . again ;",
                "test");
        System.out.println(s);
        Assert.assertEquals("543210test word execution or stack error\nJFORTH> ", s);
    }

    @Test
    public void TestBeginAgainImmediate()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check ("0 1 2 3 4 5 begin .",
                "again");
        System.out.println(s);
        Assert.assertEquals("543210"+EP, s);
    }

    @Test
    public void TestBeginAgainIfThen()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 1 2 3 4 5 6 7 8 9 10 begin . dup 5 < if \"-\" . then again ;",
                "test");
        System.out.println(s);
        Assert.assertEquals("1098765-4-3-2-1-0test word execution or stack error\nJFORTH> ", s);
    }

    @Test
    public void TestBeginAgainBreak()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 1 2 3 4 5 6 7 8 9 10 begin . dup 5 < if break then again ;",
                "test");
        System.out.println(s);
        Assert.assertEquals("1098765"+EP, s);
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
        Assert.assertEquals("1098765          lala"+EP, s);
    }

    @Test
    public void TestAddFractions()
    {
        String s = check ("1/2 1/8 1/32 1/128",
                "+ + + .");
        System.out.println(s);
        Assert.assertEquals("85/128"+EP, s);
    }

    @Test
    public void TestStringOpAddMult()
    {
        String s = check ("\"lala\" \"dumm\"",
                "+ 2 * .");
        System.out.println(s);
        Assert.assertEquals("laladummlaladumm"+EP, s);
    }

    @Test
    public void TestDSCreate()
    {
        String s = check ("{1.2,2.3,3,4,4.5} type",
                ".s");
        System.out.println(s);
        Assert.assertEquals("DoubleSequence "+EP, s);
    }

    @Test
    public void TestMToList()
    {
        String s = check ("{{1,2,3}{4,5}{6,7,8,9}} toList",
                ". . .");
        System.out.println(s);
        Assert.assertEquals("{6,7,8,9}{4,5,0,0}{1,2,3,0}"+EP, s);
    }

    @Test
    public void TestToMatrix()
    {
        String s = check ("{1,2,3} {4,5,6,7,8,9} {3,4,55,7,99} toM",
                ".");
        System.out.println(s);
        Assert.assertEquals("{{3,4,55,7,99,0}{4,5,6,7,8,9}{1,2,3,0,0,0}}"+EP, s);
    }

    @Test
    public void TestDeterminant()
    {
        String s = check ("{{1,2,3}{4,5,6}{7,8,1}} detM 3 round",
                ".");
        System.out.println(s);
        Assert.assertEquals("24"+EP, s);
    }

    @Test
    public void TestDecompLUP()
    {
        String s = check (" {{1,2,3}{4,5,6}{7,8,1}} lupM",
                ". . .");
        System.out.println(s);
        Assert.assertEquals("{{0,0,1}{1,0,0}{0,1,0}}{{7,8,1}{0,0.8571,2.8571}{0,0,4}}{{1,0,0}{0.1429,1,0}{0.5714,0.5,1}}"+EP, s);
    }

    @Test
    public void TestFitpolyRound()
    {
        String s = check ("{1,1,2,4,3,9} fitPoly 3 round",
                ".");
        System.out.println(s);
        Assert.assertEquals("x^2"+EP, s);
    }

    @Test
    public void TestlagPoly()
    {
        String s = check ("{1,2,2,4,3,9} lagPoly",
                ".");
        System.out.println(s);
        Assert.assertEquals("3-2.5x+1.5x^2"+EP, s);
    }

    @Test
    public void TestMix()
    {
        String s = check ("\"peter\" \"doof\" mix toStr",
                ".");
        System.out.println(s);
        Assert.assertEquals("pdeotoefr"+EP, s);
    }

    @Test
    public void TestPowDouble()
    {
        String s = check ("0.5 10 pow 4 round",
                ".");
        System.out.println(s);
        Assert.assertEquals("0.001"+EP, s);
    }

    @Test
    public void TestHexXor()
    {
        String s = check ("hex 1fff 1 xor",
                ".");
        System.out.println(s);
        Assert.assertEquals("1FFE"+EP, s);
    }

    @Test
    public void TestEval()
    {
        String s = check ("\"text='';for(i=0;i<10;i++)text+=3*i+'-';\" js",
                ".");
        System.out.println(s);
        Assert.assertEquals("0-3-6-9-12-15-18-21-24-27-"+EP, s);
    }

    @Test
    public void TestEval2()
    {
        String s = check ("\"str='Visit_W3Schools';n=str.search(/w3schools/i);\" js",
                ".");
        System.out.println(s);
        Assert.assertEquals("6"+EP, s);
    }

    @Test
    public void TestRWFile()
    {
        check ("\"lala\" openWriter \"hallo\" writeString \"_doof\" writeString closeWriter",
                ".");
        String s = check ("\"lala\" openReader readLine swap readLine rot +",
                ".");
        Assert.assertEquals("*EOF*hallo_doof"+EP, s);
    }

    @Test
    public void TestZeta()
    {
        String s = check ("-1 zeta toFraction",
                ".");
        Assert.assertEquals("-1/12"+EP, s);
    }

    @Test
    public void TestCopySamExe()
    {
        try
        {
            String res = Utilities.extractResource("sam.exe");
            System.out.println(res);
            Process process = new ProcessBuilder(res).start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null)
            {
                System.out.println(line);
            }
        }
        catch (IOException e)
        {
            Assert.fail();
        }
    }

    @Test
    public void Test888to6()
    {
        String s = check ("8 8 + sqrt fact 8 / fact",
                ".");
        Assert.assertEquals("6"+EP, s);
    }

    @Test
    public void TestHexStr()
    {
        String s = check ("{1,2,3,100,200,255} hexStr",
                ".");
        Assert.assertEquals("01020364C8FF"+EP, s);
    }

    @Test
    public void TestUnhexStr()
    {
        String s = check ("\"01020364C8FF\" unhexStr",
                ".");
        Assert.assertEquals("{1,2,3,100,200,255}"+EP, s);
    }

    @Test
    public void TestHash()
    {
        String s = check ("\"Hallo\" \"SHA-1\" hash hexStr",
                ".");
        Assert.assertEquals("59D9A6DF06B9F610F7DB8E036896ED03662D168F"+EP, s);
    }

    @Test
    public void TestUrlEncSpace()
    {
        String s = check ("\"lala\" {32} toStr \"dumm\" + + urlEnc",
                ".");
        Assert.assertEquals("lala+dumm"+EP, s);
    }

    @Test
    public void TestCollatz()
    {
        String s = check ("11 clltz dup length swap sum",". .");
        Assert.assertEquals("25915"+EP, s);
    }

    @Test
    public void TestUDP()
    {
        
        (new Thread(() ->
        {

            try
            {
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            String s = check ("1000 \"hello\" udpput","");
            Assert.assertEquals(""+EP, s);
        })).start();

        String s = check ("1000 udpget",".");
        Assert.assertEquals("hello"+EP, s);
    }

    @Test
    public void TestDlist()
    {
        String s = check ("1234567890L toDList",".");
        Assert.assertEquals("{1,2,3,4,5,6,7,8,9,0}"+EP, s);
    }

    @Test
    public void TestDAltsum()
    {
        String s = check ("3229380809664349823849028340088048L toDList altsum",".");
        Assert.assertEquals("33"+EP, s);
    }

    @Test
    public void TestJava()
    {
        final String source =
                "\"public static double main(double arg) {return func(arg);}" +
                        "public static double func(double arg) {return Math.sqrt(arg);}"+
                        "\"";
        String s = check ("56.25 "+source + " java",".");
        Assert.assertEquals("7.5"+EP, s);
    }

    @Test
    public void TestDotQuote()
    {
        String s = check (".\" hello doof \"",null);
        Assert.assertEquals("hello doof "+EP, s);
    }

    @Test
    public void TestMultiDots()
    {
        String s = check ("1 2 3 4 5",".....");
        Assert.assertEquals("54321"+EP, s);
    }

    @Test
    public void TestShift()
    {
        String s = check ("\"peter\" >> >> >>",".");
        Assert.assertEquals("terpe"+EP, s);
        s = check ("\"peter\" << >> <<",".");
        Assert.assertEquals("eterp"+EP, s);
    }

    @Test
    public void TestScatterColl()
    {
        String s = check ("{1,2,3,4,5,6} scatter",".s");
        Assert.assertEquals("1 2 3 4 5 6 "+EP, s);
        s = check ("1 2 3 4 5 6 collect",".");
        Assert.assertEquals("{1,2,3,4,5,6}"+EP, s);
    }

}
