package edu.yuferov.chat.server.io;

public interface DataListener {
    byte[] parseData(byte[] data) throws Exception;
}
