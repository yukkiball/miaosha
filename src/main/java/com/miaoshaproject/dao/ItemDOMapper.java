package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.ItemDO;

public interface ItemDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Wed Nov 13 10:31:02 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Wed Nov 13 10:31:02 CST 2019
     */
    int insert(ItemDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Wed Nov 13 10:31:02 CST 2019
     */
    int insertSelective(ItemDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Wed Nov 13 10:31:02 CST 2019
     */
    ItemDO selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Wed Nov 13 10:31:02 CST 2019
     */
    int updateByPrimaryKeySelective(ItemDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Wed Nov 13 10:31:02 CST 2019
     */
    int updateByPrimaryKey(ItemDO record);
}