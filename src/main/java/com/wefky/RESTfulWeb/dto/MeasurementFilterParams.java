package com.wefky.RESTfulWeb.dto;

public class MeasurementFilterParams {
    private String unitSearch;
    private String startDate;
    private String endDate;
    private String cityName;

    // Constructors
    public MeasurementFilterParams() {}

    public MeasurementFilterParams(String unitSearch, String startDate, String endDate, String cityName) {
        this.unitSearch = unitSearch;
        this.startDate = startDate;
        this.endDate = endDate;
        this.cityName = cityName;
    }

    // Getters and Setters
    public String getUnitSearch() {
        return unitSearch;
    }

    public void setUnitSearch(String unitSearch) {
        this.unitSearch = unitSearch;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
