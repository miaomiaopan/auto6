package com.yh.qa.util;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author matt Gong on 2017/10/16
 */
public class JsonUtil {
    /**
     *
     * @param json  json字符串
     * @param map   要替换字段值的map, key以JsonPath语法为格式
     * @return  返回替换后的json字符串
     */
    public static String getJson(String json,Map<String,String> map){
        DocumentContext jsonObject = JsonPath.parse(json);
        for(String s: map.keySet()){
               jsonObject.set(s,map.get(s));
        }
        return jsonObject.jsonString();
    }

    /**
     * @param filePath  指定json文件路径
     * @param map   要替换字段值的map, key以JsonPath语法为格式
     * @return 返回替换后的json字符串
     * @throws IOException
     */
    public static String getJsonFromFile(String filePath, Map<String, String> map) throws IOException {
            String jsonStr = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath),"utf-8");
            return getJson(jsonStr,map);
    }

    public static void main(String args[]) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("order_sn","sn1234343");
        map.put("packages[0].package.amount","4");
        map.put("packages[1].package.amount","444");
        System.out.println(getJsonFromFile("bodyTemplate/testJsonUtil.json",map));
    }
}
