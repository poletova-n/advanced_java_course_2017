package edu.technopolis.advancedjava.season2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static edu.technopolis.advancedjava.season2.LogUtils.logException;

public class AuthWritePhase extends Phase {
    private final boolean accepted;

    public AuthWritePhase(SocketChannel client, ByteBuffer buffer, boolean accepted) {
        super(client, buffer);
        this.accepted = accepted;
    }

    @Override
    public void proceed(int operation, Selector selector, Map<SocketChannel, Phase> phases) {

        try {
            if (!client.isOpen()) return;
            while (buffer.hasRemaining()) {
                client.write(buffer);
            }
            buffer.clear();
            if (accepted) {
                client.register(selector, SelectionKey.OP_READ);
                phases.put(client, new ConnectReadPhase(client, buffer));
            } else if (client.isOpen()) {
                client.close();
            }
        } catch (IOException e) {
            logException(e.getMessage(), e);
        }
    }
}
