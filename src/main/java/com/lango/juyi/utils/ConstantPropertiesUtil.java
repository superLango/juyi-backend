package com.lango.juyi.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Lango
 * @version 1.0
 * @date 2023/9/19 17:06
 * @description ConstantPropertiesUtil <description>
 */
@Component
public class ConstantPropertiesUtil implements InitializingBean {
    //InitializingBean在创建时读取配置文件的值实现赋值
    @Value("${tencent.cos.file.region}")
    private String region;
    @Value("${tencent.cos.file.secretid}")
    private String secretid;
    @Value("${tencent.cos.file.secretkey}")
    private String secretkey;
    @Value("${tencent.cos.file.bucketname}")
    private String bucketname;
    public static String END_POINT;
    public static String ACCESS_KEY_ID;
    public static String ACCESS_KEY_SECRET;
    public static String BUCKET_NAME;

    @Override
    public void afterPropertiesSet() throws Exception {
        END_POINT = region;
        ACCESS_KEY_ID = secretid;
        ACCESS_KEY_SECRET = secretkey;
        BUCKET_NAME = bucketname;
    }
}
