package jforth.forthwords;

import jforth.PrimitiveWord;
import jforth.WordsList;
import jforth.seq.DoubleSequence;
import org.apache.commons.math3.analysis.function.HarmonicOscillator;
import org.apache.commons.math3.analysis.function.Signum;
import org.apache.commons.math3.complex.Complex;
import tools.ClipBoard;
import tools.Haar;
import tools.Utilities;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.zip.Deflater;

import static jforth.PositionalNumberSystem.getPnsInst;
import static tools.Utilities.*;

final class Filler3 {
    static void fill(WordsList _fw, PredefinedWords predefinedWords) {

        _fw.add(new PrimitiveWord
                (
                        "pns-init", "initialize positional number converter",
                        (dStack, vStack) ->
                        {
                            try {
                                String s = readString(dStack);
                                getPnsInst().newInstance(s);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "pns-do", "convert decimal to arbitrary positional number",
                        (dStack, vStack) ->
                        {
                            try {
                                BigInteger bi = Utilities.readBig(dStack);
                                String s = getPnsInst().toString(bi);
                                dStack.push(s);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "pns-undo", "convert psn string back to number",
                        (dStack, vStack) ->
                        {
                            try {
                                String s = readString(dStack);
                                BigInteger bi = getPnsInst().toNumber(s);
                                dStack.push(bi);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "harmonic", "create harmonic oscillating seq",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence parms = Utilities.readDoubleSequence(dStack);
                                DoubleSequence in = Utilities.readDoubleSequence(dStack);
                                HarmonicOscillator ha = new HarmonicOscillator(parms.pick(0),
                                        parms.pick(1), parms.pick(2));
                                DoubleSequence out = new DoubleSequence();
                                for (double d : in.asPrimitiveArray()) {
                                    out.get_list().add(ha.value(d));
                                }
                                dStack.push(out);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sign", "signum of sequence or singe value",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o = dStack.pop();
                                if (o instanceof DoubleSequence) {
                                    DoubleSequence in = (DoubleSequence) o;
                                    Signum si = new Signum();
                                    DoubleSequence out = new DoubleSequence();
                                    for (double d : in.asPrimitiveArray()) {
                                        out.get_list().add(si.value(d));
                                    }
                                    dStack.push(out);
                                    return 1;
                                } else if (o instanceof Double) {
                                    dStack.push(Math.signum((Double) o));
                                    return 1;
                                } else if (o instanceof Long) {
                                    dStack.push((long) Math.signum((Long) o));
                                    return 1;
                                }

                                return 0;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

//        _fw.add(new PrimitiveWord
//                (
//                        "sign", "signum of sequence or singe value",
//                        (dStack, vStack) ->
//                        {
//                            try {
//                                Object o = dStack.pop();
//                                if (o instanceof DoubleSequence) {
//                                    DoubleSequence in = (DoubleSequence)o;
//                                    Signum si = new Signum();
//                                    DoubleSequence out = new DoubleSequence();
//                                    for (double d : in.asPrimitiveArray()) {
//                                        out.get_list().add(si.value(d));
//                                    }
//                                    dStack.push(out);
//                                    return 1;
//                                }
//                            } catch (Exception e) {
//                                return 0;
//                            }
//                        }
//                ));

        _fw.add(new PrimitiveWord
                (
                        "gclip", "put clipboard data on stack",
                        (dStack, vStack) ->
                        {
                            try {
                                dStack.push(ClipBoard.get());
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sclip", "send TOS to clipboard",
                        (dStack, vStack) ->
                        {
                            try {
                                ClipBoard.put(readString(dStack));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "haar", "haar wavelet transform",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence ds = readDoubleSequence(dStack);
                                double[] prim = ds.asPrimitiveArray();
                                Haar.Forward(prim);
                                dStack.push (new DoubleSequence(prim));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "unhaar", "backward haar wavelet transform",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence ds = readDoubleSequence(dStack);
                                double[] prim = ds.asPrimitiveArray();
                                Haar.Backward(prim);
                                dStack.push (new DoubleSequence(prim));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "zip", "compress",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence ds = readDoubleSequence(dStack);
                                byte[] bt = ds.asBytes();
                                byte[] zipped = compress(bt,
                                        Deflater.BEST_COMPRESSION, false);
                                dStack.push (new DoubleSequence(zipped));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "unzip", "decompress",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence ds = readDoubleSequence(dStack);
                                byte[] bt = ds.asBytes();
                                byte[] unzipped = decompress(bt, false);
                                dStack.push (new DoubleSequence(unzipped));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "neg", "negate",
                        (dStack, vStack) ->
                        {
                            try {
                                Object o = dStack.pop();
                                if (o instanceof DoubleSequence) {
                                    DoubleSequence x =((DoubleSequence)o).neg();
                                    dStack.push(x);
                                    return 1;
                                }
                                if (o instanceof Complex) {
                                    Complex c = ((Complex)o).pow(-1);
                                    dStack.push(c);
                                    return 1;
                                }
                                Double d = (Double)o;
                                dStack.push (-d);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "rndSeq", "get Sequence of random numbers",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence ds = readDoubleSequence(dStack);
                                DoubleSequence out = DoubleSequence.randomInts(ds.iPick(0),ds.iPick(1),ds.iPick(2));
                                dStack.push (out);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "rndSeqD", "get Sequence of random double numbers",
                        (dStack, vStack) ->
                        {
                            try {
                                DoubleSequence ds = readDoubleSequence(dStack);
                                DoubleSequence out = DoubleSequence.randomDoubles(ds.pick(0),ds.pick(1),ds.iPick(2));
                                dStack.push (out);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "ipaddr", "get ip address of domain",
                        (dStack, vStack) ->
                        {
                            try {
                                String domain = readString(dStack);
                                InetAddress i = InetAddress.getByName(domain);
                                dStack.push (new DoubleSequence(i.getAddress()));
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));

    }
}
