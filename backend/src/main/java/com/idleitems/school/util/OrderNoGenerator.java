package com.idleitems.school.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class OrderNoGenerator {

    private OrderNoGenerator() {}

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String RANDOM_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    public static String generate() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        StringBuilder randomDigits = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            randomDigits.append(ThreadLocalRandom.current().nextInt(10));
        }

        StringBuilder randomChars = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            randomChars.append(RANDOM_CHARS.charAt(SECURE_RANDOM.nextInt(RANDOM_CHARS.length())));
        }

        return "ORD" + timestamp + randomDigits + randomChars;
    }
}
