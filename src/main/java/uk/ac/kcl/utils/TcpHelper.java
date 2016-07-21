package uk.ac.kcl.utils;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.SocketImplFactory;

/**
 * Created by rich on 15/06/16.
 */
public class TcpHelper {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TcpHelper.class);
    private static boolean firstCalled = true;
    public static void setSocketTimeout(int timeout)  {
        if(firstCalled) {
            try {
                Socket.setSocketImplFactory(new SocketImplFactory() {
                    @Override
                    public SocketImpl createSocketImpl() {
                        try {
                            // Construct an instance of PlainSocketImpl using reflection
                            Constructor constructor = Class.forName("java.net.PlainSocketImpl").getDeclaredConstructor();

                            constructor.setAccessible(true);
                            SocketImpl socketImpl = (SocketImpl) constructor.newInstance();

                            // Set the private "timeout" member using reflection
                            Field timeoutField = Class.forName("java.net.AbstractPlainSocketImpl").getDeclaredField
                                    ("timeout");
                            timeoutField.setAccessible(true);
                            timeoutField.setInt(socketImpl, timeout);

                            return socketImpl;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            LOG.info("Global socket timeout set to " + timeout + " ms");
            firstCalled = false;
        }
    }
}
