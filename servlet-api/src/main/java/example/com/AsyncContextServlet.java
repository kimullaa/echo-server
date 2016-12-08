package example.com;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@WebServlet(urlPatterns = "/async-context", asyncSupported = true)
public class AsyncContextServlet extends HttpServlet {
    Executor executor = new ScheduledThreadPoolExecutor(10);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("begin doGet");
        AsyncContext ac = req.startAsync();
        executor.execute(new SlowTask(ac));
        log.info("end doGet");
    }

}

@Slf4j
class SlowTask implements Runnable {
    private AsyncContext ac;

    SlowTask(AsyncContext ac) {
        this.ac = ac;
    }

    @Override
    public void run() {
        log.info("begin AsyncContext#start");

        try {
            PrintWriter writer = ac.getResponse().getWriter();
            for (int i = 0; i < 5; i++) {
                writer.println("task :" + i);
                writer.flush();
                log.info("send chuncked data");
                TimeUnit.SECONDS.sleep(2);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        ac.complete();
        log.info("end AsyncContext#start");
    }
}
