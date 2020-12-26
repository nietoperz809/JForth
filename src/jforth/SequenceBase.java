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

    public SequenceBase ()
    {
    }

    public ArrayList<E> rearrange (int pos[])
    {
        return rearrange (pos, _list);
    }

    public ArrayList<E> swap (int a, int b)
    {
        return swap (_list, a, b);
    }

    public ArrayList<E> rotateLeft (int n)
    {
        return rotateLeft (_list, n);
    }

    public ArrayList<E> rotateRight (int n)
    {
        return rotateRight (_list, n);
    }

    public ArrayList<E> reverse()
    {
        ArrayList<E> list2 = new ArrayList<>(_list);
        Collections.reverse (list2);
        return list2;
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

    public ArrayList<E> shuffle ()
    {
        ArrayList<E> list2 = new ArrayList<>(_list);
        Collections.shuffle (list2);
        return list2;
    }

    public ArrayList<E> sort ()
    {
        ArrayList<E> list2 = new ArrayList<>(_list);
        Collections.sort (list2);
        return list2;
    }

    public ArrayList<E> intersect (SequenceBase<E> other)
    {
        ArrayList<E> ret = new ArrayList<>(_list);
        ret.retainAll(other._list);
        return ret;
    }

    public ArrayList<E> difference (SequenceBase<E> other)
    {
        ArrayList<E> ret = new ArrayList<>(_list);
        ret.removeAll(other._list);
        return ret;
    }

    public ArrayList<E> unique ()
    {
        return new ArrayList<>(new LinkedHashSet<>(_list));
    }

    public ArrayList<E> subList (int from, int to)
    {
        return (ArrayList<E>) _list.subList(from, to);
    }

    public boolean sameContents (SequenceBase<E> other)
    {
        if (this._list.size () != other._list.size ())
            return false;
        ArrayList<E> test = (ArrayList<E>) _list.clone ();
        test.removeAll (other._list);
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
                ar.add(d1._list.get(s));
            if (s < d2.length())
                ar.add (d2._list.get(s));
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
