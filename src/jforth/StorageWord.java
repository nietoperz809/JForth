package jforth;

import jforth.forthwords.PredefinedWords;
import jforth.forthwords.WordHelpers;
import jforth.seq.DoubleSequence;
import tools.Utilities;

public final class StorageWord extends BaseWord {
    public StorageWord(String name, int size, boolean isArray) {
        super(name, false, null);
        this.size = size;
        array = new Object[size];
        this.isArray = isArray;
    }

    public DoubleSequence asDoubleSequence()
    {
        DoubleSequence ds = new DoubleSequence();
        for (Object o : array)
        {
            try {
                Double dd = Utilities.getDouble(o);
                ds._list.add(dd);
            } catch (Exception e) {
                ds._list.add(0.0);
            }
        }
        return ds;
    }

    public Integer apply(OStack dStack, OStack vStack) {
        dStack.push(this);
        return 1;
    }

    public boolean isNotArray() {
        return !isArray;
    }

    public Object fetch(int offset) {
        if ((offset < 0) || (offset >= size))
            return null;
        return array[offset] == null ? 0 : array[offset];
    }

    public int store(Object data, int offset) {
        if ((offset < 0) || (offset >= size))
            return 0;
        array[offset] = data;
        return 1;
    }

    /**
     * add value to variable
     * @param data value
     * @param offset variable
     * @param pred list of predef words
     * @return 1=ok, 0= failure
     */
    public int plusStore(Object data, int offset, PredefinedWords pred) {
        if ((offset < 0) || (offset >= size))
            return 0;
        Object o1 = array[offset];
        OStack stack = new OStack();
        WordHelpers.add(stack, data, o1, pred);
        array[offset] = stack.pop();
        return 1;
    }

    public int minusStore(Object data, int offset) {
        if ((offset < 0) || (offset >= size))
            return 0;
        Object o1 = array[offset];
        OStack stack = new OStack();
        WordHelpers.sub(stack, data, o1);
        array[offset] = stack.pop();
        return 1;
    }

    static public int helper1 (StorageWord sw, OStack dStack) throws Exception {
        if (sw.isNotArray()) {
            if (dStack.empty()) {
                throw new Exception("Empty stack");
            }
        } else {
            if (dStack.size() < 2) {
                throw new Exception("Too few args on stack");
            }
            Object off = dStack.pop();
            if (!(off instanceof Long)) {
                throw new Exception("not a Long type");
            }
            return (int) ((Long) off).longValue();
        }
        return 0;
    }

    private final int size;
    private final Object[] array;
    private final boolean isArray;
}
