package server.blocking;


import lombok.extern.slf4j.Slf4j;
import server.EchoServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;

/**
 * ブロッキングI/Oを用いたEchoサーバ
 * ※ 同時に複数リクエストをさばくことができる
 */
@Slf4j
public class BlockingAndMultiEchoServer implements EchoServer {
    @Override
    public void start() {
        try (ServerSocketChannel ssc = ServerSocketChannel.open();) {
            ssc.socket().bind(new InetSocketAddress(PORT));
            log.info("サーバを起動しました " + ssc.socket().getLocalSocketAddress());
            while (true) {
                Socket socket = ssc.socket().accept();
                log.info("別スレッドを起動します from " + socket.getRemoteSocketAddress());
                new Thread(new EchoTask(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class EchoTask implements Runnable {
        private final Socket socket;

        EchoTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        log.info("recieved " + line + " from " + socket.getRemoteSocketAddress());
                        log.info("echo " + line + " to " + socket.getRemoteSocketAddress());
                        out.println(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
