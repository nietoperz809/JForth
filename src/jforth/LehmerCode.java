package jforth;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Arrays;

/**
 *
 * @author Administrator
 */
public class LehmerCode
{
    /**
     * Generates a single permutation
     * @param n How many elements
     * @param k number of permutation
     * @return array 0...n-1 of integers representing k'th permutation 
     */
    public static int[] perm(int n, int k)
    {
        int i, ind, m = k;
        int[] permuted = new int[n];
        int[] elems = new int[n];
        for (i = 0; i < n; i++)
        {
            elems[i] = i;
        }
        for (i = 0; i < n; i++)
        {
            ind = m % (n - i);
            m = m / (n - i);
            permuted[i] = elems[ind];
            elems[ind] = elems[n - i - 1];
        }
        return permuted;
    }

    /**
     * get index from permutation, see perm function
     * @param perm the permuted array
     * @return permutation index
     */
    public static int inv(int[] perm)
    {
        int i, k = 0, m = 1;
        int n = perm.length;
        int[] pos = new int[n];
        int[] elems = new int[n];
        for (i = 0; i < n; i++)
        {
            pos[i] = i;
            elems[i] = i;
        }
        for (i = 0; i < n - 1; i++)
        {
            k += m * pos[perm[i]];
            m = m * (n - i);
            pos[elems[n - i - 1]] = pos[perm[i]];
            elems[pos[perm[i]]] = elems[n - i - 1];
        }
        return k;
    }

    public static void main(String[] args)
    {
        int n = 10;
        int[] i1 = new int[n];
        for (int s=0; s<n; s++)
        {
            i1[s] = n-s-1;
        }
        //CryptMath.shuffleArray(i1);
        System.out.println(Arrays.toString(i1));
        System.out.println(inv(i1));
        
        int[] i2 = perm (n, inv(i1));
        System.out.println(Arrays.toString(i2));
        
//        for (int s=0; s<20; s++)
//        {
//            int[] l = perm (3,s);
//            System.out.println(s+" -- "+Arrays.toString(l) + " : "+inv(l));
//        }
    }
}
