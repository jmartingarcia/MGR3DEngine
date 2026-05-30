package com.mgr.engine;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public class Utils {

    public static String loadResource(String fileName) throws Exception {
        String result;
        try (InputStream in = Utils.class.getResourceAsStream(fileName);
                Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name())) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }

    public static float[] listFloatsToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }

    public static int[] listIntegersToArray(List<Integer> list) {
        int size = list != null ? list.size() : 0;
        int[] intArr = new int[size];
        for (int i = 0; i < size; i++) {
            intArr[i] = list.get(i);
        }
        return intArr;
    }

}
