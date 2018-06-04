package edu.technopolis.advancedjava.season2;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.util.Map;

public abstract class Phase {
    protected final SocketChannel client;
    protected final ByteBuffer buffer;

    Phase(SocketChannel clientChannel, ByteBuffer clientBuffer) {
        client = clientChannel;
        buffer = clientBuffer;
    }

    public abstract void proceed(int operation, Selector selector, Map<SocketChannel, Phase> stages);
}
