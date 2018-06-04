package edu.technopolis.advancedjava.season2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;


import static edu.technopolis.advancedjava.season2.LogUtils.logException;

public class ConnectPendPhase extends Phase {

    private byte socksVersion = 0x05;
    private byte socksError = 0x01;
    private byte adressType = 0x01;
    private byte byteReserved = 0x00;
    private int bufferSize = 300;

    private final SocketChannel proxyToDestination;

    public ConnectPendPhase(SocketChannel client, ByteBuffer buffer, SocketChannel serv) {
        super(client, buffer);
        this.proxyToDestination = serv;
    }

    @Override
    public void proceed(int operation, Selector selector, Map<SocketChannel, Phase> phases) {

        try {
            if (!client.isOpen()) {
                proxyToDestination.close();
                return;
            }
            if (proxyToDestination.finishConnect()) {
                Phase phase = new ConnectWritePhase(client, proxyToDestination, buffer, ByteBuffer.allocate(bufferSize), true);
                phases.put(client, phase);
                phases.put(proxyToDestination, phase);
                proxyToDestination.register(selector, SelectionKey.OP_READ);
                client.register(selector, SelectionKey.OP_WRITE);
            } else {
                reject();
                client.register(selector, SelectionKey.OP_WRITE);
                phases.put(client, new ConnectWritePhase(client, proxyToDestination, buffer, ByteBuffer.allocate(bufferSize), false));
                proxyToDestination.close();
            }
        } catch (IOException e) {
            logException(e.getMessage(), e);
        }
    }

    private void reject() {
        buffer.clear();
        buffer.put(socksVersion).put(socksError).put(byteReserved)
                .put(adressType).put(new byte[4]).putShort((short) 0).flip();

    }
}

