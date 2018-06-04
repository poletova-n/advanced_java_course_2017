package edu.technopolis.advancedjava.season2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;


import static edu.technopolis.advancedjava.season2.LogUtils.logException;

public class ConnectReadPhase extends Phase {

    private byte socksVersion = 0x05;
    private byte socksError = 0x01;
    private byte adressType = 0x01;
    private byte byteReserved = 0x00;
    private int bufferSize = 300;
    private byte commandNum = 0x01;


    public static final byte CONNECTION_PROVIDED_CODE = 0x00;

    public ConnectReadPhase(SocketChannel client, ByteBuffer buffer) {
        super(client, buffer);
    }

    @Override
    public void proceed(int operation, Selector selector, Map<SocketChannel, Phase> phases) {

        try {
            if (!client.isOpen()) {
                return;
            }
            int bytes = client.read(buffer);
            buffer.flip();
            if (bytes == -1) {
                client.close();
                System.out.println("Close: " + client.toString());
                return;
            }
            if (buffer.position() >= 10 || buffer.get() != socksVersion || buffer.get() != commandNum
                    || buffer.get() != byteReserved || buffer.get() != adressType) {
                rejectAndHandle(selector, phases);
                return;
            }


            byte[] ipv4 = new byte[4];
            buffer.get(ipv4);
            short port = buffer.getShort();

            SocketChannel server = SocketChannel.open();
            server.configureBlocking(false);
            server.connect(new InetSocketAddress(InetAddress.getByAddress(ipv4), port));

            accept(ipv4, port);

            Phase phase = new ConnectPendPhase(client, buffer, server);
            phases.put(client, phase);
            phases.put(server, phase);

            server.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException e) {
            logException(e.getMessage(), e);
        }
    }

    private void accept(byte[] ip, short port) {
        buffer.clear();
        buffer.put(socksVersion).put(CONNECTION_PROVIDED_CODE).put(byteReserved)
                .put(adressType).put(ip).putShort(port).flip();
    }

    private void reject() {
        buffer.clear();
        buffer.put(socksVersion).put(socksError).put(byteReserved)
                .put(adressType).put(new byte[4]).putShort((short) 0).flip();
    }

    private void rejectAndHandle(Selector selector, Map<SocketChannel, Phase> phases) throws ClosedChannelException {
        reject();
        phases.put(client, new ConnectWritePhase(client, null, buffer, null, false));
        client.register(selector, SelectionKey.OP_WRITE);
    }
}
