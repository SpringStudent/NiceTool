package io.github.springstudent.autocode.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Matcher {

    public static List<String> match(String regString, int index, String content){
        List<String> r = new ArrayList<String>();
        java.util.regex.Matcher mr = Pattern.compile(regString,Pattern.DOTALL+Pattern.CASE_INSENSITIVE).matcher(content);
        while (mr.find()) {
            r.add(mr.group(index));
        }
        return r;
    }

    public static List<String> match(String regString, String content){
        List<String> r = new ArrayList<String>();
        java.util.regex.Matcher mr = Pattern.compile(regString,Pattern.CASE_INSENSITIVE + Pattern.DOTALL).matcher(content);
        outer: while (mr.find()) {
            inner : for(int i=0;i<mr.groupCount();i++) {
                r.add(mr.group(i));
            }
        }
        return r;
    }
}
