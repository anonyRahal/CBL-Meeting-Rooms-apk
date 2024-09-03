package com.example.cbl_teams_rooms.models;

public class Attendee {
    private String name;
    private String emailAddress;
    private String responseStatus;

    public Attendee(String name, String emailAddress, String responseStatus) {
        this.name = name;
        this.emailAddress = emailAddress;
        this.responseStatus = responseStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }
}