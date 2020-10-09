import jforth.JForth;
import jforth.RuntimeEnvironment;
import org.junit.Assert;
import org.junit.Test;
import tools.ResourceLoader;
import tools.StringStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

import static jforth.PolynomialParser.parsePolynomial;

/**
 * Created by Administrator on 4/15/2017.
 */
public class TestCases
{
    private String check (String prg, String call)
    {
        StringStream _ss = new StringStream();
        JForth _forth = new JForth(_ss.getPrintStream(), RuntimeEnvironment.TEST);
//        _forth.setPrintStream(_ss.getPrintStream());
        _forth.singleShot(prg);
        if (call != null)
        {
            _ss.clear();
            _forth.singleShot(call);
        }
        return _ss.toString();
    }
    
    private void shoudBeOK (String a, String s)
    {
        final String EP = " OK\nJFORTH> ";
        Assert.assertEquals(a+EP, s);
    }

    private void shoudBe (String a, String s)
    {
        Assert.assertEquals(a, s);
    }

    @Test
    public void TestConversion()
    {
        String s = check ("hex a0 dec", ".");
        System.out.println(s);
        shoudBeOK ("160" ,s);
        s = check ("dec 65535 hex", ".");
        System.out.println(s);
        shoudBeOK ("FFFF" ,s);
        s = check ("bin 10101010 hex", ".");
        System.out.println(s);
        shoudBeOK ("AA" ,s);
        s = check ("hex 73 bin", ".");
        System.out.println(s);
        shoudBeOK ("1110011" ,s);
        s = check ("3 setbase 10 10 20 + +", ".");
        System.out.println(s);
        shoudBeOK ("110" ,s);
    }

    @Test
    public void TestCrossProduct()
    {
        String s = check ("{1,2,3} {4,5,6} crossP", ".");
        System.out.println(s);
        shoudBeOK ("{-3,6,-3}" ,s);
    }

    @Test
    public void TestRoll()
    {
        String s = check ("1 2 3 4 2 roll", ". . . .");
        System.out.println(s);
        shoudBeOK ("2431" ,s);
    }

    @Test
    public void TestDotProduct()
    {
        String s = check ("{1,2,3} {4,5,6} dotP", ".");
        System.out.println(s);
        shoudBeOK ("32" ,s);
    }

    @Test
    public void TestImmediate()
    {
        String s = check ("10 0 do i .", "loop");
        System.out.println(s);
        shoudBeOK ("0123456789" ,s);
    }

    @Test
    public void TestPlusLoop()
    {
        String s = check (": test 10 0 do i . 2 +loop 10 0 do i . 3 +loop ;", "test");
        System.out.println(s);
        shoudBeOK ("024680369" ,s);
    }


    @Test
    public void TestPrg2()
    {
        String s = check (": test 10 0 do i . loop ;", "test");
        System.out.println(s);
        shoudBeOK ("0123456789" ,s);
    }

    @Test
    public void TestPrg3()
    {
        String s = check (": test variable hello hello ! hello @ length hello @ ;",
                "{1,2,3,4} test . .");
        System.out.println(s);
        shoudBeOK ("{1,2,3,4}4" ,s);
    }

    @Test
    public void TestPrg4()
    {
        String s = check (": test variable hello hello ! hello @ length fact ;",
                "{1,2,3,4} test .");
        System.out.println(s);
        shoudBeOK ("24" ,s);
    }

    @Test
    public void TestPrg5()
    {
        String s = check (": test variable hello hello ! hello @ length fact 0 ;",
                "{1,2,3,4} test . .");
        System.out.println(s);
        shoudBeOK ("024" ,s);
    }

    @Test
    public void TestPrg6()
    {
        String s = check (": test variable hello hello ! hello @ length 24 0 do i . loop ;",
                "{1,2,3,4} test .");
        System.out.println(s);
        shoudBeOK ("012345678910111213141516171819202122234" ,s);
    }

    @Test
    public void TestPrg7()
    {
        String s = check (": test variable hello hello ! hello @ length fact 0 do i . loop ;",
                "{1,2,3} test");
        System.out.println(s);
        shoudBeOK ("012345" ,s);
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
        shoudBe (result, s);
    }

    @Test
    public void TestStringRev()
    {
        String s = check ("\"hello\" rev",
                ".");
        System.out.println(s);
        shoudBeOK ("olleh" ,s);
    }

    @Test
    public void TestSortString  ()
    {
        String s = check ("\"laladummthequickbrownfoxjumpsoverthelazydog\" sort unique toStr",
                ".");
        System.out.println(s);
        shoudBeOK ("abcdefghijklmnopqrstuvwxyz" ,s);
    }

    @Test
    public void TestPrimFac  ()
    {
        String s = check ("{2,3,5,7,11,13,17,19,23} prod factor 8 factor",
                ". .");
        System.out.println(s);
        shoudBeOK ("{2,2,2}{2,3,5,7,11,13,17,19,23}" ,s);
    }

    @Test
    public void TestSub()
    {
        String s = check ("12 11 - 12L 11 - 12L 11.0 - 12.0 11 -",
                ". . . .");
        System.out.println(s);
        shoudBeOK ("1111" ,s);
    }

    @Test
    public void TestBitsBig()
    {
        String s = check ("2 77 pow dup toBits toBig",
                ". sp .");
        System.out.println(s);
        shoudBeOK ("151115727451828646838272 151115727451828646838272" ,s);
    }

    @Test
    public void TestPolyMult()
    {
        String s = check ("x^2+x x^2+x *",
                ".");
        System.out.println(s);
        shoudBeOK ("x^2+2x^3+x^4" ,s);
    }

    @Test
    public void TestPolyDiv()
    {
        String s = check ("4x^5+3x^2 x^2-6 1 pick 1 pick / ",
                ". .\" +(\" mod . .\" )\"");
        System.out.println(s);
        shoudBeOK ("3+24x+4x^3+(18+144x)" ,s);
    }

    @Test
    public void TestPolyDiv2()
    {
        String s = check ("-13x^7+3x^5 x^2-6 /mod ",
                ". sp .\" rest:\" .");
        System.out.println(s);
        shoudBeOK ("-450x-75x^3-13x^5 rest:1-2700x" ,s);
    }

    @Test
    public void TestSimpleCalc()
    {
        String s = check ("12.0 7 / 100L * dup type ",
                ". sp .\" - \" .");
        System.out.println(s);
        shoudBeOK ("BigInteger - 100" ,s);
    }

    @Test
    public void TestTrig()
    {
        String s = check ("10 dup dup",
                "sin . sp cos . sp tan .");
        System.out.println(s);
        shoudBeOK ("-0.5440211108893698 -0.8390715290764524 0.6483608274590866" ,s);
    }

    @Test
    public void TestPolyParser()
    {
        double[] poly = parsePolynomial("8x^4+10.7+34x^2-7x+7x-9x^4", 10);
        String s = Arrays.toString(poly);
        shoudBe ("[10.7, 0.0, 34.0, 0.0, -1.0]", s);
    }

    @Test
    public void TestHex1()
    {
        String s = check ("hex 0a 14 *",
                ".");
        System.out.println(s);
        shoudBeOK ("C8" ,s);
    }

    @Test
    public void TestDefinedOp()
    {
        String s = check (": *+ * + ;",
                "5 6 7 *+ .");
        System.out.println(s);
        shoudBeOK ("47" ,s);
    }

    @Test
    public void TestVariable()
    {
        String s = check ("variable x " +
                        "3 x !",
                "x @ .");
        System.out.println(s);
        shoudBeOK ("3" ,s);
    }

    @Test
    public void TestConstant()
    {
        String s = check ("4711 constant bla",
                "bla .");
        System.out.println(s);
        shoudBeOK ("4711" ,s);
    }

    @Test
    public void TestTuck()
    {
        String s = check ("1 2 3 4 5 6 tuck",
                ". . . . . . .");
        System.out.println(s);
        shoudBeOK ("6564321" ,s);
    }

    @Test
    public void TestIf()
    {
        String s = check (": konto dup abs . 0< if \"soll\" . else \"haben\" . then ;",
                "0 konto -1 konto");
        System.out.println(s);
        shoudBeOK ("0haben1soll" ,s);
    }

    @Test
    public void TestRecurse()
    {
        String s = check (": rtest dup 0< if sp \"stop\" . else 1 - dup sp . recurse then ;",
                "6 rtest");
        System.out.println(s);
        shoudBeOK (" 5 4 3 2 1 0 -1 stop" ,s);
    }

    @Test
    public void TestBeginUntilCompiled()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 begin dup . 1+ dup 5 = until ;",
                "test");
        System.out.println(s);
        shoudBeOK ("01234" ,s);
    }

    @Test
    public void TestBeginUntilImmediate()
    {
        String s = check ("0 begin dup . 6 + dup 99 >",
                "until");
        System.out.println(s);
        shoudBeOK ("06121824303642485460667278849096" ,s);
    }

    @Test
    public void TestBeginAgainCompiled()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 1 2 3 4 5 begin . again ;",
                "test");
        System.out.println(s);
        shoudBe ("543210test word execution or stack error\nJFORTH> ", s);
    }

    @Test
    public void TestBeginAgainImmediate()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check ("0 1 2 3 4 5 begin .",
                "again");
        System.out.println(s);
        shoudBeOK ("543210" ,s);
    }

    @Test
    public void TestBeginAgainIfThen()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 1 2 3 4 5 6 7 8 9 10 begin . dup 5 < if \"-\" . then again ;",
                "test");
        System.out.println(s);
        shoudBe ("1098765-4-3-2-1-0test word execution or stack error\nJFORTH> ", s);
    }

    @Test
    public void TestBeginAgainBreak()
    {
        // : test 0 begin dup . 1+ again ;
        String s = check (": test 0 1 2 3 4 5 6 7 8 9 10 begin . dup 5 < if break then again ;",
                "test");
        System.out.println(s);
        shoudBeOK ("1098765" ,s);
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
        shoudBeOK ("1098765          lala" ,s);
    }

    @Test
    public void TestNested()
    {
        String s = check (": NESTED cr 0 do dup  0 do cr j . i . loop loop drop ;",
                "3 4 NESTED");
        System.out.println(s);
        shoudBeOK ("\r\n\r\n00\r\n01\r\n02\r\n10\r\n11\r\n12\r\n20\r\n21\r\n22\r\n30\r\n31\r\n32" ,s);
    }


    @Test
    public void TestAddFractions()
    {
        String s = check ("1/2 1/8 1/32 1/128",
                "+ + + .");
        System.out.println(s);
        shoudBeOK ("85/128" ,s);
    }

    @Test
    public void TestStringOpAddMult()
    {
        String s = check ("\"lala\" \"dumm\"",
                "+ 2 * .");
        System.out.println(s);
        shoudBeOK ("laladummlaladumm" ,s);
    }

    @Test
    public void TestDSCreate()
    {
        String s = check ("{1.2,2.3,3,4,4.5} type",
                ".s");
        System.out.println(s);
        shoudBeOK ("DoubleSequence " ,s);
    }

    @Test
    public void TestMatrixToList()
    {
        String s = check ("{{1,2,3}{4,5}{6,7,8,9}} toNumList",
                ". . .");
        System.out.println(s);
        shoudBeOK ("{6,7,8,9}{4,5,0,0}{1,2,3,0}" ,s);
    }

    @Test
    public void TestToMatrix()
    {
        String s = check ("{1,2,3} {4,5,6,7,8,9} {3,4,55,7,99} toM",
                ".");
        System.out.println(s);
        shoudBeOK ("{{3,4,55,7,99,0}{4,5,6,7,8,9}{1,2,3,0,0,0}}" ,s);
    }

    @Test
    public void TestDeterminant()
    {
        String s = check ("{{1,2,3}{4,5,6}{7,8,1}} detM 3 round",
                ".");
        System.out.println(s);
        shoudBeOK ("24" ,s);
    }

    @Test
    public void TestDecompLUP()
    {
        String s = check (" {{1,2,3}{4,5,6}{7,8,1}} lupM",
                ". . .");
        System.out.println(s);
        shoudBeOK ("{{0,0,1}{1,0,0}{0,1,0}}{{7,8,1}{0,0.8571,2.8571}{0,0,4}}{{1,0,0}{0.1429,1,0}{0.5714,0.5,1}}" ,s);
    }

    @Test
    public void TestFitpolyRound()
    {
        String s = check ("{1,1,2,4,3,9} fitPoly 3 round",
                ".");
        System.out.println(s);
        shoudBeOK ("x^2" ,s);
    }

    @Test
    public void TestlagPoly()
    {
        String s = check ("{1,2,2,4,3,9} lagPoly",
                ".");
        System.out.println(s);
        shoudBeOK ("3-2.5x+1.5x^2" ,s);
    }

    @Test
    public void TestMix()
    {
        String s = check ("\"peter\" \"doof\" mix toStr",
                ".");
        System.out.println(s);
        shoudBeOK ("pdeotoefr" ,s);
    }

    @Test
    public void TestPowDouble()
    {
        String s = check ("0.5 10 pow 4 round",
                ".");
        System.out.println(s);
        shoudBeOK ("0.001" ,s);
    }

    @Test
    public void TestHexXor()
    {
        String s = check ("hex 1fff 1 xor",
                ".");
        System.out.println(s);
        shoudBeOK ("1FFE" ,s);
    }

    @Test
    public void TestEval()
    {
        String s = check ("\"text='';for(i=0;i<10;i++)text+=3*i+'-';\" js",
                ".");
        System.out.println(s);
        shoudBeOK ("0-3-6-9-12-15-18-21-24-27-" ,s);
    }

    @Test
    public void TestEval2()
    {
        String s = check ("\"str='Visit_W3Schools';n=str.search(/w3schools/i);\" js",
                ".");
        System.out.println(s);
        shoudBeOK ("6" ,s);
    }

    @Test
    public void TestRWFile()
    {
        check ("\"lala\" openWriter \"hallo\" writeString \"_doof\" writeString closeWriter",
                ".");
        String s = check ("\"lala\" openReader readLine swap readLine rot +",
                ".");
        shoudBeOK ("*EOF*hallo_doof" ,s);
    }

    @Test
    public void TestZeta()
    {
        String s = check ("-1 zeta toFraction",
                ".");
        shoudBeOK ("-1/12" ,s);
    }

    @Test
    public void TestCopySamExe()
    {
        try
        {
            String res = ResourceLoader.extractResource("sam.exe", true);
            ProcessBuilder pb = new ProcessBuilder(res);
            Process p = pb.start();

            Thread.sleep (3000);

            BufferedReader lineReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            lineReader.lines().forEach(System.out::println);

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            errorReader.lines().forEach(System.out::println);
        }
        catch (Exception e)
        {
            Assert.fail();
        }
    }

    @Test
    public void Test888to6()
    {
        String s = check ("8 8 + sqrt fact 8 / fact",
                ".");
        shoudBeOK ("6" ,s);
    }

    @Test
    public void TestHexStr()
    {
        String s = check ("{1,2,3,100,200,255} hexStr",
                ".");
        shoudBeOK ("01020364C8FF" ,s);
    }

    @Test
    public void TestUnhexStr()
    {
        String s = check ("\"01020364C8FF\" unhexStr",
                ".");
        shoudBeOK ("{1,2,3,100,200,255}" ,s);
    }

    @Test
    public void TestHash()
    {
        String s = check ("\"Hallo\" \"SHA-1\" hash hexStr",
                ".");
        shoudBeOK ("59D9A6DF06B9F610F7DB8E036896ED03662D168F" ,s);
    }

    @Test
    public void TestCRC()
    {
        String s = check ("hallo dup crc16 hash swap crc32 hash _ swap",
                "...");
        shoudBeOK ("3111268817_1235" ,s);
    }

    @Test
    public void TestUrlEncSpace()
    {
        String s = check ("\"lala\" {32} toStr \"dumm\" + + urlEnc",
                ".");
        shoudBeOK ("lala+dumm" ,s);
    }

    @Test
    public void TestCollatz()
    {
        String s = check ("11 clltz dup length swap sum",". .");
        shoudBeOK ("25915" ,s);
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
            shoudBeOK ("" ,s);
        })).start();

        String s = check ("1000 udpget",".");
        shoudBeOK ("hello" ,s);
    }

    @Test
    public void TestDlist()
    {
        String s = check ("1234567890L toDList",".");
        shoudBeOK ("{1,2,3,4,5,6,7,8,9,0}" ,s);
    }

    @Test
    public void TestDAltsum()
    {
        String s = check ("3229380809664349823849028340088048L toDList altsum",".");
        shoudBeOK ("33" ,s);
    }

    @Test
    public void TestJava()
    {
        final String source =
                "\"public static double main(double arg) {return func(arg);}" +
                        "public static double func(double arg) {return Math.sqrt(arg);}"+
                        "\"";
        String s = check ("56.25 "+source + " java",".");
        shoudBeOK ("7.5" ,s);
    }

    @Test
    public void TestDotQuote()
    {
        String s = check (".\" hello doof \"",null);
        shoudBeOK ("hello doof " ,s);
    }

    @Test
    public void TestMultiDots()
    {
        String s = check ("1 2 3 4 5",".....");
        shoudBeOK ("54321" ,s);
    }

    @Test
    public void TestShift()
    {
        String s = check ("\"peter\" >> >> >>",".");
        shoudBeOK ("terpe" ,s);
        s = check ("\"peter\" << >> <<",".");
        shoudBeOK ("eterp" ,s);
    }

    @Test
    public void TestScatterColl()
    {
        String s = check ("{1,2,3,4,5,6} scatter",".s");
        shoudBeOK ("1 2 3 4 5 6 " ,s);
        s = check ("1 2 3 4 5 6 collect",".");
        shoudBeOK ("{1,2,3,4,5,6}" ,s);
    }

    @Test
    public void TestSeqGen()
    {
        String s = check ("3 6 9 seq",".");
        shoudBeOK ("{3,12,21,30,39,48}" ,s);
    }

    @Test
    public void TestIsPrime()
    {
        String s = check ("7919 isPrime 12 isPrime swap","..");
        shoudBeOK ("10" ,s);
    }

    @Test
    public void TestB64()
    {
        String s = check ("\"hoelle\" b64 dup unb64",". sp .");
        shoudBeOK ("hoelle aG9lbGxl" ,s);
    }

    @Test
    public void TestExecute()
    {
        String s = check ("' + 6 7 rot execute",".");
        shoudBeOK ("13" ,s);
    }

    @Test
    public void TestTimeView()
    {
        String s = check ("123456 toTime",".");
        shoudBeOK ("34:17:36" ,s);
        s = check ("34:17:36",".");
        shoudBeOK ("123456" ,s);
    }

    @Test
    public void TestNip()
    {
        String s = check ("a b c nip",".s");
        shoudBeOK ("a c " ,s);
    }

    @Test
    public void TestNonStandardRecurse()
    {
        String s = check (": facky recursive dup 1 > if dup 1- facky * then ;","6 facky .");
        shoudBeOK ("720" ,s);
    }

    @Test
    public void TestComplex2()
    {
        String s = check ("3+6i dup phi swap abs 3 round swap 3 round",
                ". sp .");
        shoudBeOK ("1.107 6.708" ,s);
    }

    @Test
    public void TestLn()
    {
        String s = check ("-1 ln 99 +",
                ".");
        shoudBeOK ("99+3.141592653589793i" ,s);
    }

    @Test
    public void TestFracRedux()
    {
        String s = check ("2/4 4/8 +",
                ".");
        shoudBeOK ("1" ,s);
    }

    @Test
    public void TestEulerIdentity()
    {
        String s = check ("0+1i pi * exp 3 round", ".");
        shoudBeOK ("-1" ,s);
    }

    @Test
    public void Test4PowI()
    {
        String s = check ("2 ln 1i * 2 * exp 4 1i pow =", ".");
        shoudBeOK ("1" ,s);
    }

    @Test
    public void TestStringMult()
    {
        String s = check ("la 2 * 3 ku * +", ".");
        shoudBeOK ("lalakukuku" ,s);
    }

    @Test
    public void TestCGroup()
    {
        String s = check ("7 11 cgroup", ".");
        shoudBeOK ("{7,5,2,3,10,4,6,9,8,1}" ,s);
    }

    @Test
    public void TestCGroupInverseAndEquality()
    {
        String s = check ("7 11 cgroup dup 11 igroup dup rot dup rot swap =", ". . .");
        shoudBeOK ("1{7,5,2,3,10,4,6,9,8,1}{8,9,6,4,10,3,2,5,7,1}" ,s);
    }

    @Test
    public void TestCTab()
    {
        String s = check ("5 ctab", ".");
        shoudBeOK ("{{1,2,3,4}{2,4,1,3}{3,1,4,2}{4,3,2,1}}" ,s);
    }

    @Test
    public void TestIntersectNlist()
    {
        String s = check ("{1,2,3,4} {1,2,4} intersect", ".");
        shoudBeOK ("{1,2,4}" ,s);
    }

    @Test
    public void TestIntersectSlist()
    {
        String s = check ("{a,b,c} {d,e,a} intersect", ".");
        shoudBeOK ("{\"a\"}" ,s);
    }

    @Test
    public void TestPick()
    {
        String s = check ("{peter,ist,lieb,oder,doof} 2 lpick", ".");
        shoudBeOK ("lieb" ,s);
    }

    @Test
    public void UniqTest()
    {
        String s = check ("{a,s,a,s,d,a,s} unique", ".");
        shoudBeOK ("{\"a\",\"s\",\"d\"}" ,s);
    }

    @Test
    public void TestRotateNumSeq()
    {
        String s = check ("{1,2,3} << {1,2,3} >>", "..");
        shoudBeOK ("{3,1,2}{2,3,1}" ,s);
    }

    @Test
    public void TestRotateStrSeq()
    {
        String s = check ("{a,f,g,j} << {j,o,7,laal} >>", "..");
        shoudBeOK ("{\"laal\",\"j\",\"o\",\"7\"}{\"f\",\"g\",\"j\",\"a\"}" ,s);
    }

    @Test
    public void TestDivToStrSeq()
    {
        String s = check ("laladumm 2 /", ".");
        shoudBeOK ("{\"la\",\"la\",\"du\",\"mm\"}" ,s);
    }

    @Test
    public void TestStrSeqFact()
    {
        String s = check ("\"peter       ist    lieb\" factor", ".");
        shoudBeOK ("{\"peter\",\"ist\",\"lieb\"}" ,s);
    }

    @Test
    public void TestStrSeqPlus()
    {
        String s = check ("{a,b,c,d} peter +", ".");
        shoudBeOK ("{\"a\",\"b\",\"c\",\"d\",\"peter\"}" ,s);
        s = check ("peter {a,b,c,f} +", ".");
        shoudBeOK ("{\"peter\",\"a\",\"b\",\"c\",\"f\"}" ,s);
    }

    @Test
    public void TestStrSeqSubX()
    {
        String s = check ("peter 1 / {e,t} - toStr", ".");
        shoudBeOK ("pr" ,s);
    }

    @Test
    public void TestListConv()
    {
        String s = check ("{1,3,3,4.5,6} dup toSList type swap type", ". sp .");
        shoudBeOK ("DoubleSequence StringSequence" ,s);
    }

    @Test
    public void TestSListPermute()
    {
        String s = check ("peter 1 / 12 permute toStr", ".");
        shoudBeOK ("trpee" ,s);
    }

    @Test
    public void TestSListSpc()
    {
        String s = check ("\"fick dich\" toslist", ".");
        shoudBeOK ("{\"fick\",\"dich\"}" ,s);
        s = check ("\"fickdich\" toslist", ".");
        shoudBeOK ("{\"f\",\"i\",\"c\",\"k\",\"d\",\"i\",\"c\",\"h\"}" ,s);
        s = check ("fickdich toslist", ".");
        shoudBeOK ("{\"f\",\"i\",\"c\",\"k\",\"d\",\"i\",\"c\",\"h\"}" ,s);
    }

    @Test
    public void TestStringSplit()
    {
        String s = check ("\"move back mo'fucker the onyx is here\" toslist", ".");
        shoudBeOK ("{\"move\",\"back\",\"mo'fucker\",\"the\",\"onyx\",\"is\",\"here\"}" ,s);
    }

    @Test
    public void TestLswap()
    {
        String s = check ("{a,b,c,d,r} 1 2 lswap dup type", "..");
        shoudBeOK ("StringSequence{\"a\",\"c\",\"b\",\"d\",\"r\"}" ,s);
        s = check ("{1,2,3,4,5} 1 2 lswap dup type", "..");
        shoudBeOK ("DoubleSequence{1,3,2,4,5}" ,s);
    }

    @Test
    public void TestRev()
    {
        String s = check ("\"hello world\" rev", ".");
        shoudBeOK ("dlrow olleh" ,s);
        s = check ("{1,2,3,4,5} rev", ".");
        shoudBeOK ("{5,4,3,2,1}" ,s);
        s = check ("{gone,wild,man} rev", ".");
        shoudBeOK ("{\"man\",\"wild\",\"gone\"}" ,s);
    }

    @Test
    public void TestLSystem()
    {
        String s = check ("ABA lsput A->AB lsrule B->A lsrule lsget", ".");
        shoudBeOK ("ABAAB" ,s);
        s = check ("ABA lsput A->AB lsrule B->A lsrule lsclr lsget", ".");
        shoudBeOK ("ABA" ,s);
    }

    @Test
    public void StrSortRev()
    {
        String s = check ("move_back_motherfucker sort rev", ".");
        shoudBeOK ("vutrroommkkhfeeeccba__" ,s);
    }

    @Test
    public void percTest()
    {
        String s = check ("12345 25 percent 12345 swap whatperc", ".");
        shoudBeOK ("25" ,s);
    }

    @Test
    public void typeTest()
    {
        String s = check ("33 type 33L type 33/1 type 33.0 type 33+0i type {33} type {{33}} type",
                ".......");
        shoudBeOK ("DoubleMatrixDoubleSequenceComplexDoubleFractionBigIntegerLong" ,s);
    }

    @Test
    public void strSubNumTest()
    {
        String s = check ("laladumm 4 -",
                ".");
        shoudBeOK ("lala" ,s);
    }

    @Test
    public void strSubStrTest()
    {
        String s = check ("peter lieb -",
                ".");
        shoudBeOK ("ptr" ,s);
    }

    @Test
    public void morseTest()
    {
        String s = check ("\"hello world\" morsetxt",
                ".");
        shoudBeOK ("···· · ·-·· ·-·· ---  ·-- --- ·-· ·-·· -··" ,s);
    }

    @Test
    public void soundTest()
    {
        check ("1234 dtmf abcd dtmf sos morse c4c5c6c7 tune 1000 300 beep 1000 200 beep", ".");
    }

    @Test
    public void binHexInputTest()
    {
        String s = check ("0xc000 _ 0b1001","...");
        shoudBeOK ("9_49152" ,s);
    }

    @Test
    public void binStrTest()
    {
        String s = check ("1234 dup binstr swap \"-\" swap hexstr","...");
        shoudBeOK ("4d2-10011010010" ,s);
    }

    @Test
    public void commentTest()
    {
        String s = check ("1234 ( a comment ) dup ( next comm.) +",".");
        shoudBeOK ("2468" ,s);
        s = check ("1234 \\ some other bullshitt",".");
        shoudBeOK ("1234" ,s);
    }

    @Test
    public void brainFuckTest()
    {
        String code = "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.";
        String s = check ("\""+code+"\""+" bf",".");
        shoudBeOK ("Hello World!\n" ,s);
    }
}
