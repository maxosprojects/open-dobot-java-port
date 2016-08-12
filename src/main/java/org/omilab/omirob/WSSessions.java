package org.omilab.omirob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Martin on 10.08.2016.
 */
public class WSSessions {
    private static List<ClientSession> sessions=new ArrayList<ClientSession>();
    private final static Logger logger = LoggerFactory.getLogger(WSSessions.class);


    private static final int BUFFER_SIZE_MB=1;
    private static final int BUFFER_SIZE_BYTE=BUFFER_SIZE_MB*1024*1024;
    private static final byte[] buffer=new byte[BUFFER_SIZE_BYTE];
    private static volatile int writepos=0;


    public static void addSession(Session session){
       synchronized (sessions) {
           ClientSession s = new ClientSession(session, buffer);
           sessions.add(s);
           logger.info(String.format("Client added: %s; count: %d",s.getSession().getId(),sessions.size()));
       }
    }

    public synchronized static void send(byte[] data){
        if(data.length+writepos>BUFFER_SIZE_BYTE){
            int remaining=BUFFER_SIZE_BYTE-writepos;
            System.arraycopy(data, 0, buffer, writepos, remaining);
            System.arraycopy(data, remaining, buffer, 0, data.length-remaining);
        }
        else
            System.arraycopy(data, 0, buffer, writepos, data.length);

        writepos=(writepos+data.length)%BUFFER_SIZE_BYTE;

        synchronized (sessions){
            Iterator<ClientSession> i = sessions.iterator();
            while (i.hasNext()) {
                ClientSession s = i.next(); // must be called before you can call i.remove()
                s.sendAsync(writepos);
                if(!s.isAlive()){
                    i.remove();
                    logger.info(String.format("Client removed: %s; count: %d",s.getSession().getId(),sessions.size()));

                }

            }}
    }


}
