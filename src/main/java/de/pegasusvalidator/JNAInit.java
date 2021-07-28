package de.pegasusvalidator;

import com.sun.jna.NativeLibrary;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JNAInit {

    private static boolean initialize = false;
    private static File output;

    public static synchronized boolean init() {
        if (initialize) {
            return true;
        }


        String home = System.getProperty("user.home");
        System.out.println("LOADING DLLS FROM HOME: " + home);
        File jna = new File(home, "/.ios-driver/jna/darwin");
        jna.mkdirs();
        output = jna;


        List<String> libs = new ArrayList<String>();


        libs.add("vcruntime140d");
        libs.add("libcrypto-1_1-x64");
        libs.add("libssl-1_1-x64");
        libs.add("lzma");
        libs.add("pcre");
        libs.add("pcred");
        libs.add("libusb0");


        libs.add("libcharset");

        libs.add("usbmuxd");
        libs.add("libxml2");
        libs.add("libiconv");
        libs.add("libcharset");
        libs.add("bz2");
        libs.add("irecovery");
        libs.add("ideviceactivation");
        libs.add("libde265");
        libs.add("heif");

        libs.add("getopt");
        libs.add("plist");

        libs.add("zip");
        libs.add("zlib1");
        libs.add("zlibd1");

        libs.add("readline");
        libs.add("pthreadVC3d");
        libs.add("pthreadVC3");

        libs.add("imobiledevice");



        for (String lib : libs) {
            unpack(lib, jna);
        }
        unpackExe("ideviceinstaller", jna);
        unpackExe("ideviceinfo", jna);


        NativeLibrary.addSearchPath("imobiledevice", jna.getAbsolutePath());



        initialize = true;
        return true;
    }

    public static File getTemporaryJNAFolder() {
        return output;
    }


    private static void unpack(String lib, File out) {
        String libName = "unknown platform";
        String resource = "unknown platform";
        if (Config.isWin) {
            libName = lib + ".dll";
            resource = "win32-x86-64/" + libName;

        } else {
            libName = "lib" + lib + ".so";
            resource = "linux-x86-64/" + libName;

        }

        File dst = new File(out, libName);
        copy(resource, dst);


    }
    private static void unpackExe(String lib, File out) {
        String libName = "unknown platform";
        String resource = "unknown platform";
        if (Config.isWin) {
            libName = lib + ".exe";
            resource = "win32-x86-64/" + libName;

        } else {
            libName = "lib" + lib + ".exe";
            resource = "linux-x86-64/" + libName;

        }

        File dst = new File(out, libName);
        copy(resource, dst);


    }

    private static void copy(String resource, File dst) {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (in == null) {
            System.err.println("Cannot load " + resource);
            return;
        }

        if (dst != null) {
            try {
                FileOutputStream w = new FileOutputStream(dst);
                IOUtils.copy(in, w);
                w.flush();
                w.close();
//            IOUtils.closeQuietly(w);
                //          IOUtils.closeQuietly(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
