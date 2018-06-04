package edu.technopolis.advancedjava.season2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static edu.technopolis.advancedjava.season2.LogUtils.logException;

public class CommunicatePhase extends Phase {

    private final SocketChannel server;
    private final ByteBuffer serverBuffer;

    public CommunicatePhase(SocketChannel client, SocketChannel server,
                              ByteBuffer clientBuffer, ByteBuffer serverBuffer) {
        super(client, clientBuffer);
        this.server = server;
        this.serverBuffer = serverBuffer;
    }

    @Override
    public void proceed(int operation, Selector selector, Map<SocketChannel, Phase> phases) {

        try {
            if (!client.isOpen()) {
                server.close();
                return;
            }
            if (!server.isOpen()) {
                client.close();
                return;
            }
            if (operation == SelectionKey.OP_READ) {
                if (buffer.hasRemaining()) {
                    return;
                }
                buffer.clear();
                int bytes = client.read(buffer);
                buffer.flip();
                if (bytes > 0) {
                    server.register(selector, SelectionKey.OP_WRITE);
                } else {
                    client.close();
                    server.close();
                }
            } else if (operation == SelectionKey.OP_WRITE) {
                while (serverBuffer.hasRemaining()) {
                    client.write(serverBuffer);
                }
                client.register(selector, SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            logException(e.getMessage(), e);
        }
    }
}
