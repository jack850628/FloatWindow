package com.example.jack8.floatwindow.AShCalculator;

import java.util.ArrayList;
/**
 *計算機
 **/
public class AShCalculator {
    public static final String XOR = "xor";
    /**變數名稱紀錄**/
    StringBuilder Variable=new StringBuilder("");
    /**Count值陣列**/
    ArrayList<Count_Array> CountRecord=new ArrayList<>();
    /**
     *變數與字串處理類別，用於將字串完整輸出時
     * @param Str 欲處理的程式碼
     **/
    public String exec(String Str){
        String result;
        //System.out.println("Strdw "+Str);
        //階層關西：邏輯運算子>引號>字串||變數
        CountRecord.add(new Count_Array());
        CountRecord.get(CountRecord.size()-1).add(new Count(new StringBuilder(""),null));
        try {
            for (int i = 0; i < Str.length(); i++) {
                String str = Str.substring(i, i + 1);
                switch (str) {
                    case "+":
                        //case "xor":
                    case "-":
                    case "~":
                    case "√":
                        CountRecord.get(CountRecord.size() - 1).get(CountRecord.get(CountRecord.size() - 1).size() - 1).Value.append(Variable);
                        Variable.delete(0, Variable.length());
                        Count_Set.CountSet(CountRecord, str);
                        CountRecord.get(CountRecord.size() - 1).add(new Count(new StringBuilder(""), null));
                        break;
                    case "&":
                    case "|":
                    case "*":
                    case "×":
                    case "/":
                    case "÷":
                    case "%":
                    case "^": {
                        StringBuilder value = CountRecord.get(CountRecord.size() - 1).get(CountRecord.get(CountRecord.size() - 1).size() - 1).Value;
                        value.append(Variable);
                        if (value.toString().equals(""))//判斷是否兩個運算子相鄰，例如：8 ×÷ 3
                            throw new Exception("Error");
                        Variable.delete(0, Variable.length());
                        Count_Set.CountSet(CountRecord, str);
                        CountRecord.get(CountRecord.size() - 1).add(new Count(new StringBuilder(""), null));
                        break;
                    }
                    case ">":
                    case "<":
                        CountRecord.get(CountRecord.size() - 1).get(CountRecord.get(CountRecord.size() - 1).size() - 1).Value.append(Variable);
                        Variable.delete(0, Variable.length());
                        switch (Str.substring(i + 1, i + 2)) {
                            case ">"://判斷是否為 >>和<<或<>和><(等價於!=)
                                Count_Set.CountSet(CountRecord, str + ">");
                                i++;
                                break;
                            case "<"://判斷是否為 >>和<<或<>和><(等價於!=)
                                Count_Set.CountSet(CountRecord, str + "<");
                                i++;
                                break;
                            default:
                                Count_Set.CountSet(CountRecord, str);
                        }
                        CountRecord.get(CountRecord.size() - 1).add(new Count(new StringBuilder(""), null));
                        break;
                    case "(":
                        StringBuilder value = CountRecord.get(CountRecord.size() - 1).get(CountRecord.get(CountRecord.size() - 1).size() - 1).Value;
                        value.append(Variable);
                        Variable.delete(0, Variable.length());
                        if (!value.toString().equals("")){//判斷數字是否與括號相鄰，例如：2(3)
                            Count_Set.CountSet(CountRecord, "*");
                            CountRecord.get(CountRecord.size() - 1).add(new Count(new StringBuilder(""), null));
                        }
                        CountRecord.add(new Count_Array());
                        CountRecord.get(CountRecord.size() - 1).add(new Count(new StringBuilder(""), null));
                        Variable.delete(0, Variable.length());
                        break;
                    case ")":
                        if (CountRecord.size() == 1)
                            throw new Exception("Error");
                        CountRecord.get(CountRecord.size() - 1).get(CountRecord.get(CountRecord.size() - 1).size() - 1).Value.append(Variable);
                        Variable.delete(0, Variable.length());
                        Count_Set.CountSet(CountRecord, null);
                        Count_Set.Count(CountRecord);//開始計算
                        CountRecord.get(CountRecord.size() - 2).get(CountRecord.get(CountRecord.size() - 2).size() - 1).Value.append(CountRecord.get(CountRecord.size() - 1).get(0).Value);
                        CountRecord.remove(CountRecord.size() - 1);
                        break;
                    default:
                        if (!str.equals(" "))
                            Variable.append(str);
                        else {
                            switch (Variable.toString()) {
                                case XOR:
                                    Count_Set.CountSet(CountRecord, Variable.toString());
                                    CountRecord.get(CountRecord.size() - 1).add(new Count(new StringBuilder(""), null));
                                    break;
                                default:
                                    CountRecord.get(CountRecord.size() - 1).get(CountRecord.get(CountRecord.size() - 1).size() - 1).Value.append(Variable);
                            }
                            Variable.delete(0, Variable.length());
                        }
                        break;
                }
            }
            if (CountRecord.size() != 1)
                throw new Exception("Error");
            CountRecord.get(CountRecord.size() - 1).get(CountRecord.get(CountRecord.size() - 1).size() - 1).Value.append(Variable);
            Count_Set.CountSet(CountRecord, null);
            Count_Set.Count(CountRecord);//開始計算
            result = CountRecord.get(0).get(0).Value.toString();
        }catch (Exception e){
            result = e.getMessage();
        }
        Variable.delete(0, Variable.length());
        CountRecord.get(0).clear();
        CountRecord.clear();
        return result;
    }
}
