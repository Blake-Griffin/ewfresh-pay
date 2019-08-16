package com.ewfresh.pay.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONLibDataFormatSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * This class consists exclusively of static methods that operate on or return
 * json.which returns a new json、jsonp or object by a specified object or json.
 *
 *
 * <p>The methods of this class all throw a <tt>NullPointerException</tt>
 * if the json or object provided to them are null.
 *
 * @author LiuJing
 * @Since jdk1.7
 */
public class JsonUtil {
    private final static String NULLSTR = "";
    private final static String JSONP_PREFIX = "callback(";
    private final static String JSONP_SUFFIX = ")";

    private JsonUtil() {};

    private final static SerializeConfig config = new SerializeConfig();
    {
        config.put(java.util.Date.class, new JSONLibDataFormatSerializer());
        config.put(java.sql.Date.class, new JSONLibDataFormatSerializer());
    }

    private final static SerializerFeature[] features = {
            SerializerFeature.WriteMapNullValue,
            SerializerFeature.WriteNullListAsEmpty,
            SerializerFeature.WriteNullNumberAsZero,
            SerializerFeature.WriteNullBooleanAsFalse,
            SerializerFeature.WriteNullStringAsEmpty
    };

    public static String toJson(Object object) {
        return JSON.toJSONString(object, config, features);
    }

    public static String toJsonp(Object object) {
        return  JSONP_PREFIX + JSON.toJSONString(object, config, features) + JSONP_SUFFIX;
    }

    public static <T> T jsonToObj(String jsonValue, Class<T> c) {
        return StringUtils.isBlank(jsonValue) ? null : JSON.parseObject(jsonValue, c);
    }

    public static <T> T jsonpToObj(String jsonpValue, Class<T> c) {
        String rex = "[()]+";

        if (StringUtils.isBlank(jsonpValue)) {
            return null;
        }

        String[] json = jsonpValue.split(rex);

        if (json.length < 1) {
            throw new IllegalArgumentException("The params of 'jsonpValue' is not invalid,pls check.");
        }

        return JSON.parseObject(json[1],c);
    }

/*
    public static <T>List<T> jsonToList(String json, T t){
        String decode="";
        if(json!=null&&!json.equals("")){
            try {
                decode = URLDecoder.decode(json, "utf-8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            JSONArray fromObject = JSONArray.fromObject(decode);
            List<T> collection = (List<T>) JSONArray.toCollection(fromObject, t.getClass());
            return collection;
        }else{
            return null;
        }

    }
*/


}
