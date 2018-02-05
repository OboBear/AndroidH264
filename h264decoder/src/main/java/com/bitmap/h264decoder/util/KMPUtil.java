package com.bitmap.h264decoder.util;

/**
 * @author obo
 * @date 2018/2/4
 */
public class KMPUtil {
    public static int KMPMatch(byte[] pattern, byte[] bytes, int start, int remain) {

        int[] lsp = computeLspTable(pattern);

        int j = 0;
        for (int i = start; i < remain; i++) {
            while (j > 0 && bytes[i] != pattern[j]) {
                j = lsp[j - 1];
            }
            if (bytes[i] == pattern[j]) {
                j++;
                if (j == pattern.length) {
                    return i - (j - 1);
                }
            }
        }
        return -1;
    }

    private static int[] computeLspTable(byte[] pattern) {
        int[] lsp = new int[pattern.length];
        lsp[0] = 0;
        for (int i = 1; i < pattern.length; i++) {
            int j = lsp[i - 1];
            while (j > 0 && pattern[i] != pattern[j]) {
                j = lsp[j - 1];
            }
            if (pattern[i] == pattern[j]) {
                j++;
            }
            lsp[i] = j;
        }
        return lsp;
    }
}
