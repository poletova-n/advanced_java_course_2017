package edu.technopolis.advancedjava.season2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;


import static edu.technopolis.advancedjava.season2.LogUtils.logException;

public class AuthReadPhase extends Phase {

    private byte socksVersion = 0x05;
    private byte authMethod = 0x00;
    private byte authReject = 0xF;

    public AuthReadPhase(SocketChannel clientChannel, ByteBuffer clientBuffer) {
        super(clientChannel, clientBuffer);
        this.buffer.clear();
    }

    private void accept(Map<SocketChannel, Phase> phases) {
        buffer.clear();
        buffer.put(socksVersion).put(authMethod).flip();
        phases.put(client, new AuthWritePhase(client, buffer, true));
    }


    @Override
    public void proceed(int operation, Selector selector, Map<SocketChannel, Phase> phases) {

        try {
            int bytes = client.read(buffer);
            if (bytes < 3) {
                return;
            }
            buffer.flip();
            if (buffer.get() == socksVersion && isAcceptableAuthMethod(buffer)) {
                accept(phases);
            } else {
                reject(phases);
            }
            client.register(selector, SelectionKey.OP_WRITE);
        } catch (IOException e) {
            logException(e.getMessage(), e);
        }
    }

    private void reject(Map<SocketChannel, Phase> phases) {
        buffer.clear();
        buffer.put(socksVersion).put(authReject).flip();
        phases.put(client, new AuthWritePhase(client, buffer, false));

    }


    private boolean isAcceptableAuthMethod(ByteBuffer bb) {
        byte method = bb.get();
        if (method < 1) {
            return false;
        }
        for (int i = 0; i < method; i++) {
            if (bb.get() == authMethod) {
                return true;
            }
        }
        return false;
    }
}
