# 电商秒杀系统

基于Spring Boot，Spring MVC，MyBatis，Nginx，Redis，RocketMQ实现的电商秒杀系统

### DAO层

- 使用MyBatis生成对应的Mapper类（由Mybatis generator自动生成，原理是根据接口定义创建接口的动态代理对象）
- Mybatis重要的类（ SqlSessionFactoryBuilder（用于创建工厂）、SqlSessionFactory（生成对象）、SqlSession （定义数据库方法）底部还是使用jdbc进行操作）

### Service层

- 用户登录、注册的服务
- 商品创建和展示的服务
- 下单和秒杀的服务
- 设计用户、商品、订单、秒杀模型

### Controller层

- 负责调用service层的服务，生成VO（前端需要展示给用户的数据）返回给前端
- 使用CommonReturnType，其中记录了status（表明对应处理结果）和data(返回前端需要的json数据其中包含VOmodel)
- json是纯文本，便于阅读，占用带宽少，可以用ajax传输



### nginx模块

- 反向代理+负载均衡：设置upstream server

- 动静分离：静态请求/resources/ 转发到静态服务器

​                   动态请求转发到Tomcat

- 设置了nginx服务器和后端服务器的长连接，在upstream中设置keepalive，启动http1.1+Connection置空
- 设置了nginx proxy_set_header，否则为nginx服务器host和端口
- 开启了tomcat access log



### 分布式会话

- 将session存入redis中，@EnableRedisHttpSession，但cookie禁用时会出现问题
- 使用UUID生成token，存入redis中，在前端存入window.localstorage（登录时），下单时添加在url参数中，后台获取token后，从redis中取出校验。还支持移动端（移动端禁用cookie)



### 多级缓存

- redis缓存商品详情页（单机、哨兵、集群）,使用Jackson2JsonRedisSerializer的自定义序列化方式
- guava cache（可控大小和时间、线程安全、lru）本地热点缓存（热点数据、脏读不敏感、内存可控，瞬时访问 ），生命周期很短
- nginx proxy cache缓存：访问本地磁盘文件太慢
- nginx lua
- 一般推荐Redis设置内存为最大物理内存的四分之三



### 交易优化

- 下单时使用redis对用户和商品model的缓存验证（redis没有从数据库读写入redis)，减轻数据库压力
- 活动发布同步库存进缓存，下单减缓存库存，异步消息扣减数据库库存



### 事务型消息

- rocketmq事务性消息，首先向broker发送半消息（消息不可消费），执行本地事务，根据结果发送二次确认，成功commit，否则rollback。若二次确认长时间未送到，则会checklocalTracsaction，根据操作流水来判断是否提交消息。
- 增加库存操作流水，初始状态为1，下单成功为2，回滚为3，根据状态来决定消息是commit还是回滚

- 库存售罄，加入库存售罄标志，提前抛出异常，避免每次都加入库存流水



### 流量削峰

- 秒杀令牌，UUID生成，每次下单前需要校验秒杀令牌，防刷接口（"promo_token_"+promoId+"_user_"+userId+"_item_"+itemId）三个维度组成完整的key
- 秒杀大闸，根据秒杀商品数量乘相应倍数发放令牌数量，设置对应数量令牌,避免秒杀令牌不断生成
- 队列泄洪，排队有时比并发更高效，依靠拥塞窗口释放流量大小



### 防刷限流

- 验证码技术
- 令牌桶算法（每一秒往桶中放10个令牌），Guava的RateLimitter，进行限流，返回系统太火爆
- 漏桶算法，往桶中加水满了就不能加，每秒往外面流水
- 令牌桶算法和漏桶算法，漏桶算法不能应对突发流量，用于平滑网络流量



