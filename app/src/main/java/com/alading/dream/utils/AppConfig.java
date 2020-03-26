package com.alading.dream.utils;

import android.content.res.AssetManager;

import com.alading.dream.data.model.Destination;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class AppConfig {

    private static HashMap<String, Destination> sDestConfig;

    public static HashMap<String, Destination> getDestConfig() {
        if (sDestConfig == null) {

            String content = parseFile("destination.json");


            sDestConfig = JSON.parseObject(content, new TypeReference<HashMap<String, Destination>>() {
            });
        }

        return sDestConfig;
    }


    private static String parseFile(String fileName) {
        AssetManager assetsManager = AppGlobals.getApplication().getResources().getAssets();
        InputStream stream = null;
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            stream = assetsManager.open(fileName);
            reader = new BufferedReader(new InputStreamReader(stream));
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }
}
