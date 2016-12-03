package server.nonblocking;

import lombok.extern.slf4j.Slf4j;
import server.EchoServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * ノンブロッキングI/Oを用いたEchoサーバ
 * ※ 同時に複数リクエストをさばくことができる
 */
@Slf4j
public class NonBlockingEchoServer implements EchoServer {
    @Override
    public void start() {
        try (ServerSocketChannel ssc = ServerSocketChannel.open();
             Selector selector = Selector.open();) {
            // ノンブロッキングモードにしてSelectorに受付チャネルを登録する
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(PORT));
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            log.info("サーバが起動しました " + ssc.socket().getLocalSocketAddress());

            // チャネルにイベントが登録されるまで待つ
            while (selector.select() > 0) {
                for (Iterator it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                    SelectionKey key = (SelectionKey) it.next();
                    it.remove();

                    if (key.isAcceptable()) {
                        doAccept((ServerSocketChannel) key.channel(), selector);
                    } else if (key.isReadable()) {
                        doRead((SocketChannel) key.channel(), selector);
                    } else if (key.isWritable()) {
                        byte[] message = (byte[]) key.attachment();
                        doWrite((SocketChannel) key.channel(), selector, message);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void doAccept(ServerSocketChannel ssc, Selector selector) {
        try {
            SocketChannel channel = ssc.accept();
            log.info("connected " + channel.socket().getRemoteSocketAddress());
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doRead(SocketChannel channel, Selector selector) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // ソケットから入力を読み込む
            // コネクションが切れていればチャネルをクローズし、読み込めなければリターンする
            int readBytes = channel.read(buffer);
            if (readBytes == -1) {
                log.info("disconnected " + channel.socket().getRemoteSocketAddress());
                channel.close();
                return;
            }
            if (readBytes == 0) {
                return;
            }

            // 入力されたメッセージを取り出し、チャネルに登録する
            buffer.flip();
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes);

            String line = new String(buffer.array(), "UTF-8").replaceAll(System.getProperty("line.separator"), "");
            log.info("recieved " + line + " from " + channel.socket().getRemoteSocketAddress());

            channel.register(selector, SelectionKey.OP_WRITE, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doWrite(SocketChannel channel, Selector selector, byte[] message) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(message);
            channel.write(byteBuffer);
            ByteBuffer restByteBuffer = byteBuffer.slice();

            // ログに送信したメッセージを表示する
            byteBuffer.flip();
            byte[] sendBytes = new byte[byteBuffer.limit()];
            byteBuffer.get(sendBytes);
            String line = new String(sendBytes, "UTF-8").replaceAll(System.getProperty("line.separator"), "");
            log.info("echo " + line + " to " + channel.socket().getRemoteSocketAddress());

            // メッセージを最後まで出力したら入力を受け付ける
            if (restByteBuffer.hasRemaining()) {
                byte[] restBytes = new byte[restByteBuffer.limit()];
                restByteBuffer.get(restBytes);
                channel.register(selector, SelectionKey.OP_WRITE, restBytes);
            } else {
                channel.register(selector, SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
