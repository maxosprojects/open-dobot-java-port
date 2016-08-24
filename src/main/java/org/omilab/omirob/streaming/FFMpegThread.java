package org.omilab.omirob.streaming;

import java.io.*;
import java.util.Map;

/**
 * Created by Martin on 19.08.2016.
 */
public class FFMpegThread implements Runnable {

    @Override
    public void run() {
        try {
            runFFmpeg();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//    ffmpeg  -r 30 -s 640x360 -f v4l2  -i /dev/video0 -f mpeg1video  -b:v 1500k "http://localhost:8080/stream/input"

    //C:\ffmpeg\bin\ffmpeg -s 1280x720 -r 30 -f dshow -rtbufsize 15000k -frame_drop_threshold 3 -i video="Logitech HD Pro Webcam C920" -f mpeg1video -b 3000k -r 30 http://localhost:8080/stream/input
    private void runFFmpeg() throws IOException {

        ProcessBuilder pb =
                new ProcessBuilder("C:\\ffmpeg\\bin\\ffmpeg",
                        "-s", "640x360",
                        "-r", "30",
                        //"-re",
                        "-f", "dshow",
                        "-rtbufsize", "500k",
                        "-i", "video=Logitech HD Pro Webcam C920",
                        "-f", "mpeg1video",
                        "-b:v", "3000k",
                        "-r", "30", "http://localhost:8080/stream/input");
        pb.redirectErrorStream(true);
        Process p = pb.start();
        assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
        InputStream in = p.getInputStream();
        BufferedReader br=new BufferedReader(new InputStreamReader(in));
        while(true){
            String line=br.readLine();
            System.out.println(line);
        }

    }
}
