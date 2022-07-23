package jforth.forthwords;

import jforth.PrimitiveWord;
import jforth.WordsList;
import tools.Utilities;

import java.math.BigInteger;

import static jforth.PositionalNumberSystem.getPnsInst;

final class Filler3 {

    static void fill(WordsList _fw, PredefinedWords predefinedWords) {

        _fw.add(new PrimitiveWord
                (
                        "pns-init", "initialize positional number converter",
                        (dStack, vStack) ->
                        {
                            try {
                                String s = Utilities.readString(dStack);
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
                                String s = Utilities.readString(dStack);
                                BigInteger bi = getPnsInst().toNumber(s);
                                dStack.push(bi);
                                return 1;
                            } catch (Exception e) {
                                return 0;
                            }
                        }
                ));


    }
}
