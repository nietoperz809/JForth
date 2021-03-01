package jforth;

import jforth.forthwords.PredefinedWords;
import jforth.forthwords.WordHelpers;

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

    public int plusStore(Object data, int offset, PredefinedWords pred) {
        if ((offset < 0) || (offset >= size))
            return 0;
        Object o1 = array[offset];
        OStack stack = new OStack();
        WordHelpers.add(stack, data, o1, pred);
        array[offset] = stack.pop();
        return 1;
    }

    private final int size;
    private final Object[] array;
    private final boolean isArray;
}
