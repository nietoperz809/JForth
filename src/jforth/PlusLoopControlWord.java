package jforth;

public final class PlusLoopControlWord extends LoopControlWord {
    public PlusLoopControlWord(Integer indexIncrement) {
        super(indexIncrement);
    }

    public Integer apply(OStack dStack, OStack vStack) {
        try {
            long index = Utilities.readLong(vStack);
            long limit = Utilities.peekLong(vStack);
            long inc = Utilities.readLong(dStack);
            index += inc;
            boolean condition = (inc >= 0) == (index >= limit);
            if (condition) {
                vStack.pop();
                return 1;
            } else {
                vStack.push(index);
                return indexIncrement;
            }
        } catch (Exception e) {
            return 0;
        }
    }
}
