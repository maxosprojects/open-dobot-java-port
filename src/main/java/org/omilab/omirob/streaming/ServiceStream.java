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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceStream extends HttpServlet implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(ServiceStream.class);
    private static Semaphore semSend;
    private final static Lock rcvLock=new ReentrantLock();
    private static volatile boolean _running = false;
    private static Thread sendThread;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
        rcvLock.tryLock();
        try {
            semSend = new Semaphore(0);
            sendThread = new Thread(this);
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
            rcvLock.unlock();
            shutDown();
            semSend=null;
            sendThread=null;
        }
    }

    private synchronized void shutDown()
    {
        _running = false;
        try {
            while(sendThread.isAlive()){
                sendThread.interrupt();
                logger.warn("Waiting for sendThread to die");
                sendThread.join(1000);
            }
        } catch (Exception e) {
            logger.warn("Shutdown sendthread", e);
        }
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