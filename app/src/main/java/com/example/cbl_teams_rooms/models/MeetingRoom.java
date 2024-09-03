package com.example.cbl_teams_rooms.models;

public class MeetingRoom {
    private String subject;
    private String date;
    private String time;
    private String duration;

    public MeetingRoom(String subject, String date, String time, String duration) {
        this.subject = subject;
        this.date = date;
        this.time = time;
        this.duration = duration;
    }

    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDuration() {
        return duration;
    }
}
