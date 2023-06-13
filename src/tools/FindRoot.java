package tools;

import org.mathIT.util.FunctionParser;

import java.util.function.DoubleFunction;

public class FindRoot {
    public final static double EPSILON = 1E-5; // Tolerance, 1E^-1 == 10^-1.

    public static DoubleFunction<Double> Derive(DoubleFunction<Double> a){
        double DX = 0.0001;
        return (x) -> (a.apply(DX+x) - a.apply(x)) / DX;
    }

    public static double newrahMethod(DoubleFunction<Double> f, double P0){
        int count = 1;
        double P1; // P1
        while(true){
            P1 = P0 - (f.apply(P0)/Derive(f).apply(P0));
            //System.out.println(P1);
            if(Math.abs(P0-P1) < EPSILON || P1 == P0){ // Don't forget to update the EPSILON constant in CONST.java.
                return P1; //System.out.println("The root is = "+P1+" Found in "+count+" iterations.");
            }
            P0 = P1;
            count++;
        }
    }

    public static void main(String[] args) {
        FunctionParser fp = new FunctionParser("x^2-10");
        DoubleFunction<Double> func = value -> fp.evaluate(0, value);
        newrahMethod (func, 10);
    }
}



