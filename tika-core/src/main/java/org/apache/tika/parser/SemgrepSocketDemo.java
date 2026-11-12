
package org.apache.tika.parser;

import java.io.IOException;
import java.net.Socket;

public final class SemgrepSocketDemo {

    private SemgrepSocketDemo() {
    }

    public static Socket openInsecureSocket(
            String host, int port) throws IOException {

        return new Socket(host, port);
    }
}
