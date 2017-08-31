package com.bravostudiodev.selenium;

import io.sterodium.rmi.protocol.client.RemoteNavigator;
import org.seleniumhq.jetty9.server.Server;
import org.seleniumhq.jetty9.servlet.ServletContextHandler;
import org.seleniumhq.jetty9.servlet.ServletHolder;

import javax.servlet.http.HttpServlet;
import java.util.Random;
import java.util.logging.Logger;

public class Entry {
    private static final Logger LOGGER = Logger.getLogger(BravoExtensionLightServlet.class.getName());

    static Server startServerForServlet(HttpServlet servlet, String path, int port) throws Exception {
        Server server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(servlet), path);
        server.start();

        return server;
    }

    public static void main(String args[]) throws Exception {
        if(args.length == 0) {
            LOGGER.severe("Usage: server|client [port]");
            return;
        }
        String basePath = "/" + BravoExtensionLightServlet.class.getSimpleName();
        Integer port = new Integer(args.length > 1 ? args[1] : "4480");
        if(args[0].equals("server")) {
            LOGGER.info("Starting server ...");
            Server sikuliServer = startServerForServlet(new BravoExtensionLightServlet(), basePath + "/*", port);
            sikuliServer.start();
            sikuliServer.join();
            LOGGER.info("... Closed server");
        } else {
            String proxyName = basePath + "/" + new Random().longs(10).toString();
            LOGGER.info("Connecting client ..." + proxyName);
            RemoteNavigator navigator = new RemoteNavigator("localhost", port, proxyName);

            LOGGER.info("Make screen proxy ...");
            SikuliScreen screen = navigator.createProxy(SikuliScreen.class, "screen");
            LOGGER.info("Proxy click ...");
            screen.click();

            LOGGER.info("Make files proxy ...");
            FileTransfer files = navigator.createProxy(FileTransfer.class, "files");
            files.readFile("/etc/apt/sources.list");
        }
    }
}
