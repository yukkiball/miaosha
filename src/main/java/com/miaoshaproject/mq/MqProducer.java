package com.miaoshaproject.mq;

import com.alibaba.fastjson.JSON;
import com.miaoshaproject.dao.StockLogDOMapper;
import com.miaoshaproject.dataobject.StockLogDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.OrderService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: yuki
 * @Date: 2020/2/13 11:27
 * @Description:
 */
@Component
public class MqProducer {

    private DefaultMQProducer producer;

    private TransactionMQProducer transactionMQProducer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private OrderService orderService;

    @Autowired(required = false)
    private StockLogDOMapper stockLogDOMapper;

    @PostConstruct
    public void init() throws MQClientException {
        //mq producer初始化
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);

        producer.start();

        transactionMQProducer = new TransactionMQProducer("transaction_producer_group");
        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.start();

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                //真正要做的事情，创建订单
                Integer itemId = (Integer) ((Map)o).get("itemId");
                Integer userId = (Integer) ((Map)o).get("userId");
                Integer promoId = (Integer) ((Map)o).get("promoId");
                Integer amount = (Integer) ((Map)o).get("amount");
                String stockLogId = (String)((Map)o).get("stockLogId");
                try {
                    orderService.createOrder(userId,itemId,promoId,amount,stockLogId);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    //设置stockLog为回滚状态
                    StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                    stockLogDO.setStatus(3);
                    stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }

                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                //根据是否扣减库存成功，来判断返回COMMIT，ROLLBACK还是继续UNKNOWN
                String jsonString = new String(messageExt.getBody());
                Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");
                String stockLogId = (String)map.get("stockLogId");
                StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                if (stockLogDO == null){
                    return LocalTransactionState.UNKNOW;
                }
                if (stockLogDO.getStatus().intValue() == 2){
                    return LocalTransactionState.COMMIT_MESSAGE;
                }else if (stockLogDO.getStatus().intValue() == 1){
                    return LocalTransactionState.UNKNOW;
                }
                return LocalTransactionState.ROLLBACK_MESSAGE;

            }
        });
    }

    //事务型同步库存扣减
    public boolean transactionAsyncReduceStock(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId){
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        bodyMap.put("stockLogId", stockLogId);

        //传递消息参数
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("itemId", itemId);
        argsMap.put("amount", amount);
        argsMap.put("userId", userId);
        argsMap.put("promoId", promoId);
        argsMap.put("stockLogId", stockLogId);

        Message message = new Message(topicName, "increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        TransactionSendResult sendResult = null;
        try {
             sendResult = transactionMQProducer.sendMessageInTransaction(message, argsMap);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        if (sendResult.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE){
            return false;
        }else if (sendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE){
            return true;
        }else{
            return false;
        }

    }


    //同步库存扣减消息
    public boolean asyncReduceStock(Integer itemId, Integer amount) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        Message message = new Message(topicName, "increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        try {
            producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
