package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.OrderModel;

/**
 * @Author：yuki
 * @Description:
 * @Date: Created in 19:46 2019/11/13
 * @Modified By:
 */
public interface OrderService {
    OrderModel crreateOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException;
}
