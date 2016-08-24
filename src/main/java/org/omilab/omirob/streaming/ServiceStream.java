package org.omilab.omirob.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class ServiceStream extends HttpServlet {
    private final static Logger logger = LoggerFactory.getLogger(ServiceStream.class);
    private static final int BLOCKSIZE = 4096;
    private byte buf[]=new byte[BLOCKSIZE];
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
        InputStream is = req.getInputStream();
        while (true) {
            int a=0;
            while(a<buf.length)
                a+=is.read(buf,a,buf.length-a);
            if(a!=buf.length)
                logger.warn("read() failed; size < BLOCKSIZE");
            WSSessions.send(buf);
        }
    }

}