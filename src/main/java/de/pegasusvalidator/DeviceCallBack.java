package de.pegasusvalidator;

import com.sun.jna.Pointer;

public abstract class DeviceCallBack implements JnaMobileDevice.idevice_event_cb_t {

    private final String ADDED = "add";
    private final String REMOVED = "remove";


    @Override
    public void apply(JnaMobileDevice.idevice_event_t event_t, Pointer user_data) {

        try {
            if (event_t.event_type == JnaMobileDevice.idevice_event_type.IDEVICE_DEVICE_ADD) {
                onDeviceAdded(event_t.udid);
            } else if (event_t.event_type == JnaMobileDevice.idevice_event_type.IDEVICE_DEVICE_REMOVE) {
                onDeviceRemoved(event_t.udid);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    protected abstract void onDeviceAdded(String udid);

    protected abstract void onDeviceRemoved(String uuid);

}
