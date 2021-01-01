package jforth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

public class SequenceBase<E extends Comparable<E>> implements Cloneable, java.io.Serializable {
    protected ArrayList<E> _list = new ArrayList<> ();

    public ArrayList<E> get_list ()
    {
        return _list;
    }

    private SequenceBase<? extends Comparable<?>> makeInstance (ArrayList<E> list)
    {
        if (this instanceof StringSequence)
        {
            return new StringSequence((ArrayList<String>)list);
        }
        if (this instanceof DoubleSequence)
        {
            return new DoubleSequence((ArrayList<Double>)list);
        }
        return null;
    }

    public String asStringX()
    {
        if (this instanceof StringSequence)
        {
            return ((StringSequence)this).asString();
        }
        if (this instanceof DoubleSequence)
        {
            return ((DoubleSequence)this).asString();
        }
        return null;
    }

    public SequenceBase ()
    {
    }

    public SequenceBase<? extends Comparable<?>> rearrange (int pos[])
    {
        ArrayList<E> list = rearrange (pos, _list);
        return makeInstance(list);
    }

    public SequenceBase<? extends Comparable<?>> swap (int a, int b)
    {
        return makeInstance (swap (_list, a, b));
    }

    public SequenceBase<? extends Comparable<?>> rotateLeft (int n)
    {
        return makeInstance (rotateLeft (_list, n));
    }

    public SequenceBase<? extends Comparable<?>> rotateRight (int n)
    {
        return makeInstance(rotateRight (_list, n));
    }

    public SequenceBase<? extends Comparable<?>> reverse()
    {
        ArrayList<E> list2 = new ArrayList<>(_list);
        Collections.reverse (list2);
        return makeInstance(list2);
    }

    public SequenceBase<? extends Comparable<?>> shuffle ()
    {
        ArrayList<E> list2 = new ArrayList<>(_list);
        Collections.shuffle (list2);
        return makeInstance(list2);
    }

    public SequenceBase<? extends Comparable<?>> sort ()
    {
        ArrayList<E> list2 = new ArrayList<>(_list);
        Collections.sort (list2);
        return makeInstance(list2);
    }

    public SequenceBase<? extends Comparable<?>> intersect (SequenceBase<E> other)
    {
        ArrayList<E> ret = new ArrayList<>(_list);
        ret.retainAll(other._list);
        return makeInstance(ret);
    }

    public SequenceBase<? extends Comparable<?>> difference (SequenceBase<E> other)
    {
        ArrayList<E> ret = new ArrayList<>(_list);
        ret.removeAll(other._list);
        return makeInstance(ret);
    }

    public SequenceBase<? extends Comparable<?>> unique ()
    {
        return makeInstance(new ArrayList<>(new LinkedHashSet<>(_list)));
    }

    public SequenceBase<? extends Comparable<?>> subList (int from, int to)
    {
        return makeInstance((ArrayList<E>) _list.subList(from, to));
    }

    public E pick (int i)
    {
        return _list.get(i);
    }

    public void add (E s)
    {
        _list.add (s);
    }

    public void put (int x, E s)
    {
        _list.add (x, s);
    }

    public int length()
    {
        return _list.size();
    }

    public boolean isEmpty()
    {
        return _list.isEmpty();
    }

    public boolean equals (Object other)
    {
        SequenceBase<E> o2 = (SequenceBase<E>) other;
        if (this._list.size () != o2._list.size ())
            return false;
        ArrayList<?> test = (ArrayList<?>)_list.clone ();
        test.removeAll (o2._list);
        return test.size() == 0;
    }

//////////////////////////////////////////////////////////////////////////////////////

    public static <E> ArrayList<E> mixin (SequenceBase<? extends E> d1, SequenceBase<? extends E> d2)
    {
        int len = Math.max (d1.length(),d2.length());
        ArrayList<E> ar = new ArrayList<>();
        for (int s=0; s<len; s++)
        {
            if (s < d1.length())
                ar.add((E)d1._list.get(s));
            if (s < d2.length())
                ar.add ((E)d2._list.get(s));
        }
        return ar;
    }

    /**
     * rearrange members of an arraylist
     * @param pos array with new positions
     * @param in the arraylist
     * @param <E> type of objects in that list
     * @return a new rearranged arraylist
     */
    public static <E> ArrayList<E> rearrange (int[] pos, ArrayList<E> in)
    {
        ArrayList<E> out = new ArrayList<> ();
        for (int p : pos)
        {
            out.add (in.get (p));
        }
        return out;
    }

    /**
     * swap 2 members of an arraylist
     * @param in the list
     * @param a position a
     * @param b position b
     * @param <E> type of objects in the list
     * @return an arraylist with 2 members swapped
     */
    public static <E> ArrayList<E> swap (ArrayList<E> in, int a, int b)
    {
        ArrayList<E> out = new ArrayList<> (in);
        E x = out.get (a);
        out.set (a, out.get (b));
        out.set (b, x);
        return out;
    }

    /**
     * rotate an arraylist left
     * @param list the array list
     * @param n number of rotations
     * @param <E> type of objects in that list
     * @return rotated list
     */
    public static <E> ArrayList<E> rotateLeft (ArrayList<E> list, int n)
    {
        return rotateRight (list, list.size ()-n);
    }

    /**
     * rotate an arraylist right
     * @param list the array list
     * @param n number of rotations
     * @param <E> type of objects in that list
     * @return rotated list
     */
    public static <E> ArrayList<E> rotateRight (ArrayList<E> list, int n)
    {
        ArrayList<E> ret = new ArrayList<> (list);
        Collections.rotate (ret, n);
        return ret;
    }

}