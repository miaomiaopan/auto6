package com.yh.qa.util;

import java.util.List;
import java.util.Random;

public class RandomString {
    public static String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String getRandomString(int length) { //length表示生成字符串的长度
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String getDeviceIdIos() {//2C86005F-0698-4715-B477-960B224EB332
        return getRandomString(8) + "-" + getRandomString(4) + "-" + getRandomString(4) + "-" + getRandomString(4) + "-" + getRandomString(12);
    }

    //public static void main(String[] args) {
    //    System.out.println(RandomString.getDeviceIdIos());
    //}
    
    //把map<String>转换为（'',''）形式
    public static String getInStrFormList(List<String> orderIds){
		if(orderIds.isEmpty()){
			return "('')";
		}
		String orderIdsStr = "(";
		int size  =orderIds.size();
		for(int i = 0; i < size; i ++){
			if(i == size - 1){
				orderIdsStr = orderIdsStr+"'"+orderIds.get(i)+"'";
			}else{
				orderIdsStr = orderIdsStr+"'"+orderIds.get(i)+"',";
			}
		}
		
		orderIdsStr = orderIdsStr+")";
		
		return orderIdsStr;
	}
    
    //把List<String>转为转义数组的字符串形式
    public static String getStringFromList(List<String> arr, boolean bool){
		if(arr.isEmpty()){
			return "";
		}
		String str = "";
		int size  =arr.size();
		if(bool){
			for(int i = 0; i < size; i ++){
				if(i == size - 1){
					str = str+"\""+arr.get(i)+"\"";
				}else{
					str = str+"\""+arr.get(i)+"\",";
				}
			}
		}else{
			for(int i = 0; i < size; i ++){
				if(i == size - 1){
					str = str+""+arr.get(i)+"";
				}else{
					str = str+""+arr.get(i)+",";
				}
			}
		}
		
		
		return str;
	}
    
}
