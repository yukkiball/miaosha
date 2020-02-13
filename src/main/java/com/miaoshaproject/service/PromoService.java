package com.miaoshaproject.service;

import com.miaoshaproject.service.model.PromoModel;

/**
 * @Author：yuki
 * @Description:
 * @Date: Created in 10:11 2019/11/14
 * @Modified By:
 */
public interface PromoService {
    //根据itemId获取即将进行或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);

    //活动发布
    void publishPromo(Integer promoId);
}
