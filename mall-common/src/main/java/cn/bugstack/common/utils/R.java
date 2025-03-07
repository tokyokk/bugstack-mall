/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package cn.bugstack.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 *
 * @author Mark sunlightcs@gmail.com
 */
public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public R() {
        put("code", 0);
        put("msg", "success");
    }

    public <T> T getData(String key,TypeReference<T> typeReference) {
        Object data = get(key); // 默认是map
        String dataStr = JSON.toJSONString(data);
        T t = JSON.parseObject(dataStr, typeReference);
        return t;
    }

    /**
     * 利用alibaba的fastjson解析json数据，处理复杂类型的转换，需要指定类型
     *
     * @param typeReference 指定类型
     * @param <T>           需要转换的类型
     * @return 返回数据
     */
    public <T> T getData(TypeReference<T> typeReference) {
        Object data = get("data"); // 默认是map
        String dataStr = JSON.toJSONString(data);
        T t = JSON.parseObject(dataStr, typeReference);
        return t;
    }

    public R setData(Object data) {
        put("data", data);
        return this;
    }

    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public Integer getCode() {
        return ((Integer) this.get("code"));
    }
}
