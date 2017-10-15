package com.yh.qa.service.impl;

import com.yh.qa.service.KDSService;
import com.yh.qa.util.Path;
import com.yh.qa.util.ResultBean;
import com.yh.qa.util.Schema;
import com.yh.qa.util.ValidateUtil;
import io.restassured.path.json.JsonPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service("KDSService")
public class KDSServiceImpl extends HttpServiceImpl implements KDSService{
    @Value("${domain-kds}")
    private String domainKDS;

    //获取加工单列表
    public JsonPath getProcessOrderList(Map<String, String> queryPara, int code) throws Exception{
        ResultBean result = get(domainKDS + Path.GETPROCESSORDERLIST + generateKDSUrl(queryPara), Schema.KDS_GET_PROCESS_ORDER_LIST);
        return ValidateUtil.validateCode(result, code);
    }

    //确认加工单
    public JsonPath confirmOrder(Map<String, String> queryPara, String body, int code) throws Exception{
        ResultBean result = post(domainKDS + Path.CONFIRMORDER + generateKDSUrl(queryPara), body, Schema.KDS_OPERATOR_ORDER);
        return ValidateUtil.validateCode(result, code);
    }

    //开始加工单
    public JsonPath beginProcessOrder(Map<String, String> queryPara, String body, int code) throws Exception{
        ResultBean result = post(domainKDS + Path.BEGINPROCESSORDER + generateKDSUrl(queryPara), body, Schema.KDS_OPERATOR_ORDER);
        return ValidateUtil.validateCode(result, code);
    }

    //完成加工单
    public JsonPath finishProcessOrder(Map<String, String> queryPara,String body, int code) throws Exception{
        ResultBean result = post(domainKDS + Path.FINISHPROCESSORDER + generateKDSUrl(queryPara), body, Schema.KDS_OPERATOR_ORDER);
        return ValidateUtil.validateCode(result, code);
    }

    //完成加工单
    public JsonPath pickUpOrder(Map<String, String> queryPara,String body, int code) throws Exception{
        ResultBean result = post(domainKDS + Path.SELFPICK + generateKDSUrl(queryPara), body, Schema.KDS_OPERATOR_ORDER);
        return ValidateUtil.validateCode(result, code);
    }

    private static String generateKDSUrl(Map<String, String> queryPara){
        StringBuffer query = new StringBuffer();
        query.append("?");
        for(String key:queryPara.keySet()){
            query.append(key).append("=").append(queryPara.get(key)).append("&");
        }
        query.append("sign=").append(generateSign(queryPara));
        return query.toString();
    }

    private static String generateSign(Map<String, String> para){
        return generateSign(para,"123");
    }

    private static String generateSign(Map<String, String> para, String appSecret){
        Set<String> paraKey = new TreeSet<String>(para.keySet());
        StringBuffer tmpPara = new StringBuffer();
        for(Iterator iter = paraKey.iterator();iter.hasNext();){
            String tmpKey = iter.next().toString();
            tmpPara.append(tmpKey);
            tmpPara.append(para.get(tmpKey));
        }
        String sign = MD5(appSecret + tmpPara + appSecret);
        return sign.toUpperCase();
    }

    private static String MD5(String inStr){
        MessageDigest md5 = null;
        try{
            md5 = MessageDigest.getInstance("MD5");
        }catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++){
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }
}
