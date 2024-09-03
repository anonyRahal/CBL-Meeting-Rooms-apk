package com.example.cbl_teams_rooms.models;

import java.util.List;

public class TodayMeeting {
    private String id;
    private String subject;
    private String startDateTime;
    private String endDateTime;
    private String location;
    private List<Attendee> attendees;

    public TodayMeeting(String id, String subject, String startDateTime, String endDateTime, String location, List<Attendee> attendees) {
        this.id = id;
        this.subject = subject;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.attendees = attendees;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Attendee> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<Attendee> attendees) {
        this.attendees = attendees;
    }


}