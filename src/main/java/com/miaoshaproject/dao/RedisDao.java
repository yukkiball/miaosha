package com.miaoshaproject.dao;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.miaoshaproject.dataobject.PromoDO;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Author：yuki
 * @Description:
 * @Date: Created in 20:35 2019/12/7
 * @Modified By:
 */
@Component
public class RedisDao {

    private JedisPool jedisPool;
    private RuntimeSchema<PromoDO> schema = RuntimeSchema.createFrom(PromoDO.class);

    public RedisDao(){
        jedisPool = new JedisPool("127.0.0.1", 6379);
    }

    public PromoDO getPromoDo(Integer itemId){
        //redis操作逻辑
        Jedis jedis = jedisPool.getResource();

        try{
            String key = "PromoId:" + itemId;
            //并没有实现内部序列化操作
            //get->byte[] -> 反序列化 -> Object
            //自定义序列化，不使用jdk内置
            byte[] bytes = jedis.get(key.getBytes());
            if (bytes != null){
                PromoDO promoDO = schema.newMessage();
                ProtostuffIOUtil.mergeFrom(bytes, promoDO, schema);
                return promoDO;
            }
        } finally {
            jedis.close();
        }
        return null;
    }

    public String putPromoDo(PromoDO promoDO){
        //Object->序列化->byte[]
        Jedis jedis = jedisPool.getResource();
        try{
            String key = "PromoId:" + promoDO.getItemId();
            byte[] bytes = ProtostuffIOUtil.toByteArray(promoDO, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            //超时缓存
            int timeout = 30 * 60;
            String result = jedis.setex(key.getBytes(), timeout, bytes);
            return result;
        } finally {
            jedis.close();
        }

    }
}
