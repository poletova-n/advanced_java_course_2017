package edu.technopolis.advancedjava.season2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static edu.technopolis.advancedjava.season2.LogUtils.logException;

public class ConnectWritePhase extends Phase {

    private final SocketChannel server;
    private final ByteBuffer serverBuffer;
    private final boolean accepted;

    public ConnectWritePhase(SocketChannel client, SocketChannel serv, ByteBuffer clientBuffer,
                             ByteBuffer serverBuffer, boolean accepted) {
        super(client, clientBuffer);
        this.server = serv;
        this.serverBuffer = serverBuffer;
        this.accepted = accepted;
    }

    @Override
    public void proceed(int operation, Selector selector, Map<SocketChannel, Phase> phases) {

        try {
            if (server == null) {
                selector.close();
                return;
            }
            if (!client.isOpen()) {
                server.close();
                selector.close();
                return;
            }
            while (buffer.hasRemaining()) {
                client.write(buffer);
            }
            if (accepted) {
                serverBuffer.flip();
                phases.put(client, new CommunicatePhase(client, server, buffer, serverBuffer));
                phases.put(server, new CommunicatePhase(server, client, serverBuffer, buffer));
                client.register(selector, SelectionKey.OP_READ);
                server.register(selector, SelectionKey.OP_READ);
                System.out.println("Connected in " + server.getRemoteAddress());
            } else {
                System.out.println("Connection reject");
            }
        } catch (IOException e) {
            logException(e.getMessage(), e);
        }
    }
}
