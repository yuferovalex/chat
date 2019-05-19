package edu.yuferov.chat.common;

public class Constants {
    private Constants() {
    }

    public static final byte EOF = 0x1A;
    public static final int BUFFER_SIZE = 4096;
    public static final int SELECTOR_WAIT_TIME = 1000;
    public static final short DEFAULT_PORT = 5555;
}
