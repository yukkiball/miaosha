package com.miaoshaproject.service;

/**
 * @Auther: yuki
 * @Date: 2020/2/12 14:45
 * @Description:
 */

//封装本地缓存操作类
public interface CacheService {
    //存操作
    void setCommonCache(String key, Object value);

    //取操作
    Object getFromCommonCache(String key);
}
