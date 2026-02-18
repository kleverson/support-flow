package br.com.supportflow.SupportFlow.common.util;

import java.security.SecureRandom;

public class TokenGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RNG = new SecureRandom();

    public static String randomAlphaNumeric(int length) {
        if (length <= 0) throw new IllegalArgumentException("length must be > 0");

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RNG.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

}
