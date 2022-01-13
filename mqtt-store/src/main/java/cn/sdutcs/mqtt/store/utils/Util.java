package cn.sdutcs.mqtt.store.utils;

import java.io.Closeable;
import java.io.IOException;

public class Util {
    public static boolean safeClose(Closeable cb) {
        if (null != cb) {
            try {
                cb.close();
            } catch (IOException var2) {
                return false;
            }
        }
        return true;
    }
}
