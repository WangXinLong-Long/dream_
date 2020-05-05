package com.alading.dream.ui.sofa;

import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        Map<String,String> map = new HashMap<>();
        map.put("1","111");
        map.put("2","222");
        map.put("3","333");
        map.put("1","444");
        map.put("2","222");

        System.out.println(map.size());
        System.out.println(map.toString());
    }
}
