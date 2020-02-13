package com.miaoshaproject.controller;

import com.miaoshaproject.config.RedisConfig;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author：yuki
 * @Description:
 * @Date: Created in 21:02 2019/11/13
 * @Modified By:
 */

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true", origins = {"*"})
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MqProducer mqProducer;

    @Autowired
    ItemService itemService;

    //封装下单请求
    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name="itemId")Integer itemId,
                                        @RequestParam(name="amount")Integer amount,
                                        @RequestParam(name = "promoId", required = false) Integer promoId) throws BusinessException {
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录不能下单");
        }
        //获取用户的登录信息
        UserModel userModel = (UserModel)redisTemplate.opsForValue().get(token);

//        Boolean isLogin = (Boolean)httpServletRequest.getSession().getAttribute("IS_LOGIN");
        //会话已经过期，需要重新登录
        if (userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "会话已过期，请重新登录");
        }

        //UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");
//        OrderModel orderModel =  orderService.createOrder(userModel.getId(), itemId, promoId, amount);

        //加入库存流水init状态
        String stockLogId = itemService.initStockLog(itemId, amount);


        //再去完成相应的下单事务型消息机制
        if (!mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, promoId, amount, stockLogId)){
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
        }
        return CommonReturnType.create(null);
    }
}
