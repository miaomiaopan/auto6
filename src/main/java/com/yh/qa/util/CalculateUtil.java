package com.yh.qa.util;

import java.math.BigDecimal;

/**
 * @author matt Gong on 2017/10/15
 */
public class CalculateUtil {
    public static Double sub(Double v1,Double v2){
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.subtract(b2).doubleValue();
    }

    public static void main(String args[]){
        Double a = 138.6;
        Double b = 19.8;
        Double c = 158.4;
        System.out.println(c-b);
        System.out.println(sub(c,b) == new BigDecimal(a).doubleValue());
    }
}
