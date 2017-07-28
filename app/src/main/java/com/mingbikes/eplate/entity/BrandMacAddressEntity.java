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
    private String macAddress;
    private String brandName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
}
