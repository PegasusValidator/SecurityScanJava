package de.pegasusvalidator;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;

import java.util.Arrays;
import java.util.List;

public class JnaMobileDevice implements Library {
    public static final String JNA_LIBRARY_NAME = "imobiledevice";
    public static final NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(JnaMobileDevice.JNA_LIBRARY_NAME);

    static {
        Native.register(JnaMobileDevice.class, JnaMobileDevice.JNA_NATIVE_LIB);

    }

    public static interface idevice_event_type {
        public static final int IDEVICE_DEVICE_ADD = 1;
        public static final int IDEVICE_DEVICE_REMOVE = 2;
    }


    public interface idevice_event_cb_t extends Callback {
        void apply(idevice_event_t event_t, Pointer user_data);

    }

    public static class idevice_event_t extends Structure {

        /**
         * C type : char**
         */
        public int event_type;
        /**
         * /**
         * C type : char**
         */
        public String udid;
        /**
         * C type : int
         */
        public Pointer conn_type;


        public idevice_event_t() {
            super();
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("event_type", "udid", "conn_type");
        }


        public idevice_event_t(int event_type, String udid, Pointer conn_type) {
            super();
            this.event_type = event_type;
            this.udid = udid;
            this.conn_type = conn_type;
        }

        public idevice_event_t(Pointer peer) {
            super(peer);
        }

        public static class ByReference extends idevice_event_t implements Structure.ByReference {
        }

        public static class ByValue extends idevice_event_t implements Structure.ByValue {
        }


    }



    public interface SCBack extends Callback {
        public void MessageHandle(double overall_progress, int backupDone);
    }

    public static synchronized native int idevice_event_subscribe(idevice_event_cb_t callback, Pointer user_data);

    public static synchronized native int idevice_new_with_options(PointerByReference idevice_t, String udid, int idevice_options);

    public static synchronized native int lockdownd_client_new_with_handshake(Pointer idevice_t, Pointer lockdownd_client_t, String label);

    public static synchronized native void mobilebackup2_start_full_backup(SCBack scBack, String backupDirectory);







}
