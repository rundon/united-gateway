package com.onefly.gateway.utils;


/**
 * 字符串工具集合
 */
public class StringUtils {

    /**
     * 从文件路径中获取文件名
     *
     * @param filepath 文件路径
     * @return 文件名, 失败返回空字符串
     */
    public static String getFileName(String filepath) {
        if (!isEmpty(filepath)) {
            return filepath.substring(filepath.lastIndexOf("\\") + 1, filepath.length());
        }
        return "";
    }

    /**
     * 把字符串第一个字母转成大写
     *
     * @param str 需要转换的字符串
     * @return 首字母大写的字符串
     */
    public static String getFirstUpper(String str) {
        String newStr = "";
        if (str.length() > 0) {
            newStr = str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
        }
        return newStr;
    }

    /**
     * 把字符串的第一个字符转换为小写
     *
     * @param str 需要转换的字符串
     * @return 转换的字符串
     */
    public static String getFirstLower(String str) {
        if (isEmpty(str)) {
            return "";
        } else {
            return str.substring(0, 1).toLowerCase() + str.substring(1, str.length());
        }
    }

    /**
     * 将驼峰式命名的字符串转换为下划线大写方式。如果转换前的驼峰式命名的字符串为空，则返回空字符串。
     * 例如：HelloWorld to HELLO_WORLD
     *
     * @param name 转换前的驼峰式命名的字符串
     * @return 转换后下划线大写方式命名的字符串
     */
    public static String underscoreName(String name) {
        if (isEmpty(name)) {
            return "";
        }
        String regex = "([a-z])([A-Z])";
        String replacement = "$1_$2";
        return name.replaceAll(regex, replacement).toUpperCase();
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串.
     * 例如：HELLO_WORLD to HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String camelName(String name) {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty()) {
            // 没必要转换
            return "";
        } else if (!name.contains("_")) {
            // 不含下划线，仅将首字母小写
            return name.substring(0, 1).toLowerCase() + name.substring(1);
        }
        // 用下划线将原始字符串分割
        String camels[] = name.split("_");
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty()) {
                continue;
            }
            // 处理真正的驼峰片段
            if (result.length() == 0) {
                // 第一个驼峰片段，全部字母都小写
                result.append(camel.toLowerCase());
            } else {
                // 其他的驼峰片段，首字母大写
                result.append(camel.substring(0, 1).toUpperCase());
                result.append(camel.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * 获取字符串的ASCII编码的长度
     *
     * @param s java Unicode 编码的字符串
     * @return ASCII编码的字符串长度
     */
    public static int getWordCount(String s) {
        s = s.replaceAll("[^\\x00-\\xff]", "**");
        return s.length();
    }

    /**
     * 转换为ASCII编码的字符串
     *
     * @param value Unicode 编码的字符串
     * @return ASCII编码的字符串
     */
    public static String stringToAscii(String value) {
        StringBuilder sbu = new StringBuilder();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((int) chars[i]);
            } else {
                sbu.append((int) chars[i]);
            }
        }
        return sbu.toString();
    }


    /**
     * 判断字符串是否为空，如果是空返回true，否则返回false string == null true string == "    " true
     * string == "" true
     *
     * @param string 需要判断的字符串
     * @return boolean 判断结果
     */
    public static boolean isEmpty(String string) {
        return (string == null) || (string.trim().isEmpty());
    }
}
