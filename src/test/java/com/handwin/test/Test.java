package com.handwin.test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by piguangtao on 15/11/24.
 */
public class Test {

    @org.junit.Test
    public void test1() {
//        String nickName = "test";
//        String groupName = null;
//        nickName = String.format("%s(%s)", nickName, groupName != null ? groupName : "");
//        System.out.println(nickName);

        Map<String,Object> test = new HashMap<>();
        test.put("dada",20);
        test.put("dadada","dfasdfa");
        System.out.println(test.entrySet().toString());


    }
}
