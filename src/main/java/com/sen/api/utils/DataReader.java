package com.sen.api.utils;

import java.util.List;

/**
 * 数据读取器接口
 * 所有数据格式读取器的统一接口
 *
 * @author sen
 */
public interface DataReader {

    /**
     * 读取数据并转换为指定类型的对象列表
     *
     * @param clz  目标类型
     * @param path 文件路径
     * @param <T>  泛型类型
     * @return 对象列表
     */
    <T> List<T> read(Class<T> clz, String path);

    /**
     * 将数据写入文件
     *
     * @param data 数据对象
     * @param path 文件路径
     * @param <T>  泛型类型
     */
    <T> void write(List<T> data, String path);

    /**
     * 判断是否支持指定的文件格式
     *
     * @param path 文件路径
     * @return 是否支持
     */
    boolean supports(String path);

    /**
     * 获取支持的文件扩展名
     *
     * @return 扩展名数组
     */
    String[] getSupportedExtensions();
}
