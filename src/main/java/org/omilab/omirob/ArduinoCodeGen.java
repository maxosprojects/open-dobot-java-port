package org.omilab.omirob;

import org.omilab.omirob.codegen.DispatchGen;
import org.omilab.omirob.codegen.HeaderGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Martin on 22.07.2016.
 */
public class ArduinoCodeGen {
    private final static Logger logger = LoggerFactory.getLogger(ArduinoCodeGen.class);

    public static void main(String[] args){
        Class c=IRobot.class;
        String name=c.getSimpleName();
        Path path=Paths.get(args[0]);
        new File(path.toString()).mkdirs();
        try {
            Files.write(path.resolve(name+".h"), HeaderGen.generate(c).getBytes(StandardCharsets.US_ASCII));
            Files.write(path.resolve("dispatch.h"), DispatchGen.generate(c).getBytes(StandardCharsets.US_ASCII));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
