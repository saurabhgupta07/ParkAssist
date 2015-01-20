package com.androidproject.parkassist;

/**
 * Created by sharmishtha on 11/20/2014.
 */
// Data contract for Parking Destination
public class ParkingDestination {
    private String destinationId;
    private String destinationName;
    private Double latitude;
    private Double longitude;
    private String timeZone;
    private Integer spaceCapacityTotal;
    private Double rateHighest;
    private Double rateLowest;
    private String currencySymbol;
    private String rateDescription;

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
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

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Integer getSpaceCapacityTotal() {
        return spaceCapacityTotal;
    }

    public void setSpaceCapacityTotal(Integer spaceCapacityTotal) {
        this.spaceCapacityTotal = spaceCapacityTotal;
    }

    public Double getRateHighest() {
        return rateHighest;
    }

    public void setRateHighest(Double rateHighest) {
        this.rateHighest = rateHighest;
    }

    public Double getRateLowest() {
        return rateLowest;
    }

    public void setRateLowest(Double rateLowest) {
        this.rateLowest = rateLowest;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getRateDescription() {
        return rateDescription;
    }

    public void setRateDescription(String rateDescription) {
        this.rateDescription = rateDescription;
    }
}
