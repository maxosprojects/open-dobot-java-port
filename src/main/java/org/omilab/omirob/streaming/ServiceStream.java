package org.omilab.omirob.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;

public class ServiceStream extends HttpServlet implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(ServiceStream.class);
    private Semaphore semSend;
    private volatile boolean _running = false;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
        synchronized (ServiceStream.class) {
            try {
                semSend = new Semaphore(0);
                Thread sendThread = new Thread(this);
                sendThread.setName("Websocket send thread");
                _running = true;
                sendThread.start();
                InputStream is = req.getInputStream();
                while (true) {
                    int count=WSSessions.feed(is);
                    if(count<0)
                        throw new EOFException("EOF from: "+req.getRemoteAddr());
                    semSend.release();
                }
            } catch (IOException e) {
                logger.warn("Input Stream error", e);
            } finally {
                shutDown();
            }
        }
    }

    private void shutDown() {
        _running = false;
    }

    @Override
    public void run() {
        try {
            while (_running) {
                semSend.acquire();
                WSSessions.send();
            }
        } catch (InterruptedException e) {
            logger.info("Send Thread interrupted", e);
        }
        logger.info("Send Thread terminating.");
    }
}