package org.omilab.omirob.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Martin on 12.08.2016.
 */
public class ClientSession implements SendHandler {
    private final static Logger logger = LoggerFactory.getLogger(ClientSession.class);

    private final Session session;
    private ByteBuffer byteBuffer;
    private volatile int writepos=0;
    private volatile boolean alive=true;

    public ClientSession(Session session, byte[] buffer, int writepos) {
        this.session=session;
        byteBuffer=ByteBuffer.wrap(buffer);
        byteBuffer.position(writepos);
        byteBuffer.limit(writepos);
    }

    synchronized void send(int writepos){
        //0 <= mark <= position <= limit <= capacity
        this.writepos=writepos;

        if(byteBuffer.position()==byteBuffer.capacity()){ //reset to zero if at end of buffer
            byteBuffer.limit(byteBuffer.capacity());
            byteBuffer.position(0);
        }

        if(writepos<byteBuffer.position()){ //buffer must have wrapped, send remaining bytes
            byteBuffer.limit(byteBuffer.capacity());
        }
        else{ //default case: send buffer until writepos
            byteBuffer.limit(writepos);
        }
        if(byteBuffer.remaining()>0)
            session.getAsyncRemote().sendBinary(byteBuffer, this);
    }

    @Override
    public void onResult(SendResult sendResult) {
        if(!sendResult.isOK())
            alive=false;
    }

    public boolean isAlive() {
        return alive;
    }

    public Session getSession() {
        return session;
    }
}
