package jforth.forthwords;

import jforth.*;
import jforth.audio.SAMSpeech;
import jforth.audio.WaveTools;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.mathIT.util.FunctionParser;
import webserver.SimpleWebserver;

import java.io.*;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Objects;

import static org.apache.commons.math3.special.Gamma.gamma;
import static org.mathIT.numbers.Riemann.zeta;

class Filler1 {
    static Object fetchVar(OStack dStack, OStack vStack) throws Exception{
        Object o = dStack.pop();
        if (!(o instanceof StorageWord)) {
            throw new Exception("no storage word");
        }
        StorageWord sw = (StorageWord) o;
        Object data;
        if (sw.isNotArray()) {
            data = sw.fetch(0);
            if (data == null) {
                throw new Exception("var is empty");
            }
        } else {  // No Array
            Object off = dStack.pop();
            if (!(off instanceof Long)) {
                throw new Exception("Offset not Long");
            }
            int offset = (int) ((Long) off).longValue();
            data = sw.fetch(offset);
            if (data == null) {
                throw new Exception("var is empty");
            }
        }
        return data;
    }

    static void fill(WordsList _fw, PredefinedWords predefinedWords) {
        // do nothing. comments handled by tokenizer
        _fw.add(new PrimitiveWord   // dummy
                (
                        "(", true, "Begin comment",
                        (dStack, vStack) -> 1
                ));

        // do nothing. this handled by tokenizer
        _fw.add(new PrimitiveWord  // dummy
                (
                        ".\"", true, "String output",
                        (dStack, vStack) -> 1
                ));

        _fw.add(new PrimitiveWord
                (
                        "'", true, "Push word from dictionary onto stack",
                        (dStack, vStack) ->
                        {
                            if (predefinedWords._jforth.compiling) {
                                return 1;
                            }
                            String name = predefinedWords._jforth.getNextToken();
                            if (name == null) {
                                return 0;
                            }
                            BaseWord bw = null;
                            try {
                                bw = predefinedWords._jforth.dictionary.search(name);
                            } catch (Exception ignore) {
                            }
                            if (bw != null) {
                                dStack.push(bw);
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "dup", "Duplicate TOS",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.peek();
                            WordHelpers.dup(o, dStack);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "2dup", "Duplicate upper 2 elements",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            WordHelpers.dup(o2, dStack);
                            WordHelpers.dup(o1, dStack);
                            WordHelpers.dup(o2, dStack);
                            WordHelpers.dup(o1, dStack);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "?dup", "Duplicate TOS if not zero",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty()) {
                                return 0;
                            }
                            Object o = dStack.peek();
                            if (o instanceof Long) {
                                if (((Long) o) != 0) {
                                    dStack.push(o);
                                }
                            } else if (o instanceof Double) {
                                if (((Double) o) != 0.0) {
                                    dStack.push(o);
                                }
                            } else if (o instanceof DoubleSequence) {
                                if (!((DoubleSequence) o).isEmpty()) {
                                    dStack.push(new DoubleSequence((DoubleSequence) o));
                                }
                            } else if (o instanceof String) {
                                if (!((String) o).isEmpty()) {
                                    dStack.push(o);
                                }
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "permute", "Generate permutation",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o1 = dStack.pop();
                                if (o1 instanceof SequenceBase) {
                                    SequenceBase ds = (SequenceBase) o1;
                                    int[][] arr = LehmerCode.perm(ds.length());
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("{");
                                    for (int[] i : arr) {
                                        sb.append(ds.rearrange(i));
                                    }
                                    sb.append("}");
                                    dStack.push(sb.toString());
                                } else {
                                    long l2 = Utilities.getLong(o1);  // perm #
                                    SequenceBase ds = (SequenceBase) dStack.pop();
                                    int[] arr = LehmerCode.perm(ds.length(), (int) l2);
                                    dStack.push(ds.rearrange(arr));
                                }
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "drop", false, "remove TOS",
                        (dStack, vStack) ->
                        {
                            dStack.pop();
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "swap", false, "Swap TOS and TOS-1",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            dStack.push(o1);
                            dStack.push(o2);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "2swap", "Swaps first 2 pairs of elements",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            Object o3 = dStack.pop();
                            Object o4 = dStack.pop();
                            dStack.push(o2);
                            dStack.push(o1);
                            dStack.push(o4);
                            dStack.push(o3);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "tuck", false, "Copy TOS to TOS-2",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            dStack.push(o1);
                            dStack.push(o2);
                            dStack.push(o1);
                            return 1;
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "over", "Copy TOS-1 to TOS",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            dStack.push(o2);
                            dStack.push(o1);
                            dStack.push(o2);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "2over", "Copy TOS-2 & TOS-3 to TOS & TOS-1",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            Object o3 = dStack.pop();
                            Object o4 = dStack.pop();
                            dStack.push(o4);
                            dStack.push(o3);
                            dStack.push(o2);
                            dStack.push(o1);
                            dStack.push(o4);
                            dStack.push(o3);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "rot", "Rotate first 3 elements on stack",
                        (dStack, vStack) ->
                        {
                            Object o3 = dStack.pop();
                            Object o2 = dStack.pop();
                            Object o1 = dStack.pop();
                            dStack.push(o2);
                            dStack.push(o3);
                            dStack.push(o1);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "2rot", "Rotates first 3 pairs of elements",
                        (dStack, vStack) ->
                        {
                            Object o6 = dStack.pop();
                            Object o5 = dStack.pop();
                            Object o4 = dStack.pop();
                            Object o3 = dStack.pop();
                            Object o2 = dStack.pop();
                            Object o1 = dStack.pop();
                            dStack.push(o3);
                            dStack.push(o4);
                            dStack.push(o5);
                            dStack.push(o6);
                            dStack.push(o1);
                            dStack.push(o2);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "depth", false, "number of elements currently on the stack",
                        (dStack, vStack) ->
                        {
                            Long i = (long) dStack.size();
                            dStack.push(i);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "<", false, "gives 1 of TOS-1 smaller than TOS",
                        (dStack, vStack) ->
                        {
                            Object o2 = dStack.pop();
                            Object o1 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long)) {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                if (i1 < i2) {
                                    dStack.push(JForth.TRUE);
                                } else {
                                    dStack.push(JForth.FALSE);
                                }
                            } else if ((o1 instanceof Double) && (o2 instanceof Double)) {
                                double d1 = (Double) o1;
                                double d2 = (Double) o2;
                                if (d1 < d2) {
                                    dStack.push(JForth.TRUE);
                                } else {
                                    dStack.push(JForth.FALSE);
                                }
                            } else if ((o1 instanceof String) && (o2 instanceof String)) {
                                String s1 = (String) o1;
                                String s2 = (String) o2;
                                int result = s1.compareTo(s2);
                                if (result < 0) {
                                    dStack.push(JForth.TRUE);
                                } else {
                                    dStack.push(JForth.FALSE);
                                }
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "=", false, "1 if TOS is equal to TOS-1",
                        (dStack, vStack) ->
                        {
                            Object o2 = dStack.pop();
                            Object o1 = dStack.pop();
                            if (o1.equals(o2)) {
                                dStack.push(JForth.TRUE);
                            } else {
                                dStack.push(JForth.FALSE);
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "<>", false, "1 if TOS is not equal to TOS-1",
                        (dStack, vStack) ->
                        {
                            Object o2 = dStack.pop();
                            Object o1 = dStack.pop();
                            if (!o1.equals(o2)) {
                                dStack.push(JForth.TRUE);
                            } else {
                                dStack.push(JForth.FALSE);
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ">", false, "1 if TOS-1 is bigger than TOS",
                        (dStack, vStack) ->
                        {
                            Object o2 = dStack.pop();
                            Object o1 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long)) {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                if (i1 > i2) {
                                    dStack.push(JForth.TRUE);
                                } else {
                                    dStack.push(JForth.FALSE);
                                }
                            } else if ((o1 instanceof Double) && (o2 instanceof Double)) {
                                double d1 = (Double) o1;
                                double d2 = (Double) o2;
                                if (d1 > d2) {
                                    dStack.push(JForth.TRUE);
                                } else {
                                    dStack.push(JForth.FALSE);
                                }
                            } else if ((o1 instanceof String) && (o2 instanceof String)) {
                                String s1 = (String) o1;
                                String s2 = (String) o2;
                                int result = s1.compareTo(s2);
                                if (result > 0) {
                                    dStack.push(JForth.TRUE);
                                } else {
                                    dStack.push(JForth.FALSE);
                                }
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "0<", false, "Gives 1 if TOS smaller than 0",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long) {
                                long i1 = (Long) o1;
                                dStack.push((i1 < 0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            } else if (o1 instanceof Double) {
                                double d1 = (Double) o1;
                                dStack.push((d1 < 0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            } else if (o1 instanceof String) {
                                Long l1 = Utilities.parseLong((String) o1, 10);
                                if (l1 == null)
                                    return 0;
                                dStack.push((l1 < 0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "0=", "Gives 1 if TOS is zero",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long) {
                                long i1 = (Long) o1;
                                dStack.push((i1 == 0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            } else if (o1 instanceof Double) {
                                double d1 = (Double) o1;
                                dStack.push((d1 == 0.0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            } else if (o1 instanceof String) {
                                Long l1 = Utilities.parseLong((String) o1, 10);
                                if (l1 == null)
                                    return 0;
                                dStack.push((l1 == 0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "0>", "Gives 1 if TOS greater than zero",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long) {
                                long i1 = (Long) o1;
                                dStack.push((i1 > 0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            } else if (o1 instanceof Double) {
                                double d1 = (Double) o1;
                                dStack.push((d1 > 0) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "not", "Gives 0 if TOS is not 0, otherwise 1",
                        (dStack, vStack) ->
                        {
                            try {
                                Long i1 = Utilities.readLong(dStack);
                                dStack.push((Objects.equals(i1, JForth.FALSE)) ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "true", "Gives 1",
                        (dStack, vStack) ->
                        {
                            dStack.push(JForth.TRUE);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "false", "Gives 0",
                        (dStack, vStack) ->
                        {
                            dStack.push(JForth.FALSE);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "+", "Add 2 values on stack",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            return WordHelpers.add(dStack, o1, o2, predefinedWords);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "-", "Substract values",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            return WordHelpers.sub(dStack, o1, o2);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "1+", "Add 1 to TOS",
                        (dStack, vStack) ->
                        {
                            Object o2 = dStack.pop();
                            return WordHelpers.add(dStack, 1L, o2, predefinedWords);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "1-", "Substract 1 from TOS",
                        (dStack, vStack) ->
                        {
                            Object o2 = dStack.pop();
                            return WordHelpers.sub(dStack, 1L, o2);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "2+", "Add 2 to TOS",
                        (dStack, vStack) ->
                        {
                            Object o2 = dStack.pop();
                            return WordHelpers.add(dStack, 2L, o2, predefinedWords);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "2-", "Substract 2 from TOS",
                        (dStack, vStack) ->
                        {
                            Object o2 = dStack.pop();
                            return WordHelpers.sub(dStack, 2L, o2);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "*", "Multiply TOS and TOS-1",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            return WordHelpers.mult(dStack, o1, o2);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "gf*", "Galois multiplication of TOS and TOS-1",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            return WordHelpers.multGF(dStack, o1, o2);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "gf/", "Galois division TOS-1 by TOS",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            return WordHelpers.divGF(dStack, o1, o2);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "gf+", "Galois addition TOS-1 + TOS",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            return WordHelpers.addGF(dStack, o1, o2);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "gf-", "Galois subtraction TOS-1 - TOS",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            return WordHelpers.subGF(dStack, o1, o2);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "2*", "Multiply TOS by 2",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            return WordHelpers.mult(dStack, o1, 2L);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "/", "Divide TOS-1 by TOS",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            return WordHelpers.div(dStack, o1, o2);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "2/", "Divide TOS by 2",
                        (dStack, vStack) ->
                        {
                            Object o2 = dStack.pop();
                            return WordHelpers.div(dStack, 2L, o2);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "mod", "Division remainder",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            try {
                                dStack.push(Utilities.doCalcBigInt(o2, o1, BigInteger::mod));
                                return 1;
                            } catch (Exception ignored) {
                            }
                            try {
                                dStack.push(PolySupport.execute(o2, o1, PolySupport::polyMod));
                                return 1;
                            } catch (Exception ignored) {
                            }
                            if ((o1 instanceof Long) && (o2 instanceof Long)) {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 %= i1;
                                dStack.push(i2);
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "/mod", "Dividend and Remainder",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            try {
                                dStack.push(PolySupport.execute(o2, o1, PolySupport::polyMod));
                                dStack.push(PolySupport.execute(o2, o1, PolySupport::polyDiv));
                                return 1;
                            } catch (Exception ignored) {
                            }
                            try {
                                long l1 = Utilities.getLong(o1);
                                long l2 = Utilities.getLong(o2);
                                dStack.push(l2 % l1);
                                dStack.push(l2 / l1);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "max", "Biggest value",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof DoubleSequence) {
                                dStack.push(DoubleSequence.getStats((DoubleSequence) o1).getMax());
                                return 1;
                            }
                            Object o2 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long)) {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 = Math.max(i1, i2);
                                dStack.push(i2);
                                return 1;
                            }
                            if ((o1 instanceof String) && (o2 instanceof String)) {
                                String s1 = (String) o1;
                                String s2 = (String) o2;
                                s2 = (s1.compareTo(s2) > 0) ? s1 : s2;
                                dStack.push(s2);
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "min", "Smallest value",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof DoubleSequence) {
                                dStack.push(DoubleSequence.getStats((DoubleSequence) o1).getMin());
                                return 1;
                            }
                            Object o2 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long)) {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 = Math.min(i1, i2);
                                dStack.push(i2);
                                return 1;
                            }
                            if ((o1 instanceof String) && (o2 instanceof String)) {
                                String s1 = (String) o1;
                                String s2 = (String) o2;
                                s2 = (s1.compareTo(s2) < 0) ? s1 : s2;
                                dStack.push(s2);
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "gMean", "Geometric mean",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof DoubleSequence) {
                                dStack.push
                                        (DoubleSequence.getStats((DoubleSequence) o1).getGeometricMean());
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "mean", "Mean value of sequence",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof DoubleSequence) {
                                dStack.push
                                        (DoubleSequence.getStats((DoubleSequence) o1).getMean());
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "qMean", "Quadratic Mean of sequence",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof DoubleSequence) {
                                dStack.push
                                        (DoubleSequence.getStats((DoubleSequence) o1).getQuadraticMean());
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "stdDev", "Standard Deviation of sequence",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof DoubleSequence) {
                                dStack.push
                                        (DoubleSequence.getStats((DoubleSequence) o1).getStandardDeviation());
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "var", "Variance of sequence",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof DoubleSequence) {
                                dStack.push
                                        (DoubleSequence.getStats((DoubleSequence) o1).getVariance());
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "abs", "Absolute value",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long) {
                                long i1 = (Long) o1;
                                i1 = Math.abs(i1);
                                dStack.push(i1);
                            } else if (o1 instanceof Complex) {
                                Complex d1 = (Complex) o1;
                                dStack.push(d1.abs());
                            } else if (o1 instanceof Fraction) {
                                Fraction d1 = (Fraction) o1;
                                dStack.push(d1.abs());
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord   //
                (
                        "phi", "Phi of complex number",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Complex) {
                                Complex d1 = (Complex) o1;
                                dStack.push(Math.atan(d1.getImaginary() / d1.getReal()));
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord   //
                (
                        "conj", "Conjugate of complex or fraction",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Complex) {
                                Complex d1 = (Complex) o1;
                                dStack.push(d1.conjugate());
                            }
                            if (o1 instanceof Fraction) {
                                Fraction d1 = (Fraction) o1;
                                dStack.push(d1.reciprocal());
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord   //
                (
                        "split", "Split object into partitions",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Complex) {
                                Complex d1 = (Complex) o1;
                                dStack.push(d1.getReal());
                                dStack.push(d1.getImaginary());
                                return 1;
                            }
                            if (o1 instanceof Fraction) {
                                Fraction d1 = (Fraction) o1;
                                dStack.push((double) d1.getNumerator());
                                dStack.push((double) d1.getDenominator());
                                return 1;
                            }
                            if (o1 instanceof Double) {
                                double d1 = (Double) o1;
                                dStack.push(Math.floor(d1));
                                dStack.push(d1 - Math.floor(d1));
                                return 1;
                            }
                            if (o1 instanceof String) {
                                String s = (String) o1;
                                if (dStack.empty()) {
                                    return 0;
                                }
                                Object o2 = dStack.pop();
                                if (!(o2 instanceof String)) {
                                    return 0;
                                }
                                String[] sp = s.split((String) o2);
                                for (String x : sp) {
                                    dStack.push(x);
                                }
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "and", "Binary and of 2 values",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long)) {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 &= i1;
                                dStack.push(i2);
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "or", "Binary or of 2 values",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long)) {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 |= i1;
                                dStack.push(i2);
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "xor", "Xors two values",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long)) {
                                long i1 = (Long) o1;
                                long i2 = (Long) o2;
                                i2 ^= i1;
                                dStack.push(i2);
                            } else if ((o1 instanceof String) && (o2 instanceof FileBlob)) {
                                String s = (String) o1;
                                FileBlob fb = (FileBlob) o2;
                                fb.xor(s);
                                dStack.push(fb);
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "<<", "Rotate left",
                        (dStack, vStack) ->
                        {
                            Object o2 = dStack.pop();
                            if (o2 instanceof Long) {
                                long i2 = (Long) o2;
                                dStack.push(Long.rotateLeft(i2, 1));
                                return 1;
                            }
                            if (o2 instanceof SequenceBase) {
                                SequenceBase i2 = (SequenceBase) o2;
                                dStack.push(i2.rotateLeft(1));
                                return 1;
                            }
                            if (o2 instanceof String) {
                                String i2 = (String) o2;
                                dStack.push(Utilities.rotLeft(i2, 1));
                                return 1;
                            }
                            if (o2 instanceof FileBlob) {
                                FileBlob fb = (FileBlob) o2;
                                byte[] cnt = Utilities.rotLeft(fb.get_content(), 1);
                                FileBlob n = new FileBlob(cnt, fb.getPath());
                                dStack.push(n);
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ">>", "Rotate right",
                        (dStack, vStack) ->
                        {
                            Object o2 = dStack.pop();
                            if (o2 instanceof Long) {
                                long i2 = (Long) o2;
                                dStack.push(Long.rotateRight(i2, 1));
                                return 1;
                            }
                            if (o2 instanceof SequenceBase) {
                                SequenceBase i2 = (SequenceBase) o2;
                                dStack.push(i2.rotateRight(1));
                                return 1;
                            }
                            if (o2 instanceof String) {
                                String i2 = (String) o2;
                                dStack.push(Utilities.rotRight(i2, 1));
                                return 1;
                            }
                            if (o2 instanceof FileBlob) {
                                FileBlob fb = (FileBlob) o2;
                                byte[] cnt = Utilities.rotRight(fb.get_content(), 1);
                                FileBlob n = new FileBlob(cnt, fb.getPath());
                                dStack.push(n);
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ".", "Pop TOS and print it",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            String outstr = predefinedWords._jforth.ObjectToString(o);
                            if (outstr == null) {
                                return 0;
                            }
                            predefinedWords._jforth._out.print(outstr);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "<.", "Restore last stack object",
                        (dStack, vStack) ->
                        {
                            if (!dStack.unpop()) {
                                predefinedWords._jforth._out.print("Nothing to do ...");
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ".v", "Show whole variable stack",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth._out.print(predefinedWords._jforth.dictionary.variableList());
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ".s", "Show whole data stack",
                        (dStack, vStack) ->
                        {
                            for (Object o : dStack) {
                                predefinedWords._jforth._out.print(predefinedWords._jforth.ObjectToString(o) + " ");
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "flush", "pops and shows whole stack",
                        (dStack, vStack) ->
                        {
                            while (!dStack.isEmpty()) {
                                Object o = dStack.pop();
                                predefinedWords._jforth._out.print(predefinedWords._jforth.ObjectToString(o));
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "revs", "reverse whole stack",
                        (dStack, vStack) ->
                        {
                            ArrayList<Object> al = new ArrayList<>();
                            while (!dStack.isEmpty()) {
                                Object o = dStack.pop();
                                al.add(o);
                            }
                            for (Object o : al)
                                dStack.push(o);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "cr", "Emit carriage return",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth._out.println();
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sp", "Emit single space",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth._out.print(' ');
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "spaces", "Emit multiple spaces",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long) {
                                long i1 = (Long) o1;
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < i1; i++) {
                                    sb.append(" ");
                                }
                                predefinedWords._jforth._out.print(sb.toString());
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "bin", "Set number base to 2",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth.base = 2;
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "dec", "Set number base to 10",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth.base = 10;
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "hex", "Set number base to 16",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth.base = 16;
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "setbase", "Set a new number base",
                        (dStack, vStack) ->
                        {
                            try {
                                Long l = Utilities.readLong(dStack);
                                if (l > 36 || l < 2) {
                                    return 0;
                                }
                                predefinedWords._jforth.base = l.intValue();
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "recurse", "Re-run current word",
                        (dStack, vStack) ->
                        {
                            try {
                                return predefinedWords._jforth.currentWord.apply(dStack, vStack);
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "recursive", true, "Re-run current word",
                        (dStack, vStack) ->
                        {
                            try {
                                //return predefinedWords._jforth.currentWord.execute(dStack, vStack);
                                predefinedWords._jforth.currentWord = predefinedWords._jforth.wordBeingDefined;
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ":", "Begin word definition",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth.compiling = true;
                            String name = predefinedWords._jforth.getNextToken();
                            if (name == null) {
                                return 0;
                            }
                            predefinedWords._jforth.wordBeingDefined = new NonPrimitiveWord(name);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ";", true, "End word definition",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth.compiling = false;
                            predefinedWords._jforth.dictionary.add(predefinedWords._jforth.wordBeingDefined);
                            predefinedWords._jforth.wordBeingDefined = null;
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "words", "Show all words",
                        (dStack, vStack) ->
                        {
                            String c = Utilities.readStringOrNull(dStack);
                            dStack.push(predefinedWords._jforth.dictionary.toString(false, c));
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "wordsd", "Show words and description",
                        (dStack, vStack) ->
                        {
                            String c = Utilities.readStringOrNull(dStack);
                            dStack.push(predefinedWords._jforth.dictionary.toString(true, c));
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "forget", true, "Delete word from dictionary",
                        (dStack, vStack) ->
                        {
                            String name = predefinedWords._jforth.getNextToken();
                            if (name == null) {
                                return 0;
                            }
                            BaseWord bw;
                            try {
                                bw = predefinedWords._jforth.dictionary.search(name);
                            } catch (Exception ignore) {
                                return 0;
                            }
                            if (bw != null) {
                                predefinedWords._jforth.dictionary.remove(bw);
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "constant", "create new Constant",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty()) {
                                return 0;
                            }
                            String name = predefinedWords._jforth.getNextToken();
                            if (name == null) {
                                return 0;
                            }
                            NonPrimitiveWord constant = new NonPrimitiveWord(name);
                            predefinedWords._jforth.dictionary.add(constant);
                            Object o1 = dStack.pop();
                            BaseWord bw = WordHelpers.toLiteral(o1);
                            constant.addWord(bw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "variable", true, "Create new variable",
                        (dStack, vStack) ->
                        {
                            String name = predefinedWords._jforth.getNextToken();
                            if (name == null) {
                                return 0;
                            }
                            StorageWord sw = new StorageWord(name, 1, false);
                            predefinedWords._jforth.dictionary.add(sw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        ">r", "Put TOS to variable stack",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            vStack.push(o);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "r>", "Move variable to data stack",
                        (dStack, vStack) ->
                        {
                            Object o = vStack.pop();
                            dStack.push(o);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "r@", "Copy variable to data stack",
                        (dStack, vStack) ->
                        {
                            Object o = vStack.peek();
                            dStack.push(o);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "!", "Store value into variable or array",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            if (!(o instanceof StorageWord)) {
                                return 0;
                            }
                            StorageWord sw = (StorageWord) o;
                            int offset = 0;
                            if (sw.isNotArray()) {
                                if (dStack.empty()) {
                                    return 0;
                                }
                            } else {
                                if (dStack.size() < 2) {
                                    return 0;
                                }
                                Object off = dStack.pop();
                                if (!(off instanceof Long)) {
                                    return 0;
                                }
                                offset = (int) ((Long) off).longValue();
                            }
                            return sw.store(dStack.pop(), offset);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "+!", "Add value to variable",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            if (!(o instanceof StorageWord)) {
                                return 0;
                            }
                            StorageWord sw = (StorageWord) o;
                            int offset = 0;
                            if (sw.isNotArray()) {
                                if (dStack.empty()) {
                                    return 0;
                                }
                            } else {
                                if (dStack.size() < 2) {
                                    return 0;
                                }
                                Object off = dStack.pop();
                                if (!(off instanceof Long)) {
                                    return 0;
                                }
                                offset = (int) ((Long) off).longValue();
                            }
                            return sw.plusStore(dStack.pop(), offset, predefinedWords);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "@", "Put variable value on stack",
                        (dStack, vStack) ->
                        {
                            try {
                                dStack.push(fetchVar (dStack, vStack));
                            } catch (Exception e) {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "?", "immediately print variable content",
                        (dStack, vStack) ->
                        {
                            try {
                                Object xx = fetchVar (dStack, vStack);
                                String outstr = predefinedWords._jforth.ObjectToString(xx) +' ';
                                if (outstr == null) {
                                    return 0;
                                }
                                predefinedWords._jforth._out.print(outstr);
                            } catch (Exception e) {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "array", "Create array",
                        (dStack, vStack) ->
                        {
                            try {
                                Long ll = Utilities.readLong(dStack);
                                int size = ll.intValue();
                                String name = predefinedWords._jforth.getNextToken();
                                if (name == null) {
                                    return 0;
                                }
                                StorageWord sw = new StorageWord(name, size, true);
                                predefinedWords._jforth.dictionary.add(sw);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "round", "Rounding double value",
                        (dStack, vStack) ->
                        {
                            try {
                                double d1 = Utilities.readDouble(dStack);
                                double r = Math.pow(10, d1);
                                Object o = dStack.pop();
                                if (o instanceof PolynomialFunction) {
                                    PolynomialFunction p = PolySupport.roundPoly(
                                            (PolynomialFunction) o, r);
                                    dStack.push(p);
                                    return 1;
                                }
                                if (o instanceof Complex) {
                                    double dr = ((Complex) o).getReal();
                                    double di = ((Complex) o).getImaginary();
                                    Complex cx = new Complex
                                            (
                                                    Math.round(r * dr) / r,
                                                    Math.round(r * di) / r
                                            );
                                    dStack.push(cx);
                                    return 1;
                                }
                                if (o instanceof DoubleSequence) {
                                    double[] vals = ((DoubleSequence) o).asPrimitiveArray();
                                    for (int s = 0; s < vals.length; s++)
                                        vals[s] = Math.round(vals[s] * r) / r;
                                    dStack.push(new DoubleSequence(vals));
                                    return 1;
                                }
                                Double d2 = Utilities.getDouble(o);
                                double dd = Math.round(r * d2) / r;
                                dStack.push(dd);
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "uptime", "Get uptime",
                        (dStack, vStack) ->
                        {
                            long ll = System.currentTimeMillis() - predefinedWords._jforth.StartTime;
                            String o1 = Utilities.readStringOrNull(dStack);
                            try {
                                dStack.push(Utilities.formatTime(ll, o1));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "time", "Get time",
                        (dStack, vStack) ->
                        {
                            String o1 = Utilities.readStringOrNull(dStack);
                            try {
                                dStack.push(Utilities.formatTime(System.currentTimeMillis(), o1));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sleep", "Sleep some milliseconds",
                        (dStack, vStack) ->
                        {
                            try {
                                long l1 = Utilities.readLong(dStack);
                                Thread.sleep(l1);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "emit", "Emit single char to console",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long) {
                                Long l = (Long) o1;
                                predefinedWords._jforth._out.print((char) (long) l);
                                predefinedWords._jforth._out.flush();
                                return 1;
                            }
                            if (o1 instanceof String) {
                                String str = (String) o1;
                                for (int s = 0; s < str.length(); s++) {
                                    predefinedWords._jforth._out.print(str.charAt(s));
                                }
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "fraction", "Create a fraction from 2 Numbers",
                        (dStack, vStack) ->
                        {
                            try {
                                int o1 = (int) Utilities.readLong(dStack);
                                int o2 = (int) Utilities.readLong(dStack);
                                dStack.push(new Fraction(o1, o2));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toComplex", "convert to Complex",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o1 = dStack.pop();
                                if (o1 instanceof DoubleSequence) {
                                    DoubleSequence t = (DoubleSequence) o1;
                                    dStack.push(new Complex(t.pick(0), t.pick(1)));
                                } else {
                                    Double d = Utilities.getDouble(o1);
                                    dStack.push(new Complex(d, 0));
                                }
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "complex", "Create a complex from 2 numbers",
                        (dStack, vStack) ->
                        {
                            try {
                                double o1 = Utilities.readDouble(dStack);
                                double o2 = Utilities.readDouble(dStack);
                                dStack.push(new Complex(o1, o2));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toLong", "Make long values of what is on the stack",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Double) {
                                dStack.push(((Double) o1).longValue());
                            } else if (o1 instanceof DoubleSequence) {
                                dStack.push(((DoubleSequence) o1).fromBitList().longValue());
                            } else if (o1 instanceof String) {
                                try {
                                    dStack.push(Long.parseLong((String) o1));
                                } catch (NumberFormatException e) {
                                    FunctionParser fp = new FunctionParser((String) o1);
                                    dStack.push((long) fp.evaluate(0, 0));
                                }
                            } else if (o1 instanceof Complex) {
                                Complex oc = (Complex) o1;
                                dStack.push((long) oc.getReal());
                                dStack.push((long) oc.getImaginary());
                            } else if (o1 instanceof Fraction) {
                                Fraction oc = (Fraction) o1;
                                dStack.push((long) oc.getNumerator() / (long) oc.getDenominator());
                            } else if (o1 instanceof BigInteger) {
                                BigInteger oc = (BigInteger) o1;
                                dStack.push(oc.longValue());
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toBig", "Make BigInt values of what is on the stack",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long) {
                                dStack.push(BigInteger.valueOf((Long) o1));
                            } else if (o1 instanceof Double) {
                                dStack.push(BigInteger.valueOf(((Double) o1).longValue()));
                            } else if (o1 instanceof DoubleSequence) {
                                dStack.push(((DoubleSequence) o1).fromBitList());
                            } else if (o1 instanceof String) {
                                dStack.push(new BigInteger((String) o1));
                            } else if (o1 instanceof Complex) {
                                Complex oc = (Complex) o1;
                                dStack.push(BigInteger.valueOf((long) oc.getReal()));
                                dStack.push(BigInteger.valueOf((long) oc.getImaginary()));
                            } else if (o1 instanceof Fraction) {
                                Fraction oc = (Fraction) o1;
                                dStack.push(BigInteger.valueOf((long) oc.getNumerator() / (long) oc.getDenominator()));
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toBits", "Make bit sequence from number",
                        (dStack, vStack) ->
                        {
                            try {
                                BigInteger l = Utilities.readBig(dStack);
                                dStack.push(DoubleSequence.makeBits(l));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toDouble", "Make double value of what is on the stack",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long) {
                                dStack.push((double) (Long) o1);
                            } else if (o1 instanceof String) {
                                try {
                                    dStack.push(Double.parseDouble((String) o1));
                                } catch (NumberFormatException e) {
                                    FunctionParser fp = new FunctionParser((String) o1);
                                    dStack.push(fp.evaluate(0, 0));
                                }
                            } else if (o1 instanceof Complex) {
                                Complex oc = (Complex) o1;
                                dStack.push(oc.getReal());
                                dStack.push(oc.getImaginary());
                            } else if (o1 instanceof Fraction) {
                                Fraction oc = (Fraction) o1;
                                dStack.push((double) oc.getNumerator() / (double) oc.getDenominator());
                            } else if (o1 instanceof BigInteger) {
                                BigInteger oc = (BigInteger) o1;
                                dStack.push(oc.doubleValue());
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "type", "Get type of TOS as string",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.peek();
                            dStack.push(o1.getClass().getSimpleName());
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "big", "BigPrint",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o = dStack.pop();
                                String outstr = predefinedWords._jforth.makePrintable(o);
                                dStack.push(Utilities.bigPrint(outstr, 20));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sam", "speak with SAM voice",
                        (dStack, vStack) ->
                        {
                            try {
                                String words = Utilities.readString(dStack);
                                byte[] bstr = SAMSpeech.doSam(words);
                                WaveTools.playWave(bstr, false);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toFraction", "Make fraction from value on the stack",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o1 = dStack.pop();
                                dStack.push(Utilities.getFrac(o1));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "crossP", "Cross product of 3D vectors",
                        (dStack, vStack) ->
                        {
                            Vector3D o1;
                            Vector3D o2;
                            try {
                                o1 = Utilities.readVector3D(dStack);
                                o2 = Utilities.readVector3D(dStack);
                                Vector3D cp = o2.crossProduct(o1);
                                dStack.push(new DoubleSequence(cp));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "dotP", "Dot product of 3D vectors",
                        (dStack, vStack) ->
                        {
                            Vector3D o1;
                            Vector3D o2;
                            try {
                                o1 = Utilities.readVector3D(dStack);
                                o2 = Utilities.readVector3D(dStack);
                                double dp = o2.dotProduct(o1);
                                dStack.push(dp);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "mix", "Mix two Lists",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            try {
                                DoubleSequence d1 = Utilities.getDoubleSequence(o1);
                                DoubleSequence d2 = Utilities.getDoubleSequence(o2);
                                DoubleSequence ds = new DoubleSequence(DoubleSequence.mixin(d2, d1));
                                dStack.push(ds);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            try {
                                StringSequence d1 = Utilities.getStringSequence(o1);
                                StringSequence d2 = Utilities.getStringSequence(o2);
                                StringSequence ds = new StringSequence(StringSequence.mixin(d2, d1));
                                dStack.push(ds);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toM", "Make Matrix from Sequences",
                        (dStack, vStack) ->
                        {
                            ArrayList<DoubleSequence> arr = new ArrayList<>();
                            for (; ; ) {
                                if (dStack.isEmpty()) {
                                    break;
                                }
                                Object o1 = dStack.pop();
                                if (o1 instanceof DoubleSequence) {
                                    arr.add((DoubleSequence) o1);
                                } else {
                                    break;
                                }
                            }
                            dStack.push(DoubleMatrix.fromSequenceArray(arr));
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "detM", "Determinant of a Matrix",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof DoubleMatrix) {
                                RealMatrix bm = ((DoubleMatrix) o1);
                                dStack.push(new LUDecomposition(bm).getDeterminant());
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "lupM", "l/u decomposition of a Matrix",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof DoubleMatrix) {
                                RealMatrix bm = ((DoubleMatrix) o1);
                                LUDecomposition lud = new LUDecomposition(bm);
                                dStack.push(new DoubleMatrix(lud.getL()));
                                dStack.push(new DoubleMatrix(lud.getU()));
                                dStack.push(new DoubleMatrix(lud.getP()));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "transM", "Transpose a Matrix",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof DoubleMatrix) {
                                RealMatrix bm = ((DoubleMatrix) o1).transpose();
                                dStack.push(new DoubleMatrix(bm));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "invM", "Inverse of a Matrix",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof DoubleMatrix) {
                                RealMatrix inv = MatrixUtils.inverse((DoubleMatrix) o1);
                                dStack.push(new DoubleMatrix(inv));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "idM", "Create Identity Matrix",
                        (dStack, vStack) ->
                        {
                            try {
                                long d = Utilities.readLong(dStack);
                                dStack.push(DoubleMatrix.identity((int) d));
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "diagM", "Create diagonal Matrix from List",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence d = Utilities.readDoubleSequence(dStack);
                                dStack.push(DoubleMatrix.diagonal(d));
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toDList", "Create List of digits",
                        (dStack, vStack) ->
                        {
                            try {
                                String s = Utilities.readString(dStack);
                                dStack.push(DoubleSequence.fromNumberString(s));
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toSList", "Create String List from Double List",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            if (o instanceof DoubleSequence) {
                                DoubleSequence ds = (DoubleSequence) o;
                                dStack.push(new StringSequence(ds));
                                return 1;
                            } else if (o instanceof String) {
                                String s = StringEscape.unescape((String) o);
                                if (s.contains(" ")) {
                                    String[] sp = s.split(" ");
                                    dStack.push(new StringSequence(sp));
                                    return 1;
                                }
                                dStack.push(new StringSequence(s.toCharArray()));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toNumList", "Make number list of what is on the stack",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof StorageWord) {
                                StorageWord sw = (StorageWord)o1;
                                dStack.push (sw.asDoubleSequence());
                                return 1;
                            }
                            if (o1 instanceof String) {
                                String str = (String) o1;
                                DoubleSequence ds = new DoubleSequence();
                                for (int s = 0; s < str.length(); s++) {
                                    ds.add((double) str.charAt(s));
                                }
                                dStack.push(ds);
                                return 1;
                            }
                            if (o1 instanceof DoubleMatrix) {
                                DoubleSequence[] seq = ((DoubleMatrix) o1).toSequence();
                                for (DoubleSequence d : seq) {
                                    dStack.push(d);
                                }
                                return 1;
                            }
                            if (o1 instanceof Complex) {
                                Complex t = (Complex) o1;
                                DoubleSequence ds = new DoubleSequence();
                                ds.add(t.getReal());
                                ds.add(t.getImaginary());
                                dStack.push(ds);
                                return 1;
                            }
                            if (o1 instanceof Fraction) {
                                Fraction t = (Fraction) o1;
                                DoubleSequence ds = new DoubleSequence();
                                ds.add((double) t.getNumerator());
                                ds.add((double) t.getDenominator());
                                dStack.push(ds);
                                return 1;
                            }

                            dStack.push(o1);
                            DoubleSequence seq = new DoubleSequence();
                            do {
                                Object o2 = dStack.pop();
                                if (o2 instanceof Double) {
                                    seq.add((Double) o2);
                                } else if (o2 instanceof Long) {
                                    long ll = (Long) o2;
                                    seq.add((double) ll);
                                } else if (o2 instanceof DoubleSequence) {
                                    seq = seq.add((DoubleSequence) o2);
                                }
                            } while (!dStack.empty());
                            dStack.push(seq);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toPoly", "Make polynomial from doubleSequence",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            if (!(o instanceof DoubleSequence)) {
                                return 0;
                            }
                            PolynomialFunction p =
                                    new PolynomialFunction(((DoubleSequence) o).asPrimitiveArray());
                            dStack.push(p);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "fitPoly", "Make polynomial sequence of Points",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            if (!(o instanceof DoubleSequence)) {
                                return 0;
                            }
                            try {
                                PolynomialFunction p = ((DoubleSequence) o).polyFit();
                                dStack.push(p);
                                return 1;
                            } catch (Exception ignored) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "lagPoly", "Make lagrange polynomial sequence of Points",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            if (!(o instanceof DoubleSequence)) {
                                return 0;
                            }
                            try {
                                PolynomialFunction p = ((DoubleSequence) o).lagFit();
                                dStack.push(p);
                                return 1;
                            } catch (Exception ignored) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "f'=", "Derive a polynomial",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            if (!(o instanceof PolynomialFunction)) {
                                return 0;
                            }
                            PolynomialFunction p = (PolynomialFunction) o;
                            dStack.push(p.polynomialDerivative());
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "Sf=", "Antiderive of a polynomial",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            Object o2;
                            Object o3;
                            PolynomialFunction p;
                            if (!dStack.isEmpty()) {
                                o2 = dStack.pop();
                                o3 = dStack.pop();
                                p = (PolynomialFunction) o3;
                            } else {
                                p = (PolynomialFunction) o;
                                dStack.push(PolySupport.antiDerive(p));
                                return 1;
                            }
                            if (o2 instanceof Long) {
                                o2 = ((Long) o2).doubleValue();
                            }
                            if (o instanceof Long) {
                                o = ((Long) o).doubleValue();
                            }
                            if (!(o2 instanceof Double && o instanceof Double)) {
                                return 0;
                            }
                            SimpsonIntegrator si = new SimpsonIntegrator();
                            double d = si.integrate(1000, p,
                                    (Double) o2, (Double) o);
                            dStack.push(d);
                            return 1;
                        }
                ));

//        _fw.add(new PrimitiveWord
//                (
//                        "f=", "Solve a polynomial",
//                        (dStack, vStack) ->
//                        {
//                            try {
//                                Object o = dStack.pop();
//                                PolynomialFunction p1 = PolySupport.readPoly(dStack);
//                                if (o instanceof DoubleSequence) {
//                                    DoubleSequence s1 = (DoubleSequence) o;
//                                    ArrayList<Double> list = new ArrayList<>();
//                                    for (double d1 : s1.asPrimitiveArray())
//                                        list.add(p1.value(d1));
//                                    dStack.push(new DoubleSequence(list));
//                                    return 1;
//                                }
//                                double d1 = Utilities.getDouble(o);
//                                dStack.push(p1.value(d1));
//                                return 1;
//                            } catch (Exception e) {
//                                return 0;
//                            }
//                        }
//                ));

        _fw.add(new PrimitiveWord
                (
                        "apply", "Apply polynomial to sequence",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence s1 = Utilities.readDoubleSequence(dStack);
                                PolynomialFunction p1 = PolySupport.readPoly(dStack);
                                dStack.push(s1.apply(p1));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "toStr", "Make string of TOS",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            if (o1 instanceof Long) {
                                dStack.push(Long.toString((Long) o1, predefinedWords._jforth.base).toUpperCase());
                            } else if (o1 instanceof Double) {
                                dStack.push(Double.toString((Double) o1));
                            } else if (o1 instanceof Fraction) {
                                dStack.push(Utilities.formatFraction((Fraction) o1));
                            } else if (o1 instanceof Complex) {
                                dStack.push(Utilities.formatComplex((Complex) o1));
                            } else if (o1 instanceof DoubleSequence) {
                                dStack.push(((DoubleSequence) o1).asString());
                            } else if (o1 instanceof PolynomialFunction) {
                                dStack.push(PolySupport.formatPoly((PolynomialFunction) o1));
                            } else if (o1 instanceof String) {
                                dStack.push(o1);
                            } else if (o1 instanceof StringSequence) {
                                dStack.push(((StringSequence) o1).asString());
                            } else if (o1 instanceof FileBlob) {
                                dStack.push(((FileBlob) o1).asString());
                            } else {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "length", "Get length of what is on the stack",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty()) {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof String) {
                                dStack.push((long) ((String) o1).length());
                            } else if (o1 instanceof DoubleSequence) {
                                dStack.push((long) ((DoubleSequence) o1).length());
                            } else if (o1 instanceof StringSequence) {
                                dStack.push((long) ((StringSequence) o1).length());
                            } else if (o1 instanceof FileBlob) {
                                dStack.push((long) ((FileBlob) o1).getSize());
                            } else {
                                String s = predefinedWords._jforth.makePrintable(o1);
                                dStack.push(s.length());
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "subSeq", "Subsequence of string or list",
                        (dStack, vStack) ->
                        {
                            if (dStack.size() < 3) {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            Object o3 = dStack.pop();
                            if ((o1 instanceof Long) && (o2 instanceof Long) && (o3 instanceof String)) {
                                int i1 = (int) ((Long) o1).longValue();
                                int i2 = (int) ((Long) o2).longValue();
                                dStack.push(((String) o3).substring(i2, i1));
                                return 1;
                            }
                            if ((o1 instanceof Long) && (o2 instanceof Long) && (o3 instanceof DoubleSequence)) {
                                int i1 = (int) ((Long) o1).longValue();
                                int i2 = (int) ((Long) o2).longValue();
                                DoubleSequence ds = (DoubleSequence) o3;
                                dStack.push(ds.subList(i2, i1));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "E", "Natural logarithm base",
                        (dStack, vStack) ->
                        {
                            dStack.push(Math.E);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "PI", "Circle constant PI",
                        (dStack, vStack) ->
                        {
                            dStack.push(Math.PI);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sqrt", "Square root",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.sqrt());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "gcd", "Greates common divisor",
                        (dStack, vStack) ->
                        {
                            try {
                                long o1 = Utilities.readLong(dStack);
                                long o2 = Utilities.readLong(dStack);
                                dStack.push(ArithmeticUtils.gcd(o1, o2));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "lcm", "Least common multiple",
                        (dStack, vStack) ->
                        {
                            try {
                                long o1 = Utilities.readLong(dStack);
                                long o2 = Utilities.readLong(dStack);
                                dStack.push(ArithmeticUtils.lcm(o1, o2));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "isPrime", "Primality test",
                        (dStack, vStack) ->
                        {
                            try {
                                long num = Utilities.readLong(dStack);
//                                boolean t = LongStream.rangeClosed(2, (long) Math.sqrt(num)).noneMatch(div -> num % div == 0);
                                DoubleSequence ds = DoubleSequence.primeFactors(num);
                                dStack.push(ds.length() == 1 ? JForth.TRUE : JForth.FALSE);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "factor", "Prime factorisation",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            try {
                                long o1 = Utilities.getLong(o);
                                dStack.push(DoubleSequence.primeFactors(o1));
                                return 1;
                            } catch (Exception ignored) {
                            }
                            try {
                                String ss = StringEscape.unescape((String) o);
                                String[] split = ss.split("\\s+");
                                dStack.push(new StringSequence(split));
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "pow", "Exponentation",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o1 = dStack.pop();
                                Object o2 = dStack.pop();
                                try {
                                    dStack.push(Utilities.doCalcBigInt(o2, o1, Utilities::pow));
                                    return 1;
                                } catch (Exception u) {
                                    try {
                                        dStack.push(Utilities.doCalcComplex(o2, o1, Complex::pow));
                                        return 1;
                                    } catch (Exception u2) {
                                        try {
                                            dStack.push(Utilities.pow((Fraction) o2, Utilities.getLong(o1)));
                                            return 1;
                                        } catch (Exception u3) {
                                            try {
                                                if (o1 instanceof Long && o2 instanceof Long) {
                                                    Long l1 = (Long) o1;
                                                    Long l2 = (Long) o2;
                                                    dStack.push(BigInteger.valueOf(l2).pow(l1.intValue()));
                                                    return 1;
                                                } else {
                                                    Double d1 = Utilities.getDouble(o1);
                                                    Double d2 = Utilities.getDouble(o2);
                                                    dStack.push(Math.pow(d2, d1));
                                                    return 1;
                                                }
                                            } catch (Exception e) {
                                                return 0;
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                //
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "fib", "Fibonacci number",
                        (dStack, vStack) ->
                        {
                            try {
                                long l = Utilities.readLong(dStack);
                                dStack.push(Utilities.fib(l));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "ln", "Natural logarithm",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex oc = Utilities.readComplex(dStack);
                                Complex erg = oc.log();
                                dStack.push(erg);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "fact", "Factorial",
                        (dStack, vStack) ->
                        {
                            try {
                                long ol = Utilities.readLong(dStack);
                                dStack.push(Utilities.factorial(ol));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "log10", "Logarithm to base 10",
                        (dStack, vStack) ->
                        {
                            try {
                                double d = Utilities.readDouble(dStack);
                                dStack.push(Math.log10(d));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "exp", "E^x",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.exp());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sin", "Sine",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.sin());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "gamma", "Gamma funcction",
                        (dStack, vStack) ->
                        {
                            try {
                                double o1 = Utilities.readDouble(dStack);
                                dStack.push(gamma(o1));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "zeta", "Riemann Zeta function",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex o1 = Utilities.readComplex(dStack);
                                double[] s = {o1.getReal(), o1.getImaginary()};
                                double[] z = zeta(s);
                                dStack.push(new Complex(z[0], z[1]));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "cos", "Cosine",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.cos());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "tan", "Tangent",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.tan());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "asin", "Inverse sine",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.asin());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "acos", "Inverse cosine",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.acos());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "atan", "Inverse tangent",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.atan());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "atan2", "Second arctan, see: https://de.wikipedia.org/wiki/Arctan2",
                        (dStack, vStack) ->
                        {
                            try {
                                double o1 = Utilities.readDouble(dStack);
                                double o2 = Utilities.readDouble(dStack);
                                dStack.push(Math.atan2(o2, o1));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sinh", "Sinus hyperbolicus",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.sinh());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "cosh", "Cosinus hyperbolicus",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.cosh());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "tanh", "Tangent hyperbolicus",
                        (dStack, vStack) ->
                        {
                            try {
                                Complex o1 = Utilities.readComplex(dStack);
                                dStack.push(o1.tanh());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "runFile", "run program file",
                        (dStack, vStack) ->
                        {
                            try {
                                String fileName = Utilities.readString(dStack);
                                predefinedWords._jforth.executeFile(fileName);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        PredefinedWords.SAVEHIST, false, "Save history",
                        (dStack, vStack) ->
                        {
                            try {
                                predefinedWords._jforth.history.save();
                            } catch (Exception ex) {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "loadHist", "Load history",
                        (dStack, vStack) ->
                        {
                            try {
                                predefinedWords._jforth.history.load();
                            } catch (Exception ex) {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        PredefinedWords.PLAYHIST, false, "Execute History",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth.play();
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "clearHist", "Clear History",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth.history.clear();
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "editor", "Enter line editor",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth._out.println("Type #h for help ...");
                            predefinedWords._jforth.mode = JForth.MODE.EDIT;
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "run", "Runs program in editor",
                        (dStack, vStack) ->
                        {
                            String s = predefinedWords._jforth._lineEditor.toString();
                            predefinedWords._jforth.interpretLine(s);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "list", "Put program in editor on stack",
                        (dStack, vStack) ->
                        {
                            dStack.push(predefinedWords._jforth._lineEditor.toString());
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "gaussian", "Gaussian random number",
                        (dStack, vStack) ->
                        {
                            try {
                                Object ob = dStack.pop();
                                long lo = Utilities.getLong(ob);
                                double number = predefinedWords._jforth.random.nextGaussian() * lo;
                                if (ob instanceof Double)
                                    dStack.push(number);
                                else
                                    dStack.push((long)number);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "random", "Pseudo random number",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o = dStack.pop();
                                if (o instanceof Long) {
                                    long rnd = predefinedWords._jforth.random.nextLong() * (Long) o;
                                    dStack.push(rnd);
                                } else {
                                    double number = predefinedWords._jforth.random.nextDouble() * (Double) o;
                                    dStack.push(number);
                                }
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "openByteReader", "Open file for reading",
                        (dStack, vStack) ->
                        {
                            try {
                                String str = Utilities.readString(dStack);
                                File f = new File(str);
                                dStack.push(new FileInputStream(f));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "readByte", "Read byte from file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty()) {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof FileInputStream) {
                                try {
                                    dStack.push((long) (((FileInputStream) o1).read()));
                                    return 1;
                                } catch (IOException ioe) {
                                    ioe.printStackTrace();
                                }
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "dir", "Get directory",
                        (dStack, vStack) ->
                        {
                            String path;
                            try {
                                path = Utilities.readString(dStack);
                            } catch (Exception e) {
                                path = ".";
                            }
                            dStack.push(FileUtils.dir(path));
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "unlink", "Delete file",
                        (dStack, vStack) ->
                        {
                            String o;
                            try {
                                o = Utilities.readString(dStack);
                            } catch (Exception e) {
                                return 0;
                            }
                            return FileUtils.del(o) ? 1 : 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "key", true, "Get key from keyboard",
                        (dStack, vStack) ->
                        {
                            try {
                                int c = RawConsoleInput.read(true);
                                RawConsoleInput.resetConsoleMode();
                                dStack.push((long) c);
                                return 1;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "clear", true, "Clear the stack",
                        (dStack, vStack) ->
                        {
                            dStack.clear();
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "pick", true, "Get value from arbitrary Positon and place it on TOS",
                        (dStack, vStack) ->
                        {
                            try {
                                long o = Utilities.readLong(dStack);
                                Object n = dStack.get2(dStack.size() - ((Long) o).intValue() - 1);
                                if (n == null) {
                                    return 0;
                                }
                                dStack.push(n);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "roll", "Remove nth element and put it on TOS",
                        (dStack, vStack) ->
                        {
                            try {
                                long o = Utilities.readLong(dStack);
                                Object n = dStack.remove(dStack.size() - ((Long) o).intValue() - 1);
                                if (n == null) {
                                    return 0;
                                }
                                dStack.push(n);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "accept", true, "Read string from keyboard",
                        (dStack, vStack) ->
                        {
                            long l;
                            try {
                                l = Utilities.readLong(dStack);
                            } catch (Exception e) {
                                l = -1;
                            }
                            StringBuilder s = new StringBuilder();
                            try {
                                while (true) {
                                    char c = (char) RawConsoleInput.read(true);
                                    if (l > 0) {
                                        l--;
                                    }
                                    if (c == '\r') {
                                        break;
                                    }
                                    s.append(c);
                                    if (l == 0) {
                                        break;
                                    }
                                    predefinedWords._jforth._out.print('-');
                                    predefinedWords._jforth._out.flush();
                                }
                                RawConsoleInput.resetConsoleMode();
                                dStack.push(s.toString());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

//        _fw.add(new PrimitiveWord
//                (
//                        "tick", "Get clock value",
//                        (dStack, vStack) ->
//                        {
//                            long n = System.currentTimeMillis();
//                            dStack.push(n);
//                            return 1;
//                        }
//                ));

        _fw.add(new PrimitiveWord
                (
                        "closeByteReader", "Close file",
                        (dStack, vStack) ->
                        {
                            try {
                                FileInputStream fi = Utilities.readFileInputStream(dStack);
                                fi.close();
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "openReader", "Open file",
                        (dStack, vStack) ->
                        {
                            try {
                                String o1 = Utilities.readString(dStack);   // file name
                                File f = new File(o1);
                                dStack.push(new BufferedReader(new FileReader(f)));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "udpput", "Send udp packet",
                        (dStack, vStack) ->
                        {
                            try {
                                String data = Utilities.readString(dStack);
                                byte[] bt = data.getBytes();
                                int port = (int) Utilities.readLong(dStack);
                                String ip;
                                if (dStack.empty())
                                    ip = "255.255.255.255";
                                else
                                    ip = Utilities.readString(dStack);
                                DatagramPacket pkt = new DatagramPacket(bt, bt.length,
                                        InetAddress.getByName(ip), port);
                                DatagramSocket sock = new DatagramSocket();
                                sock.setBroadcast(true);
                                sock.send(pkt);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "udpget", "Receive udp packet",
                        (dStack, vStack) ->
                        {
                            try {
                                int port = (int) Utilities.readLong(dStack);
                                DatagramPacket packet = new DatagramPacket(new byte[1500], 1500);
                                DatagramSocket sock = new DatagramSocket(port);
                                sock.setBroadcast(true);
                                sock.receive(packet);
                                byte[] dat = new byte[packet.getLength()];
                                System.arraycopy(packet.getData(), 0, dat, 0, packet.getLength());
                                String s = new String(dat);
                                sock.close();
                                dStack.push(s);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "readLine", "Read line from file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty()) {
                                return 0;
                            }
                            Object o1 = dStack.peek();
                            if (o1 instanceof BufferedReader) {
                                try {
                                    String s = ((BufferedReader) o1).readLine();
                                    if (s == null)
                                        dStack.push("*EOF*");
                                    else
                                        dStack.push(s);
                                    return 1;
                                } catch (IOException ioe) {
                                    //ioe.printStackTrace();
                                    return 0;
                                }
                            } else {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "closeReader", "Close file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty()) {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof BufferedReader) {
                                try {
                                    ((BufferedReader) o1).close();
                                    return 1;
                                } catch (IOException ioe) {
                                    ioe.printStackTrace();
                                    return 0;
                                }
                            } else {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "openWriter", "Open file for Writing",
                        (dStack, vStack) ->
                        {
                            try {
                                String fname = Utilities.readString(dStack);
                                dStack.push(new PrintStream(fname));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "writeString", "Write string to file",
                        (dStack, vStack) ->
                        {
                            try {
                                String o2 = Utilities.readString(dStack);
                                Object o1 = dStack.peek();
                                ((PrintStream) o1).print(o2);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "writeEol", "Write string end into file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty()) {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof PrintStream) {
                                ((PrintStream) o1).println();
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "writeByte", "Write byte into file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty()) {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            if ((o1 instanceof PrintStream) && (o2 instanceof Long)) {
                                ((PrintStream) o1).write((byte) (((Long) o2).longValue()));
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "closeWriter", "Close file",
                        (dStack, vStack) ->
                        {
                            if (dStack.empty()) {
                                return 0;
                            }
                            Object o1 = dStack.pop();
                            if (o1 instanceof PrintStream) {
                                ((PrintStream) o1).close();
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "bye", "End the Forth interpreter",
                        (dStack, vStack) ->
                        {
                            predefinedWords._jforth._out.println("JForth will close now!");
                            predefinedWords._jforth._out.flush();
                            Utilities.terminateSoon(1000);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sort", "Sort a Sequence",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o = dStack.pop();
                                if (o instanceof String) {
                                    String s = (String) o;
                                    dStack.push(Utilities.sort(s));
                                    return 1;
                                }
                                if (o instanceof SequenceBase) {
                                    SequenceBase od = (SequenceBase) o;
                                    dStack.push(od.sort());
                                    return 1;
                                }
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "rev", "Reverse a sequence",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o = dStack.pop();
                                if (o instanceof String) {
                                    String s = (String) o;
                                    s = new StringBuilder(s).reverse().toString();
                                    dStack.push(s);
                                    return 1;
                                }
                                if (o instanceof SequenceBase) {
                                    SequenceBase od = (SequenceBase) o;
                                    dStack.push(od.reverse());
                                    return 1;
                                }
                                if (o instanceof FileBlob) {
                                    FileBlob fb = new FileBlob(Utilities.reverse(((FileBlob) o).get_content()),
                                            ((FileBlob) o).getPath());
                                    dStack.push(fb);
                                    return 1;
                                }
                                if (o instanceof Fraction) {
                                    Fraction p = (Fraction) o;
                                    Fraction p2 = p.reciprocal();
                                    dStack.push(p2);
                                    return 1;
                                }
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "shuffle", "Random shuffle a sequence",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o = dStack.pop();
                                if (o instanceof String) {
                                    String s = (String) o;
                                    dStack.push(Utilities.shuffle(s));
                                    return 1;
                                }
                                if (o instanceof SequenceBase) {
                                    SequenceBase o2 = (SequenceBase) o;
                                    dStack.push(o2.shuffle());
                                    return 1;
                                }
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sum", "Add all elements together",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence o = Utilities.readDoubleSequence(dStack);
                                Double sum = o.sum();
                                if (sum.longValue() == sum)
                                    dStack.push(sum.longValue());
                                else
                                    dStack.push(sum);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "altsum", "Add all elements together but alternates sign",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence o = Utilities.readDoubleSequence(dStack);
                                Double sum = o.altsum();
                                if (sum.longValue() == sum)
                                    dStack.push(sum.longValue());
                                else
                                    dStack.push(sum);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sumq", "Make sum of squares",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence o = Utilities.readDoubleSequence(dStack);
                                dStack.push(o.sumQ());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "prod", "Product of all values",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence o = Utilities.readDoubleSequence(dStack);
                                dStack.push(o.prod());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "intersect", "Make intersection of 2 sequences",
                        (dStack, vStack) ->
                        {
                            boolean str = false;
                            Object o1 = dStack.pop();
                            Object o2 = dStack.pop();
                            try {
                                if (o1 instanceof String) {
                                    o1 = new DoubleSequence((String) o1);
                                    o2 = new DoubleSequence((String) o2);
                                    str = true;
                                }
                                SequenceBase oo1 = (SequenceBase) o1;
                                SequenceBase oo2 = (SequenceBase) o2;
                                SequenceBase result = oo1.intersect(oo2);
                                if (str)
                                    dStack.push(result.asStringX());
                                else
                                    dStack.push(result);
                                return 1;
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "unique", "Only keep unique elements of sequence",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o = dStack.pop();
                                if (o instanceof String) {
                                    String s = (String) o;
                                    s = Utilities.unique(s);
                                    dStack.push(s);
                                    return 1;
                                }
                                if (o instanceof SequenceBase) {
                                    SequenceBase od = (SequenceBase) o;
                                    dStack.push(od.unique());
                                    return 1;
                                }
                            } catch (Exception ignored) {
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "lpick", "Get one Element from sequence",
                        (dStack, vStack) ->
                        {
                            try {
                                long l1 = Utilities.readLong(dStack);
                                Object o = dStack.pop();
                                if (o instanceof DoubleSequence) {
                                    dStack.push(((DoubleSequence) o).pick((int) l1));
                                } else if (o instanceof StringSequence) {
                                    dStack.push(((StringSequence) o).pick((int) l1));
                                } else if (o instanceof String) {
                                    char c = ((String) o).charAt((int) l1);
                                    dStack.push("" + c);
                                } else
                                    return 0;
                                return 1;
                            } catch (Exception ex) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "http", "run web server",
                        (dStack, vStack) ->
                        {
                            try {
                                long o = Utilities.readLong(dStack);
                                SimpleWebserver.start((int) o);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "unhttp", "stop web server",
                        (dStack, vStack) ->
                        {
                            try {
                                SimpleWebserver.stop();
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));


    }
}
