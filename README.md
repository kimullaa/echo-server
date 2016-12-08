# echo-server

## Server
Socketを用いたECHOサーバ
- ノンブロッキングI/Oを利用したバージョン(シングルスレッド)
- ノンブロッキングI/Oを利用したバージョン(マルチスレッド)
- nioパッケージのノンブロッキングI/Oを利用したバージョン

## Clinet
- Socketを用いてServerに接続するクライアント(1行送信ごとに待機する)
- Socketを用いてServerに接続するクライアント(全行送信してから待機する)

## Servlet-api
- Servlet3.0のAsyncContext
- Servlet3.1のNonBlocking I/O
