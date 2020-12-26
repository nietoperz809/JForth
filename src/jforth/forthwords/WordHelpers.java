package jforth.forthwords;

import jforth.*;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fraction.Fraction;

import java.math.BigInteger;
import java.util.List;

class WordHelpers {
    static int addLoopWord(OStack vStack, PredefinedWords predefs, Class<? extends LoopControlWord> clazz) {
        try {
            int beginIndex = (int) Utilities.readLong(vStack);
            int endIndex = predefs._jforth.wordBeingDefined.getNextWordIndex();
            int increment = beginIndex - endIndex;
            LoopControlWord lcw = clazz.getConstructor(Integer.class).newInstance(increment);
            predefs._jforth.wordBeingDefined.addWord(lcw);
            predefs.executeTemporaryImmediateWord();
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    static int add(OStack dStack, Object o1, Object o2, PredefinedWords pred) {
        try {
            dStack.push(Utilities.doCalcMatrix(o2, o1, Utilities::add));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcBigInt(o2, o1, BigInteger::add));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcComplex(o2, o1, Complex::add));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcFraction(o2, o1, Fraction::add));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(PolySupport.execute(o2, o1, PolynomialFunction::add));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcDouble(o2, o1, Utilities::add));
            return 1;
        } catch (Exception ignored) {
        }
        if (o2 instanceof FileBlob) // Fileblob handling
        {
            if (o1 instanceof FileBlob) {
                ((FileBlob) o2).append((FileBlob) o1);
            } else if (o1 instanceof String) {
                ((FileBlob) o2).append((String) o1);
            } else {
                return 0;
            }
            dStack.push(o2);
            return 1;
        } else if ((o1 instanceof Long) && (o2 instanceof Long)) {
            long i1 = (Long) o1;
            long i2 = (Long) o2;
            i2 += i1;
            dStack.push(i2);
        } else if ((o1 instanceof DoubleSequence) && (o2 instanceof DoubleSequence)) {
            DoubleSequence s = new DoubleSequence((DoubleSequence) o2, (DoubleSequence) o1);
            dStack.push(s);
        } else if ((o1 instanceof Double) && (o2 instanceof DoubleSequence)) {
            Double d1 = (Double) o1;
            DoubleSequence d2 = (DoubleSequence) o2;
            d2.add(d1);
            dStack.push(d2);
        } else if ((o1 instanceof Long) && (o2 instanceof DoubleSequence)) {
            Long d1 = (Long) o1;
            DoubleSequence d2 = (DoubleSequence) o2;
            d2.add(d1.doubleValue());
            dStack.push(d2);
        } else if (o2 instanceof StringSequence) {
            StringSequence ss = (StringSequence) o2;
            ss.add(pred._jforth.makePrintable(o1));
            dStack.push(ss);
        } else if (o1 instanceof StringSequence) {
            StringSequence ss = (StringSequence) o1;
            ss.put(0, (pred._jforth.makePrintable(o2)));
            dStack.push(ss);
        } else if (o2 instanceof String) {
            String s = o2 + pred._jforth.makePrintable(o1);
            dStack.push(s);
        } else if (o1 instanceof String) {
            String s = pred._jforth.makePrintable(o2) + o1;
            dStack.push(s);
        } else {
            return 0;
        }
        return 1;
    }

    static int divGF(OStack dStack, Object o1, Object o2) {
        if ((o1 instanceof Long) && (o2 instanceof Long)) {
            int i1 = ((Long) o1).intValue();
            int i2 = ((Long) o2).intValue();
            dStack.push(GaloisField256.Quotient(i2, i1)); // i2 / i1;
            return 1;
        }
        return 0;
    }

    static int addGF(OStack dStack, Object o1, Object o2) {
        if ((o1 instanceof Long) && (o2 instanceof Long)) {
            int i1 = ((Long) o1).intValue();
            int i2 = ((Long) o2).intValue();
            dStack.push(GaloisField256.Sum(i2, i1)); // i2 / i1;
            return 1;
        }
        return 0;
    }

    static int subGF(OStack dStack, Object o1, Object o2) {
        if ((o1 instanceof Long) && (o2 instanceof Long)) {
            int i1 = ((Long) o1).intValue();
            int i2 = ((Long) o2).intValue();
            dStack.push(GaloisField256.Difference(i2, i1)); // i2 / i1;
            return 1;
        }
        return 0;
    }

    static int multGF(OStack dStack, Object o1, Object o2) {
        if ((o1 instanceof Long) && (o2 instanceof Long)) {
            int i1 = ((Long) o1).intValue();
            int i2 = ((Long) o2).intValue();
            dStack.push(GaloisField256.Product(i2, i1)); // i2 / i1;
            return 1;
        }
        return 0;
    }

    static int div(OStack dStack, Object o1, Object o2) {
        try {
            dStack.push(Utilities.doCalcMatrix(o2, o1, Utilities::div));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcBigInt(o2, o1, BigInteger::divide));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcComplex(o2, o1, Complex::divide));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcFraction(o2, o1, Fraction::divide));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(PolySupport.execute(o2, o1, PolySupport::polyDiv));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcDouble(o2, o1, Utilities::div));
            return 1;
        } catch (Exception ignored) {
        }
        if ((o1 instanceof Long) && (o2 instanceof Long)) {
            long i1 = (Long) o1;
            long i2 = (Long) o2;
            i2 /= i1;
            dStack.push(i2);
        } else if ((o1 instanceof Long) && (o2 instanceof String)) {
            long d1 = (Long) o1;
            String d2 = (String) o2;
            List<String> ll = Utilities.splitEqually(d2, (int) d1);
            if (ll == null) {
                return 0;
            }
            StringSequence sl = new StringSequence(ll);
            dStack.push(sl);
//            for (String s : ll)
//            {
//                dStack.push(s);
//            }
        } else if ((o1 instanceof Long) && (o2 instanceof DoubleSequence)) {
            long d1 = (Long) o1;
            DoubleSequence d2 = (DoubleSequence) o2;
            List<DoubleSequence> ll = Utilities.splitEqually(d2, (int) d1);
            if (ll == null) {
                return 0;
            }
            for (DoubleSequence s : ll) {
                dStack.push(s);
            }
        } else {
            return 0;
        }
        return 1;
    }

    static void dup(Object o, OStack dStack) {
        dStack.push(Utilities.deepCopy(o));
    }

    static int mult(OStack dStack, Object o1, Object o2) {
        try {
            dStack.push(Utilities.doCalcMatrix(o2, o1, Utilities::mult));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcBigInt(o2, o1, BigInteger::multiply));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcComplex(o2, o1, Complex::multiply));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcFraction(o2, o1, Fraction::multiply));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(PolySupport.execute(o2, o1, PolynomialFunction::multiply));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcDouble(o2, o1, Utilities::mult));
            return 1;
        } catch (Exception ignored) {
        }
        if (o2 instanceof Long) {
            Object tmp = o2;
            o2 = o1;
            o1 = tmp;
        }
        if (o1 instanceof Long) {
            long i1 = (Long) o1;
            if (o2 instanceof Long) {
                long i2 = (Long) o2;
                dStack.push(i2 * i1);
                return 1;
            } else if (o2 instanceof DoubleSequence) {
                DoubleSequence d2 = (DoubleSequence) o2;
                DoubleSequence d3 = new DoubleSequence();  // empty
                while (i1-- != 0) {
                    d3 = d3.add(d2);
                }
                dStack.push(d3);
                return 1;
            } else if (o2 instanceof String) {
                String d2 = (String) o2;
                StringBuilder sb = new StringBuilder();  // empty
                while (i1-- != 0) {
                    sb.append(d2);
                }
                dStack.push(sb.toString());
                return 1;
            }
        }
        return 0;
    }

    static int sub(OStack dStack, Object o1, Object o2) {
        try {
            dStack.push(Utilities.doCalcMatrix(o2, o1, Utilities::sub));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcBigInt(o2, o1, BigInteger::subtract));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcComplex(o2, o1, Complex::subtract));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcFraction(o2, o1, Fraction::subtract));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(PolySupport.execute(o2, o1, PolynomialFunction::subtract));
            return 1;
        } catch (Exception ignored) {
        }
        try {
            dStack.push(Utilities.doCalcDouble(o2, o1, Utilities::sub));
            return 1;
        } catch (Exception ignored) {
        }
        if (o1 instanceof Long && o2 instanceof Long) {
            long i1 = (Long) o1;
            long i2 = (Long) o2;
            i2 -= i1;
            dStack.push(i2);
        } else if (o1 instanceof DoubleSequence && o2 instanceof DoubleSequence) {
            DoubleSequence d1 = (DoubleSequence) o1;
            DoubleSequence d2 = (DoubleSequence) o2;
            dStack.push(d2.difference(d1));
        } else if (o1 instanceof StringSequence && o2 instanceof StringSequence) {
            StringSequence d1 = (StringSequence) o1;
            StringSequence d2 = (StringSequence) o2;
            dStack.push(d2.difference(d1));
        } else if (o1 instanceof Long && o2 instanceof DoubleSequence) {
            Long d1 = (Long) o1;
            DoubleSequence d2 = (DoubleSequence) o2;
            dStack.push(d2.subList(0, d2.length() - d1.intValue()));
        } else if (o1 instanceof Long && o2 instanceof StringSequence) {
            Long d1 = (Long) o1;
            StringSequence d2 = (StringSequence) o2;
            dStack.push(d2.subList(0, d2.length() - d1.intValue()));
        } else if (o2 instanceof String && o1 instanceof String) {
            StringSequence ss2 = new StringSequence(((String) o2).toCharArray());
            StringSequence ss1 = new StringSequence(((String) o1).toCharArray());
            dStack.push(ss2.difference(ss1).asString());
        } else if (o2 instanceof String && o1 instanceof Long) {
            String s = (String) o2;
            int l = ((Long) o1).intValue();
            dStack.push(s.substring(0, s.length() - l));
        } else if (o2 instanceof FileBlob && o1 instanceof Long) {
            FileBlob s = (FileBlob) o2;
            int len = s.getSize() - ((Long) o1).intValue();
            byte[] nc = new byte[len];
            System.arraycopy(s.get_content(), 0, nc, 0, len);
            FileBlob ncb = new FileBlob(nc, s.getPath());
            dStack.push(ncb);
        } else {
            return 0;
        }
        return 1;
    }

    static BaseWord toLiteral(Object o1) {
        return new Literal(o1);
    }
}
