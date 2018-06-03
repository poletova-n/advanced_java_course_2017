package edu.technopolis.advancedjava.season2;


import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static edu.technopolis.advancedjava.season2.LogUtils.logException;

public class NewServer {
    private static final Map<SocketChannel, Phase> phases = new HashMap<>();
    private static final Map<SocketChannel, ByteBuffer> buffers = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); Selector selector = Selector.open()) {
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(10002));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                if (selectionKeys.isEmpty()) {
                    continue;
                }
                selectionKeys.removeIf(key -> {
                    if (!key.isValid()) {
                        return true;
                    }
                    if (key.isAcceptable()) {
                        accept(key);
                        return true;
                    }
                    Phase phase = phases.get(key.channel());
                    int selectionKey = 0;
                    if (key.isWritable()) {
                        selectionKey = SelectionKey.OP_WRITE;
                    } else if (key.isConnectable()) {
                        selectionKey = SelectionKey.OP_CONNECT;
                    } else if (key.isReadable()) {
                        selectionKey = SelectionKey.OP_READ;
                    }
                    phase.proceed(selectionKey, selector, phases);
                    return true;
                });
                phases.keySet().removeIf(channel -> !channel.isOpen());
            }

        } catch (IOException e) {
            logException("Error on connection", e);
        }
    }

    private static ByteBuffer getOrCreateBuffer(SocketChannel channel) {
        ByteBuffer byteBuffer = buffers.computeIfAbsent(channel, k -> ByteBuffer.allocate(300));
        return byteBuffer;
    }

    private static void accept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = null;
        try {
            socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(key.selector(), SelectionKey.OP_READ);
            phases.put(socketChannel, new AuthReadPhase(socketChannel, getOrCreateBuffer(socketChannel)));
        } catch (IOException e) {
            logException("Fail process channel: " + socketChannel, e);
            if (socketChannel != null) {
                closeChannel(socketChannel);
            }
        }
    }

    private static void closeChannel(SocketChannel socketChannel) {
        try {
            socketChannel.close();
        } catch (IOException e) {
            logException("Fail close channel: " + socketChannel, e);
        }
    }
}

