
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class JettyWarMain {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("missing war file name");
        }

        // Ensure we have a "work" directory for the webapp
        if (System.getProperty("jetty.home") != null) {
            new File(System.getProperty("jetty.home"), "work").mkdirs();
        }

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setExtractWAR(true);
        webapp.setWar(args[0]);
        webapp.setDefaultsDescriptor(webdefaultPath());

        Server server = new Server();

        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(8443);
        http_config.setOutputBufferSize(32768);
        ServerConnector http = new ServerConnector(server,new HttpConnectionFactory(http_config));
        http.setPort(8080);
        http.setIdleTimeout(30000);
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(System.getProperty("jetty.home") + "/etc/keystore");
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

        ServerConnector https = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory,"http/1.1"),
            new HttpConnectionFactory(https_config));
        https.setPort(8443);
        https.setIdleTimeout(500000);


        //Connector connector = new SelectChannelConnector();
        //connector.setPort(Integer.getInteger("jetty.port",8080).intValue());
        //server.setConnectors(new Connector[]{connector});
        server.setConnectors(new Connector[] { http, https });
        server.setHandler(webapp);
        server.start();
        server.join();
    }

    private static String webdefaultPath() throws Exception {
        String path = System.getProperty("jetty.home", System.getProperty("java.io.tmpdir")) + System.getProperty("file.separator") + "webdefault.xml";
        FileOutputStream out = new FileOutputStream(path);
        InputStream is = JettyWarMain.class.getResourceAsStream("/webdefault.xml");
        try {
            byte[] buf = new byte[4096];
            int bytesRead = 0;
            while ((bytesRead = is.read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }
        } finally {
            is.close();
            out.close();
        }
        return path;
    }
}
