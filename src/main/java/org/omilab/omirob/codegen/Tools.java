package org.omilab.omirob.codegen;

import java.nio.charset.StandardCharsets;

/**
 * Created by Martin on 22.07.2016.
 */
public class Tools {
    public static String typeToC(Class<?> type){
        if(type==Void.TYPE){
            return "void";
        }
        else if(type==Integer.TYPE){
            return "int32_t";
        }
        else if(type==Short.TYPE){
            return "int16_t";
        }
        else if(type==Byte.TYPE){
            return "int8_t";
        }
        else if(type==String.class){
            return "char*";
        }

        throw new IllegalArgumentException("Unsupported type: "+type.getCanonicalName());
    }


    public static byte typeLength(Object arg){
        if(arg instanceof Void)
            return 0;
        if(arg instanceof Integer)
            return 4;
        if(arg instanceof Short)
            return 2;
        if(arg instanceof Byte)
            return 1;
        if(arg instanceof String){
            String s= (String) arg;
            byte[] bytes = s.getBytes(StandardCharsets.US_ASCII);
            if(bytes.length>254)
                throw new IllegalArgumentException("String too long: \""+bytes.toString()+"\"");
            return (byte) (bytes.length+1); //1 byte for length
        }
        throw new IllegalArgumentException("Unsupported type: "+arg.getClass().getTypeName());
    }


}
