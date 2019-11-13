package com.miaoshaproject.error;

/**
 * @Author：yuki
 * @Description:
 * @Date: Created in 8:57 2019/11/12
 * @Modified By:
 */
public interface CommonError {
    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);
}
