package edu.yuferov.chat.common;

import org.apache.commons.lang.StringUtils;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import static edu.yuferov.chat.common.Constants.EOF;

public class Utils {
    private Utils() {
    }

    private static final Pattern ipv4AddressPattern = Pattern.compile(
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private static final Pattern usernamePattern = Pattern.compile("^[A-Za-z_]{4,20}$");

    public static int findEndOfRequest(ByteBuffer buffer, int bytesRead) {
        final int begin = buffer.position() - bytesRead;
        final int end = buffer.position();
        for (int i = begin; i < end; i++) {
            if (buffer.get(i) == EOF) {
                return i;
            }
        }
        return -1;
    }

    public static void validateServerAddress(String address) {
        if (StringUtils.isBlank(address)) {
            throw new IllegalArgumentException("server address cannot be blank");
        }
        if (!ipv4AddressPattern.matcher(address).matches()) {
            throw new IllegalArgumentException("server address must be valid IPv4 address");
        }
    }

    public static void validateUserPassword(String password) {
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("user password can not be blank");
        }
        if (password.length() <= 6) {
            throw new IllegalArgumentException("password must have more than 6 symbols");
        }
    }

    public static void validateUserName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("username cannot be blank");
        }
        if (!usernamePattern.matcher(name).matches()) {
            throw new IllegalArgumentException("username can contains from 4 to 20 alphabetical " +
                    "and \"_\" symbols");
        }
    }
}
