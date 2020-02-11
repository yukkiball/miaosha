package com.miaoshaproject.config;

import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.stereotype.Component;

/**
 * @Auther: yuki
 * @Date: 2020/2/11 19:47
 * @Description: Redis实现分布式会话,将session存入redis缓存中
 */

@Component
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class RedisConfig {
}
