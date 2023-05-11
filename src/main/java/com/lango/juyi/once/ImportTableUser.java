package com.lango.juyi.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入用户到数据库
 *
 * @author Lango
 * @version 1.0
 */
public class ImportTableUser {
    public static void main(String[] args) {
        String fileName = "文件相对地址";
        List<TableUserInfo> userInfoList =
                EasyExcel.read(fileName)
                        .head(TableUserInfo.class).sheet().doReadSync();
        System.out.println("总数 = " + userInfoList.size());
        Map<String, List<TableUserInfo>> listMap =
                userInfoList.stream()
                        .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                        .collect(Collectors.groupingBy(TableUserInfo::getUsername));
        for (Map.Entry<String,List<TableUserInfo>> stringListEntry : listMap.entrySet()) {
            if (stringListEntry.getValue().size() > 1){
                System.out.println("username = " + stringListEntry.getKey());
            }
        }
        System.out.println("不重名昵称数 = " + listMap.keySet().size());
    }
}
