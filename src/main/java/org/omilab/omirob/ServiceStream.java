package org.omilab.omirob;

import com.sun.net.httpserver.HttpExchange;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ServiceStream extends HttpServlet {


    private static final int BLOCKSIZE = 4096;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
        InputStream is = req.getInputStream();
        while (true) {
            byte buf[]=new byte[BLOCKSIZE];
            int a=0;
            while(a<buf.length)
                a+=is.read(buf,a,buf.length-a);
            if(a!=buf.length)
                System.out.println("SDIOAHDFÄOHDF"+a);

            WSSessions.send(buf);
        }
    }

}