package server.blocking;


import lombok.extern.slf4j.Slf4j;
import server.EchoServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * ブロッキングI/Oを用いたEchoサーバ
 * ※ユーザの同時アクセス不可
 */
@Slf4j
public class BlockingAndSingleEchoServer implements EchoServer {
    @Override
    public void start() {
        try (ServerSocketChannel ssc = ServerSocketChannel.open();) {
            ssc.socket().bind(new InetSocketAddress(PORT));
            log.info("サーバを起動しました");
            while (true) {
                try (SocketChannel sc = ssc.accept();//ブロックされる
                     BufferedReader in = new BufferedReader(new InputStreamReader(sc.socket().getInputStream()));
                     PrintWriter out = new PrintWriter(sc.socket().getOutputStream(), true);
                ) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        log.info("recieved " + line + " from " + sc.socket().getRemoteSocketAddress());
                        log.info("echo " + line + " to " + sc.socket().getRemoteSocketAddress());
                        out.println(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
