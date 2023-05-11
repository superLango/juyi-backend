package com.lango.juyi.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * 导入 excel 数据
 *
 * @author Lango
 * @version 1.0
 */
public class ImportExcel {
    /**
     * 读取数据
     */
    public static void main(String[] args) {

        String fileName = "文件相对地址";
        readByListener(fileName);
        synchronousRead(fileName);

    }

    /**
     * 监听器读取
     * @param fileName
     */
    public static void readByListener(String fileName) {
        EasyExcel.read(fileName, TableUserInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读取
     */
    public static void synchronousRead(String fileName) {
        List<TableUserInfo> totalDataList = EasyExcel.read(fileName).head(TableUserInfo.class).sheet().doReadSync();
        for (TableUserInfo tableUserInfo : totalDataList) {
            System.out.println(tableUserInfo);
        }
    }

}
