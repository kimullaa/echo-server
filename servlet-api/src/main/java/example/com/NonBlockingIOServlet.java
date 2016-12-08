package example.com;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebServlet(urlPatterns = "/non-blocking", asyncSupported = true)
public class NonBlockingIOServlet extends HttpServlet {

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("begin doGet");
        AsyncContext ctx = req.startAsync();
        ServletInputStream input = req.getInputStream();
        input.setReadListener(new NonBlockingReadListener(input, ctx));
        log.info("end doGet");
    }
}

@Slf4j
class NonBlockingReadListener implements ReadListener {
    private final ServletInputStream inputStream;
    private final AsyncContext ctx;

    NonBlockingReadListener(ServletInputStream inputStream, AsyncContext ctx) {
        log.info("ReadListener is initialized");
        this.inputStream = inputStream;
        this.ctx = ctx;
    }

    @Override
    public void onDataAvailable() throws IOException {
        log.info("onDataAvaliable");

        StringBuilder sb = new StringBuilder();
        int len = -1;
        byte b[] = new byte[1024];

        while (!inputStream.isFinished() && inputStream.isReady() && (len = inputStream.read(b)) != -1) {
            String data = new String(b, 0, len);
            log.info("recieved : " + data);
        }
    }

    @Override
    public void onAllDataRead() throws IOException {
        log.info("onAllDataRead");
        ctx.complete();
    }

    @Override
    public void onError(Throwable throwable) {
        log.info("onError : " + throwable);
        ctx.complete();
    }
}
