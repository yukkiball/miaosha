package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.PromoDOMapper;
import com.miaoshaproject.dao.RedisDao;
import com.miaoshaproject.dataobject.PromoDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author：yuki
 * @Description:
 * @Date: Created in 10:14 2019/11/14
 * @Modified By:
 */
@Service
public class PromoServiceImpl implements PromoService {

    @Autowired(required = false)
    private PromoDOMapper promoDOMapper;

//    @Autowired
//    private RedisDao redisDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {

        //获取对应商品的秒杀活动信息
        //通过redis缓存优化
//        PromoDO promoDO = redisDao.getPromoDo(itemId);
//        if (promoDO == null){
//            promoDO = promoDOMapper.selectByItemId(itemId);
//            if (promoDO == null){
//                return null;
//            }else{
//                String res = redisDao.putPromoDo(promoDO);
//
//            }
//        }

        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        if (promoDO == null){
            return null;
        }

        //dataobeject->model
        PromoModel promoModel = convertFromDataObeject(promoDO);
        if (promoModel == null){
            return null;
        }
        //判断当前时间是否秒杀活动即将开始或正在进行
        DateTime now = new DateTime();
        if (promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }

        return promoModel;

    }

    @Override
    public void publishPromo(Integer promoId) {
        //通过活动id获取活动
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if (promoDO.getItemId() == null || promoDO.getItemId().intValue() == 0){
            return;
        }
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());
        //降库存同步到redis内
        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(), itemModel.getStock());

    }

    @Override
    public String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId) {
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);

        //dataobeject->model
        PromoModel promoModel = convertFromDataObeject(promoDO);
        if (promoModel == null){
            return null;
        }
        //判断当前时间是否秒杀活动即将开始或正在进行
        DateTime now = new DateTime();
        if (promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }
        //判断活动是否在进行
        if (promoModel.getStatus() != 2){
            return null;
        }
        //判断item信息是否存在
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null){
            return null;
        }

        //判断用户是否存在
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null){
            return null;
        }

        //生成token,存入redis内
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("promo_token_"+promoId+"_user_"+userId+"_item_"+itemId, token);
        redisTemplate.expire("promo_token_"+promoId+"_user_"+userId+"_item_"+itemId, 5, TimeUnit.MINUTES);
        return token;
    }

    private PromoModel convertFromDataObeject(PromoDO promoDO){
        if (promoDO == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));

        return promoModel;
    }

}
