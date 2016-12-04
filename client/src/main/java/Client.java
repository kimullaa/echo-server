import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * サーバにブロッキングI/Oを利用して接続する
 * コンソールからの入力を行単位でサーバに送信する
 * 改行が2こ続けば切断する
 */
@Slf4j
public class Client {
    public static void main(String[] args) {
        perLine();
    }

    /**
     * サーバへ1行送信するたびに受信を待つ
     */
    public static void perLine() {
        try (
                Socket socket = new Socket("localhost", 8080);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedReader keyIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
            log.info("サーバに接続しました " + socket.getLocalSocketAddress() + " to " + socket.getRemoteSocketAddress());

            String input;
            while ((input = keyIn.readLine()).length() > 0) {
                out.println(input);
                String line = in.readLine();
                if (line != null) {
                    log.info("recieved " + line + " from " + socket.getRemoteSocketAddress());
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * サーバへの送信がすべて完了してから受信を待つ
     */
    public static void batch() {
        try {
            try (
                    Socket socket = new Socket("localhost", 8080);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedReader keyIn = new BufferedReader(new InputStreamReader(System.in));
            ) {
                log.info("サーバに接続しました " + socket.getLocalSocketAddress() + " to " + socket.getRemoteSocketAddress());

                String input;
                while (!(input = keyIn.readLine()).equals("enter")) {
                    out.write(input);
                }
                out.close();

                while (true) {
                    input = in.readLine();
                    if (input == null) {
                        break;
                    }
                    log.info(input);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}