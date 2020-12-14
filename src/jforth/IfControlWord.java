package jforth;

import java.util.Objects;

public final class IfControlWord extends BaseWord {
    public IfControlWord(int currentIndex) {
        super("", false, null);
        this.currentIndex = currentIndex;
        thenIndexIncrement = 0;
        elseIndexIncrement = 0;
    }

    public void setThenIndex(int thenIndex) {
        thenIndexIncrement = thenIndex - currentIndex;
    }

    public void setElseIndex(int elseIndex) {
        elseIndexIncrement = elseIndex - currentIndex;
    }

    public int execute(OStack dStack, OStack vStack) {
        if (dStack.empty())
            return 0;
        Object o = dStack.pop();
        if (o instanceof Long) {
            if (Objects.equals(o, JForth.TRUE)) {
                return 1;
            } else {
                if (elseIndexIncrement != 0)
                    return elseIndexIncrement;
                else
                    return thenIndexIncrement;
            }
        } else
            return 0;
    }

    private final int currentIndex;
    private int thenIndexIncrement;
    private int elseIndexIncrement;
}
