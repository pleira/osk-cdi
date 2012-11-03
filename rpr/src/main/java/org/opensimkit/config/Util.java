package org.opensimkit.config;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Util {
	
	public static double[] extractDoubleArray(String values) {
		StringTokenizer tokenizer = new StringTokenizer(values, " ");		
		ArrayList<Double> list = new ArrayList<Double>();
		while (tokenizer.hasMoreTokens()) {
			Double d = Double.parseDouble(tokenizer.nextToken());
			list.add(d);
		}
		double[] array = new double[list.size()];
		for (int i=0; i < list.size() ;i++) {
		    array[i] = list.get(i).doubleValue();
		}
		return array;
	}
}
