package com.mingbikes.eplate.event;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public class PlateFoundEvent {

    public String macAddress;
    public int type;

    public PlateFoundEvent(String _macAddress, int _type) {
        macAddress = _macAddress;
        type = _type;
    }
}
