package com.smileshark.utils;

import opennlp.tools.util.StringUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class KeyUtils {
    public static String redisKeyUtils(Object... keys) {
        if (ObjectUtils.isEmpty(keys)) {
            throw new RuntimeException("key不能为空");
        }
        // 将所有的key转换并拼接
        return StringUtils.joinWith(":", keys);
    }
}
