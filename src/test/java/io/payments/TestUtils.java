package io.payments;

import java.io.IOException;
import java.net.ServerSocket;

public final class TestUtils {
    public static Integer findRandomOpenPortOnAllLocalInterfaces() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return findRandomOpenPortOnAllLocalInterfaces();
        }
    }
}
