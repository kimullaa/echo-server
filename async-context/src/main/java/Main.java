import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import javax.servlet.ServletException;
import java.io.File;

public class Main {
    public static void main(String[] args) throws ServletException, LifecycleException {

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        // アプリケーションのルートとWEB資材配置場所の指定
        StandardContext ctx = (StandardContext) tomcat.addWebapp("/async-context", new File("src/main/webapp").getAbsolutePath());

        // WEB-INF/classes 相当のディレクトリにtarget/classesを追加して指定する
        File additionWebInfClasses = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        tomcat.start();
        tomcat.getServer().await();

    }
}
