package com.mingbikes.eplate.entity;

/**
 * Created by cronus-tropix on 17/7/28.
 */

public class BrandMacAddressEntity {

    /**
     * {
     * id: ""
     "macAddress": "EC:DB:10:38:C9:4C",
     "brandName": "OFO"
     }
     */

    private String id;
    private String mac;
    private String brandName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
}
