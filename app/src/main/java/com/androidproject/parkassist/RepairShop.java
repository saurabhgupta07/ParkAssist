package com.androidproject.parkassist;

/**
 * Created by Saurabh on 2014-12-11.
 */
// data contract for Repair Shop
public class RepairShop {

    private String repairShopId;
    private String name;
    private Double latitude;
    private Double longitude;

    public String getRepairShopId() {
        return repairShopId;
    }

    public void setRepairShopId(String repairShopId) {
        this.repairShopId = repairShopId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
