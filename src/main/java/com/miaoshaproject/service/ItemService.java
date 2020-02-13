package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

/**
 * @Author：yuki
 * @Description:
 * @Date: Created in 15:01 2019/11/13
 * @Modified By:
 */
public interface ItemService {

    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    //商品列表浏览
    List<ItemModel> listItem();

    //验证item及promo model缓存模型
    ItemModel getItemByIdInCache(Integer id);

    //商品详情浏览
    ItemModel getItemById(Integer id);

    //异步更新库存,更新数据库
    boolean asyncDecreaseStock(Integer itemId, Integer amount);

    //库存扣减
    boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException;

    //库存回补
    boolean increaseStock(Integer itemId, Integer amount) throws BusinessException;

    //商品销量增加
    void increaseSales(Integer itemId,Integer amount)throws BusinessException;

    //初始化库存流水
    String initStockLog(Integer itemId, Integer amount);
}
