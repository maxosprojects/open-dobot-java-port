package org.omilab.omirob.tinyrpc;

import org.omilab.omirob.codegen.TinyRPCMethod;
import org.omilab.omirob.codegen.Tools;
import purejavacomm.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Created by Martin on 27.07.2016.
 */
public class TinyRpcHandler implements InvocationHandler {

    private final InputStream portIS;
    private final OutputStream portOS;
    private SerialPort serialPort;

    public TinyRpcHandler(CommPortIdentifier portid) throws PortInUseException, IOException, UnsupportedCommOperationException {
        serialPort = (SerialPort) portid.open("asdf", 1500);

        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPort.disableReceiveFraming();
        serialPort.disableReceiveThreshold();
        serialPort.disableReceiveTimeout();

        portIS = serialPort.getInputStream();
        portOS = serialPort.getOutputStream();
    }

    public static Object createHandler(Class clazz, CommPortIdentifier portid) throws PortInUseException, IOException, UnsupportedCommOperationException {
        Object o = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{clazz}, new TinyRpcHandler(portid));
        return o;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(Object.class  == method.getDeclaringClass()) {
            String name = method.getName();
            if("equals".equals(name)) {
                return proxy == args[0];
            } else if("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        TinyRPCMethod annotation = method.getDeclaredAnnotation(TinyRPCMethod.class);
        sendMethodCall(method, args);

        if(method.getReturnType()==Integer.TYPE) {
            int ret=portIS.read();
            ret|=(portIS.read()<<8);
            ret|=(portIS.read()<<16);
            ret|=(portIS.read()<<24);
            return ret;
        }
        else if(method.getReturnType()==Short.TYPE) {
            int ret=portIS.read();
            ret|=(portIS.read()<<8);
            return ret;
        }
        return null;
    }

    private void sendMethodCall(Method m, Object[] args) throws IOException {
        ByteBuffer b= ByteBuffer.allocate(256);
        TinyRPCMethod annotation = m.getDeclaredAnnotation(TinyRPCMethod.class);
        b.put(annotation.id());
        int length=0;
        if(args!=null)
            for(int i=0;i<args.length;i++){
                Object arg = args[i];
                if(arg instanceof Byte)
                    b.put((Byte) arg);
                else if(arg instanceof Short)
                    b.putShort((Short) arg);
                else if(arg instanceof Integer)
                    b.putInt((Integer) arg);
                else if(arg instanceof String) {
                    String s= (String) arg;
                    byte[] bytes = s.getBytes(StandardCharsets.US_ASCII);
                    b.put((byte) bytes.length);
                    b.put(bytes);
                }
                byte argLength=Tools.typeLength(arg);
                b.put(argLength);
            }
        b.put(0, (byte) b.position());
        byte[] res = new byte[b.position()];
        b.rewind();
        b.get(res);
        portOS.write(res);
    }
}
