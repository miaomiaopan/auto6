package com.yh.qa.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class SignUtil {

    private SignUtil() {
    }
    public static void main(String[] args) throws MalformedURLException {
//        String newUrl = getNewUrl("", "");
    }

    public static String getNewUrl(String url, String deviceid)  {

        String[] split = url.split("\\?");
        String[] kvs = split[1].split("&");
        StringBuilder sb = new StringBuilder();
        sb.append(split[0] + "?");

        for (String kv : kvs) {

            if (kv.contains("deviceid")) {
                sb.append(kv.split("=")[0]).append("=").append(deviceid);
            } else {
                sb.append(kv);
            }
            sb.append("&");
        }
        return sb.delete(sb.length() - 1, sb.length()).toString();
    }

    public static String getSignedUrl(String baseUrl, TreeMap<String, String[]> params, String body, String secret) throws Exception {

//        String sign = toMD5Sign(params, body, secret);
        String newUrl = baseUrl + "?";
        Set<Map.Entry<String, String[]>> entries = params.entrySet();
        int size = entries.size();
        StringBuilder sb = new StringBuilder();
        sb.append(newUrl);
        int index = 0;
        mergeUrl(entries, size, sb, index);

        return sb.toString();
    }

    private static void mergeUrl(Set<Map.Entry<String, String[]>> entries, int size, StringBuilder sb, int index) {
        for (Map.Entry<String, String[]> entry : entries) {
            index++;
            String key = entry.getKey();
            String value = entry.getValue()[0];
            if (key.equals("timestamp")) {
                value = String.valueOf(System.currentTimeMillis());
            }

            if (index == size) {
                sb.append(key + "=" + value);
            } else {
                sb.append(key + "=" + value + "&");
            }
        }
    }

    public static String getSignedUrl(String url, String body, String secret) {
        String sign = sign(url, body, secret);
        String[] urls = url.split("\\?");
        String[] kvs = urls[1].split("&");
        TreeMap<String, String[]> treeMap = new TreeMap<>();
        for (String kv : kvs) {
            String[] keyValue = kv.split("=");
            if (keyValue[0].equals("sign")) {
                keyValue[1] = sign;
            }
            treeMap.put(keyValue[0], new String[]{keyValue[1]});
        }
        String newUrl = urls[0] + "?";
        Set<Map.Entry<String, String[]>> entries = treeMap.entrySet();
        int size = entries.size();
        StringBuilder sb = new StringBuilder();
        sb.append(newUrl);
        int index = 0;
        mergeUrl(entries, size, sb, index);
        return sb.toString();

    }


    public static String sign(String url, String body, String secret) {
        try {


            if (StringUtils.isEmpty(url)) {
                throw new Exception("url为空");
            }
            String[] params = url.split("\\?");
            if (params.length < 2) {
                throw new Exception("url参数为空");
            }
            String[] kvs = params[1].split("&");
            TreeMap<String, String[]> treeMap = new TreeMap<>();
            for (String kv : kvs) {
                String[] keyValue = kv.split("=");
                treeMap.put(keyValue[0], new String[]{keyValue[1]});
            }
            treeMap.remove("sign");
            if (StringUtils.isEmpty(body)) {
                body = "";
            }
            return toMD5Sign(treeMap, body, secret);
        } catch (Exception e) {
        }

        return "";
    }

    public static String toMD5Sign(TreeMap<String, String[]> params, String json, String secret) {

        StringBuilder sb = new StringBuilder();
        sb.append(secret);
        StringBuilder input = new StringBuilder();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = "";
            for (String s : entry.getValue()) {
                value += s;
            }
            sb.append(key).append(value);
            input.append(key).append(":").append(value).append(",");
        }

        sb.append(json);
        return DigestUtils.md5Hex(sb.toString()).toUpperCase();
    }

    private final static String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static String getRandomString(int length) { //length表示生成字符串的长度
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


}
