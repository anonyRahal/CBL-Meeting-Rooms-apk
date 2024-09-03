package com.example.cbl_teams_rooms.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Room implements Parcelable {
    private String displayName;
    private String emailAddress;

    public Room(String displayName, String emailAddress) {
        this.displayName = displayName;
        this.emailAddress = emailAddress;
    }

    protected Room(Parcel in) {
        displayName = in.readString();
        emailAddress = in.readString();
    }

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    public String getDisplayName() {
        return displayName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(emailAddress);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
