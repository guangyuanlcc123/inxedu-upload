package com.inxedu.wxos.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Administrator
 * @date 2017/3/13
 */
@Slf4j
@Component
public class JsonUtil {

    @Autowired
    @Qualifier("serializingObjectMapper")
    public ObjectMapper tempObjectMapper;
    public static ObjectMapper objectMapper;
    @PostConstruct
    public void init(){
        objectMapper = tempObjectMapper;
    }
    @SuppressWarnings("unchecked")
    public static Map<String,Object> json2Map(String json){
        return json2Object(json,Map.class);
    }
    @Deprecated
    public static Map<String,String> json2StringMap(String json){
        return json2Object(json,Map.class);
    }
    public static <T> T obj2Object(String obj,Class<T> c){
        return json2Object(obj,c);
    }

    public static String obj2JsonString(Object obj){
        return toJSon(obj);
    }
    public static List obj2JsonList(String json){
        return json2Object(json,List.class);
    }


    /**
     * 使用泛型方法，把json字符串转换为相应的JavaBean对象。
     * (1)转换为普通JavaBean：readValue(json,Student.class)
     * (2)转换为List,如List<Student>,将第二个参数传递为Student
     * [].class.然后使用Arrays.asList();方法把得到的数组转换为特定类型的List
     *
     * @param jsonStr
     * @param valueType
     * @return
     */
    public static <T> T json2Object(String jsonStr, Class<T> valueType) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        try {
            return objectMapper.readValue(jsonStr, valueType);
        } catch (Exception e) {
            log.error("json2Object error : "+e);
        }

        return null;
    }

    /**
     * json数组转List
     * @param jsonStr
     * @param valueTypeRef
     * @return
     */
    public static <T> T json2Object(String jsonStr, TypeReference<T> valueTypeRef){
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        try {
            return objectMapper.readValue(jsonStr, valueTypeRef);
        } catch (Exception e) {
            log.error("json2Object error : "+e);
        }
        return null;
    }

    /**
     * 把JavaBean转换为json字符串
     *
     * @param object
     * @return
     */
    public static String toJSon(Object object) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("toJSon error : "+e);
        }
        return null;
    }

}


