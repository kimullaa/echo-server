import server.EchoServer;
import server.blocking.BlockingAndMultiEchoServer;
import server.nonblocking.NonBlockingEchoServer;

/**
 * サーバ起動クラス
 */
public class Main {
    public static void main(String[] args) {
        EchoServer server = new BlockingAndMultiEchoServer();
        server.start();
    }
}
