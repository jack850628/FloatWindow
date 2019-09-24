package com.example.jack8.floatwindow.AShCalculator;

import static java.lang.Math.pow;
import java.util.ArrayList;
/** 因為先乘除後加減的關西，所以這個函數是用來處理乘與除的
	 * 在ACMD上，1+2*2-3會被儲存成
	 * +---+---+---+----+
	 * | 1 | 2 | 2 | 3  |
	 * +---+---+---+----+
	 * | + | * | - |null|
	 * +---+---+---+----+**/
class Count_Set {
	/** 最優先處理的運算子
     * @param CountRecord 需要被運算的陣列
     * @param Operator 下一個運算子*/
	public static void CountSet(ArrayList<Count_Array> CountRecord,String Operator) throws Exception{
            Count count = CountRecord.get(CountRecord.size()-1).get(CountRecord.get(CountRecord.size()-1).size()-1);
            if(IsNumeric.isNumericTest(count.Value.toString()) || count.Value.toString().equals(""))
                count.Operator = Operator;
            else
                throw new Exception("Error");
	}
        /** 第一優先處理的運算子
     * @param CountRecord 需要被運算的陣列
     * @param index 須被運算的項目在陣列中的位置
     * @return 接下來須被運算的項目在陣列中的位置
     */
        public static int CountSet1(ArrayList<Count_Array> CountRecord,int index) throws Exception{
		if(CountRecord.get(CountRecord.size()-1).size()>1){
                    if(CountRecord.get(CountRecord.size()-1).get(index).Operator==null)
                        return ++index;
                    switch (CountRecord.get(CountRecord.size()-1).
                            get(index).
                            Operator) {
                        case "+":
                        {
                            switch (CountRecord.get(CountRecord.size()-1).get(index).Value.toString()) {
                                case "":
                                    switch(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString()){
                                        case "":
                                            CountSet1(CountRecord,index+1);
                                        default:
                                            double a=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(String.valueOf(a).matches("[+]?\\d+(?:\\.0)?")?String.format("%.0f", a):a);
                                            CountRecord.get(CountRecord.size()-1).remove(index);
                                    }
                                    break;
                                default:
                                    index++;
                            }
                            break;
                        }
                        case "-":
                        {
                            switch (CountRecord.get(CountRecord.size()-1).get(index).Value.toString()) {
                                case "":
                                    switch(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString()){
                                        case "":
                                            CountSet1(CountRecord,index+1);
                                        default:
                                            double a=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(String.valueOf(a).matches("[+]?\\d+(?:\\.0)")?String.format("%.0f", -a):-a);
                                            CountRecord.get(CountRecord.size()-1).remove(index);
                                    }
                                    break;
                                default:
                                    index++;
                            }
                            break;
                        }
                        case "~":
                        {
                            switch (CountRecord.get(CountRecord.size()-1).get(index).Value.toString()) {
                                case "":
                                    switch(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString()){
                                        case "":
                                            CountSet1(CountRecord,index+1);
                                        default:
                                            long a=(long)IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                                CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(~a);
                                            CountRecord.get(CountRecord.size()-1).remove(index);
                                    }
                                    break;
                                default:
                                    throw new Exception("Error");
                            }
                            break;
                        }
                        default:
                            index++;
                    }
		}
                return index;
	}
        /** 第二優先處理的運算子
     * @param CountRecord 需要被運算的陣列
     * @param index 須被運算的項目在陣列中的位置
     * @return 負數處理判斷，用來判斷這個這次運算子後面的 - 是負號還是減號 ，如果為null就代表著已經是運算式的末端，後面沒東西了*/
	public static int CountSet2(ArrayList<Count_Array> CountRecord,int index){
		if(CountRecord.get(CountRecord.size()-1).size()>1){
                        if(CountRecord.get(CountRecord.size()-1).get(index).Operator==null)
                            return ++index;
                        switch (CountRecord.get(CountRecord.size()-1).
                                get(index).
                                Operator) {
                            case "^":
                            {
                                if (CountRecord.get(CountRecord.size()-1).get(index+1).Operator!=null&&
                                        CountRecord.get(CountRecord.size()-1).get(index+1).Operator.equals("**"))
                                    CountSet2(CountRecord,index+1);
                                else{
                                    double a=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index).Value.toString()),
                                        b=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                        CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                                    a=pow(a,b);
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(String.valueOf(a).matches("[+]?\\d+(?:\\.0)")?String.format("%.0f", a):a);
                                    CountRecord.get(CountRecord.size()-1).remove(index);
                                }
                                break;
                            }
                            case "√":
                            {
                                if (CountRecord.get(CountRecord.size()-1).get(index+1).Operator!=null&&
                                        CountRecord.get(CountRecord.size()-1).get(index+1).Operator.equals("**"))
                                    CountSet2(CountRecord,index+1);
                                else{
                                    double a,b=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                                    if(!CountRecord.get(CountRecord.size()-1).get(index).Value.toString().equals(""))
                                        a = IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index).Value.toString());
                                    else
                                        a = 2d;
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                                    a=pow(b,1d/a);
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(String.valueOf(a).matches("[+]?\\d+(?:\\.0)")?String.format("%.0f", a):a);
                                    CountRecord.get(CountRecord.size()-1).remove(index);
                                }
                                break;
                            }
                            default:
                                index++;
                        }
		}
            return index;
	}
        /** 第三優先處理的運算子
     * @param CountRecord 需要被運算的陣列
     * @param index 須被運算的項目在陣列中的位置
     * @return 負數處理判斷，用來判斷這個這次運算子後面的 - 是負號還是減號 ，如果為null就代表著已經是運算式的末端，後面沒東西了*/
	public static int CountSet3(ArrayList<Count_Array> CountRecord,int index){
		if(CountRecord.get(CountRecord.size()-1).size()>1){
                        if(CountRecord.get(CountRecord.size()-1).get(index).Operator==null)
                            return ++index;
                        switch (CountRecord.get(CountRecord.size()-1).
                                get(index).
                                Operator) {
                            case "*":
                            case "×":
                            {
                                double b=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                                double a=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index).Value.toString());
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                        CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                                    a*=b;
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(String.valueOf(a).matches("[+]?\\d+(?:\\.0)")?String.format("%.0f", a):a);
                                CountRecord.get(CountRecord.size()-1).remove(index);
                                break;
                            }
                            case "/":
                            case "÷":
                            {
                                double a=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index).Value.toString()),
                                    b=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                                CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                                a/=b;
                                CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(String.valueOf(a).matches("[+]?\\d+(?:\\.0)")?String.format("%.0f", a):a);
                                CountRecord.get(CountRecord.size()-1).remove(index);
                                break;
                            }
                            case "%":
                            {
                                double a=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index).Value.toString()),
                                    b=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                                CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                                a%=b;
                                CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(String.valueOf(a).matches("[+]?\\d+(?:\\.0)")?String.format("%.0f", a):a);
                                CountRecord.get(CountRecord.size()-1).remove(index);
                                break;
                            }
                            default:
                                index++;
                        }
		}
            return index;
	}
        /** 第四優先處理的運算子
     * @param CountRecord 需要被運算的陣列
     * @param index 須被運算的項目在陣列中的位置
     * @return 接下來須被運算的項目在陣列中的位置*/
        public static int CountSet4(ArrayList<Count_Array> CountRecord,int index){
		if(CountRecord.get(CountRecord.size()-1).size()>1){
                    if(CountRecord.get(CountRecord.size()-1).get(index).Operator==null)
                        return ++index;
                    switch (CountRecord.get(CountRecord.size()-1).
                            get(index).
                            Operator) {
                        case "+":{/*目前布林直無法進行加號運算*/
                            /**在StrDW中字串類型引號(")會再取值時被去除掉，所以當你要呼叫native write顯示例如"2"+"3"時，來到這裡時就會變成2+3，所以會顯示5，
                                                                    且如果是""，再來到這裡時也會變成什麼都沒有且長度為零的空字串**/
                            double a=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index).Value.toString()),
                                        b=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                                CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                        CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                                a+=b;
                                CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(String.valueOf(a).matches("[+]?\\d+(?:\\.0)")?String.format("%.0f", a):a);
                                CountRecord.get(CountRecord.size()-1).remove(index);
                            break;
                        }
                        case "-":{
                            double a=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index).Value.toString()),
                                    b=IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                            a-=b;
                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(String.valueOf(a).matches("[+]?\\d+(?:\\.0)")?String.format("%.0f", a):a);
                            CountRecord.get(CountRecord.size()-1).remove(index);
                            break;
                        }
                        default:
                            index++;
                    }
		}
                return index;
	}
        /** 第五優先處理的運算子
     * @param CountRecord 需要被運算的陣列
     * @param index 須被運算的項目在陣列中的位置
     * @return 接下來須被運算的項目在陣列中的位置*/
        public static int CountSet5(ArrayList<Count_Array> CountRecord,int index){
		if(CountRecord.get(CountRecord.size()-1).size()>1){
                    if(CountRecord.get(CountRecord.size()-1).get(index).Operator==null)
                        return ++index;
                    switch (CountRecord.get(CountRecord.size()-1).
                            get(index).
                            Operator) {
                        case "<<":
                        {
                            long a=(long)IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index).Value.toString()),
                                    b=(long)IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(a<<b);
                            CountRecord.get(CountRecord.size()-1).remove(index);
                            break;
                        }
                        case ">>":
                        {
                            long a=(long)IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index).Value.toString()),
                                    b=(long)IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(a>>b);
                            CountRecord.get(CountRecord.size()-1).remove(index);
                            break;
                        }
                        default:
                            index++;
                    }
		}
                return index;
	}
        
        /** 第八優先處理的運算子
     * @param CountRecord 需要被運算的陣列
     * @param index 須被運算的項目在陣列中的位置
     * @return 接下來須被運算的項目在陣列中的位置*/
        public static int CountSet8(ArrayList<Count_Array> CountRecord,int index){
		if(CountRecord.get(CountRecord.size()-1).size()>1){
                    if(CountRecord.get(CountRecord.size()-1).get(index).Operator==null)
                        return ++index;
                    switch (CountRecord.get(CountRecord.size()-1).
                            get(index).
                            Operator) {
                        case "&":
                            long a=(long)IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index).Value.toString()),
                                    b=(long)IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(a&b);
                            CountRecord.get(CountRecord.size()-1).remove(index);
                            break;
                        default:
                            index++;
                    }
		}
                return index;
	}
        /** 第九優先處理的運算子
     * @param CountRecord 需要被運算的陣列
     * @param index 須被運算的項目在陣列中的位置
     * @return 接下來須被運算的項目在陣列中的位置*/
        public static int CountSet9(ArrayList<Count_Array> CountRecord,int index){
		if(CountRecord.get(CountRecord.size()-1).size()>1){
                    if(CountRecord.get(CountRecord.size()-1).get(index).Operator==null)
                        return ++index;
                    switch (CountRecord.get(CountRecord.size()-1).
                            get(index).
                            Operator) {
                        case AShCalculator.XOR:
                            long a=(long)IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index).Value.toString()),
                                    b=(long)IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(a^b);
                            CountRecord.get(CountRecord.size()-1).remove(index);
                            break;
                        default:
                            index++;
                    }
		}
                return index;
	}
        /** 第十優先處理的運算子
     * @param CountRecord 需要被運算的陣列
     * @param index 須被運算的項目在陣列中的位置
     * @return 接下來須被運算的項目在陣列中的位置*/
        public static int CountSet10(ArrayList<Count_Array> CountRecord,int index){
		if(CountRecord.get(CountRecord.size()-1).size()>1){
                    if(CountRecord.get(CountRecord.size()-1).get(index).Operator==null)
                        return ++index;
                    switch (CountRecord.get(CountRecord.size()-1).
                            get(index).
                            Operator) {
                        case "|":
                            long a=(long)IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index).Value.toString()),
                                    b=(long)IsNumeric.isNumeric(CountRecord.get(CountRecord.size()-1).get(index+1).Value.toString());
                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.delete(0,
                                    CountRecord.get(CountRecord.size()-1).get(index+1).Value.length());
                            CountRecord.get(CountRecord.size()-1).get(index+1).Value.append(a|b);
                            CountRecord.get(CountRecord.size()-1).remove(index);
                            break;
                        default:
                            index++;
                    }
		}
                return index;
	}
        /** 運算處理
     * @param CountRecord 需要被運算的陣列
     */
        public static void Count(ArrayList<Count_Array> CountRecord) throws Exception{
                for(int count=0;count<CountRecord.get(CountRecord.size()-1).size()-1;)
			count=CountSet1(CountRecord,count);
            
                for(int count=0;count<CountRecord.get(CountRecord.size()-1).size()-1;)
			count=CountSet2(CountRecord,count);
                
                for(int count=0;count<CountRecord.get(CountRecord.size()-1).size()-1;)
			count=CountSet3(CountRecord,count);
            
                for(int count=0;count<CountRecord.get(CountRecord.size()-1).size()-1;)
			count=CountSet4(CountRecord,count);
            
                for(int count=0;count<CountRecord.get(CountRecord.size()-1).size()-1;)
			count=CountSet5(CountRecord,count);
                
                for(int count=0;count<CountRecord.get(CountRecord.size()-1).size()-1;)
			count=CountSet8(CountRecord,count);
                
                for(int count=0;count<CountRecord.get(CountRecord.size()-1).size()-1;)
			count=CountSet9(CountRecord,count);
                
                for(int count=0;count<CountRecord.get(CountRecord.size()-1).size()-1;)
			count=CountSet10(CountRecord,count);
        }
}
