package org.omilab.omirob;

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
    private final Session session;
    private ByteBuffer byteBuffer;
    private volatile int writepos=0;
    private volatile boolean alive=true;
    private Lock lock=new ReentrantLock();

    public ClientSession(Session session, byte[] buffer) {
        this.session=session;
        byteBuffer=ByteBuffer.wrap(buffer);
    }

    public void sendAsync(int writepos) {
        this.writepos=writepos;
        try {
            if(lock.tryLock(0, TimeUnit.SECONDS))
                send();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void send(){
        //0 <= mark <= position <= limit <= capacity
        if(byteBuffer.position()==byteBuffer.capacity()){ //reset to zero if at end of buffer
            byteBuffer.limit(writepos);
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
        else
            lock.unlock();
    }

    @Override
    public void onResult(SendResult sendResult) {
        lock.unlock();
        if(sendResult.isOK()){
            lock.lock();
            send();
        }

        else alive=false;

    }

    public boolean isAlive() {
        return alive;
    }

    public Session getSession() {
        return session;
    }
}
