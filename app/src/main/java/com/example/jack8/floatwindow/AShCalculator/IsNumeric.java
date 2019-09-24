package com.example.jack8.floatwindow.AShCalculator;
/**字串與數字的轉換**/
class IsNumeric {
	public static double isNumeric(String str){
		try{
			return Double.parseDouble(str);
		} catch(Exception e) {
			return 0.0;
		}
	}
	public static boolean isNumericTest(String str){
		return str.matches("[-+]?\\d+[.]?\\d*|[-+]?\\d+\\.\\d+E[-]?\\d+|[-+]?Infinity");
                /*try{
			Double.valueOf(str);
			return true;
		} catch(Exception e) {
			return false;
		}*/
	}
}
