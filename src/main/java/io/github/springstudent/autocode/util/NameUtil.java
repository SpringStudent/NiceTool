package io.github.springstudent.autocode.util;

import cn.hutool.core.util.StrUtil;

public class NameUtil {
    public static String toWords(String name) {
        return name.replaceAll("_", " ");
    }

    public static String formatName(String name) {
        return upFirstAll(toWords(name.toLowerCase())).replaceAll(" ", "");
    }

    public static String upFirstAll(String s) {
        s = s.trim().replaceAll("\\s", " ");
        String[] sa = s.split(" ");
        String r = "";
        for (String o : sa) {
            if (StrUtil.isNotBlank(o)) {
                r += upFirst(o);
            }
        }
        return r;
    }

    public static String upFirst(String o) {
        return o.replaceFirst(o.substring(0, 1), o.substring(0, 1).toUpperCase());
    }

    public static String lowFirst(String o) {
        return o.replaceFirst(o.substring(0, 1), o.substring(0, 1).toLowerCase());
    }

    public static String propertyName(String s) {
        return lowFirst(formatName(s));
    }


}
