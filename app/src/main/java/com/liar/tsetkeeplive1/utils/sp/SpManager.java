package com.liar.tsetkeeplive1.utils.sp;

/**
 * SharedPreferences管理类
 * Created by zhouL on 2016/12/26.
 */
public class SpManager {

    private static SpManager mInstance = new SpManager();

    public static SpManager get() {
        return mInstance;
    }

    private SpManager() {}


    /**
     * 设置是否第一次安装
     * @param isFirst
     */
    public void setFIRST_INSTALL(String isFirst){
        SharedPreferencesUtils.putString(SpConfig.FIRST_INSTALL, isFirst);
    }

    /** 获取是否第一次安装 */
    public String getFIRST_INSTALL(){
        return SharedPreferencesUtils.getString(SpConfig.FIRST_INSTALL, "");

    }

}
