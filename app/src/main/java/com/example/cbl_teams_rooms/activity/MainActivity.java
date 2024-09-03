package com.example.cbl_teams_rooms.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.LottieAnimationView;
import com.example.cbl_teams_rooms.R;
import com.example.cbl_teams_rooms.api.GraphApiUtils;
import com.example.cbl_teams_rooms.models.Room;
import com.example.cbl_teams_rooms.ui.MeetingRoomDialogFragment;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import android.view.WindowInsetsController;


public class MainActivity extends AppCompatActivity implements MeetingRoomDialogFragment.OnRoomSelectedListener {
    private static final String TAG = "kav";
    private TextView dateText;
    private TextView timeText;
    private TextView meetingRoomText;
    private final Handler handler = new Handler();
    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSX", Locale.ENGLISH);
    private LinearLayout meetingsContainer,ongoingMeetingContainer;
    private List<Room> meetingRooms = new ArrayList<>();
    private boolean isFullScreen = true;
    private ImageButton toggleFullScreenButton,adminLogoutButton,meetingRoomSelectButton,editSettingsButton;
    private LottieAnimationView meetingsLoading ;
    private TextView noMeetingTodayMsg;
    private TextView currentMeetingMsg;

    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            updateTime();
            handler.postDelayed(this, 60000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String startDateTime = "2024-09-02T10:30:00.0000000";
        String startTimeZone = "UTC";
        String endDateTime = "2024-09-02T11:30:00.0000000";
        String endTimeZone = "UTC";

        // Convert and format the times
        String formattedTime = formatTime(startDateTime, startTimeZone, endDateTime, endTimeZone);
        Log.d(TAG, "test date: " + formattedTime);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        meetingRoomText = findViewById(R.id.meeting_room_text);
        adminLogoutButton = findViewById(R.id.admin_logout);
        meetingRoomSelectButton = findViewById(R.id.meeting_room_select);
        meetingsContainer = findViewById(R.id.meetings_container);
        ongoingMeetingContainer = findViewById(R.id.ongoing_meeting_view);
        editSettingsButton = findViewById(R.id.admin_login);
        dateText = findViewById(R.id.date_text);
        timeText = findViewById(R.id.time_text);
        toggleFullScreenButton = findViewById(R.id.toggle_fullscreen);
        meetingsLoading = findViewById(R.id.loading_view);
        noMeetingTodayMsg = findViewById(R.id.no_meeting_today);
        currentMeetingMsg = findViewById(R.id.current_meeting_msg);


        meetingRoomText.setText("CBL - IT Meeting Room");
        meetingsLoading.playAnimation();


        // Initially hide the buttons
        adminLogoutButton.setVisibility(View.GONE);
        meetingRoomSelectButton.setVisibility(View.GONE);

        updateButtonVisibility();
        fetchTodayMeetings("meetingroom.cble@cbllk.com");


        updateDate();
        updateTime();

        handler.post(updateTimeRunnable);


        editSettingsButton.setOnClickListener(v-> openLoginPage());
        adminLogoutButton.setOnClickListener(v -> logout());
        meetingRoomSelectButton.setOnClickListener(v-> showMeetingRoomDialog());
        toggleFullScreenButton.setOnClickListener(v -> toggleFullscreen());


    }

    private void toggleFullscreen() {
        if (isFullScreen) {
            // Exit fullscreen mode
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            isFullScreen = false;
            toggleFullScreenButton.setImageResource(R.drawable.fullscreen_24px); // Change icon to fullscreen icon
        } else {
            // Enter fullscreen mode
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            isFullScreen = true;
            toggleFullScreenButton.setImageResource(R.drawable.fullscreen_exit_24px); // Change icon to exit fullscreen icon
        }
    }


    private void updateButtonVisibility() {
        boolean isLoggedIn = checkLoginStatus();

        if (isLoggedIn) {
            adminLogoutButton.setVisibility(View.VISIBLE);
            meetingRoomSelectButton.setVisibility(View.VISIBLE);
        } else {
            adminLogoutButton.setVisibility(View.GONE);
            meetingRoomSelectButton.setVisibility(View.GONE);
        }
    }

    private boolean checkLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isLoggedIn", false);
    }

    private void logout() {
        // Clear login status
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        adminLogoutButton.setVisibility(View.GONE);
        meetingRoomSelectButton.setVisibility(View.GONE);

    }

    @Override
    public void onRoomSelected(String roomEmail,String displayName) {
        noMeetingTodayMsg.setVisibility(View.GONE);
        currentMeetingMsg.setVisibility(View.GONE);
        fetchTodayMeetings(roomEmail);
        meetingRoomText.setText(displayName);

    }

    private void fetchTodayMeetings(String roomEmail){

        meetingsLoading.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                String accessToken = GraphApiUtils.getAccessToken();
                Log.d(TAG, "access token "+accessToken);
                JsonObject meetingRoomsResponse = GraphApiUtils.getMeetingRooms(accessToken);
                Log.d(TAG, "meeting rooms"+meetingRoomsResponse);

                // Extract room names from response
                if (meetingRoomsResponse != null) {
                    JsonArray roomsArray = meetingRoomsResponse.getAsJsonArray("value");
                    meetingRooms.clear();
                    for (int i = 0; i < roomsArray.size(); i++) {
                        JsonObject roomObject = roomsArray.get(i).getAsJsonObject();
                        String displayName = roomObject.get("displayName").getAsString();
                        String emailAddress = roomObject.get("emailAddress").getAsString();
                        meetingRooms.add(new Room(displayName, emailAddress));
                    }
                }

                String dummyJsonResponse = "{ \"value\": ["
                        + "{ \"id\": \"1\", \"subject\": \"Project Kickoff\", \"start\": { \"dateTime\": \"2024-08-20T16:30:00.0000000\", \"timeZone\": \"UTC\" }, \"end\": { \"dateTime\": \"2024-08-30T04:30:00.0000000\", \"timeZone\": \"UTC\" }, \"location\": { \"displayName\": \"Conference Room A\" } },"
                        + "{ \"id\": \"2\", \"subject\": \"Team Standup\", \"start\": { \"dateTime\": \"2024-08-30T10:30:00.0000000\", \"timeZone\": \"UTC\" }, \"end\": { \"dateTime\": \"2024-08-30T11:00:00.0000000\", \"timeZone\": \"UTC\" }, \"location\": { \"displayName\": \"Meeting Room 1\" } },"
                        + "{ \"id\": \"3\", \"subject\": \"Sprint Planning\", \"start\": { \"dateTime\": \"2024-08-30T11:15:00.0000000\", \"timeZone\": \"UTC\" }, \"end\": { \"dateTime\": \"2024-08-30T12:30:00.0000000\", \"timeZone\": \"UTC\" }, \"location\": { \"displayName\": \"Conference Room B\" } },"
                        + "{ \"id\": \"4\", \"subject\": \"Design Review\", \"start\": { \"dateTime\": \"2024-08-30T13:00:00.0000000\", \"timeZone\": \"UTC\" }, \"end\": { \"dateTime\": \"2024-08-30T14:00:00.0000000\", \"timeZone\": \"UTC\" }, \"location\": { \"displayName\": \"Meeting Room 2\" } },"
                        + "{ \"id\": \"5\", \"subject\": \"Client Feedback\", \"start\": { \"dateTime\": \"2024-08-30T15:00:00.0000000\", \"timeZone\": \"UTC\" }, \"end\": { \"dateTime\": \"2024-08-30T16:00:00.0000000\", \"timeZone\": \"UTC\" }, \"location\": { \"displayName\": \"Conference Room C\" } },"
                        + "{ \"id\": \"6\", \"subject\": \"Weekly Retrospective\", \"start\": { \"dateTime\": \"2024-08-30T16:15:00.0000000\", \"timeZone\": \"UTC\" }, \"end\": { \"dateTime\": \"2024-08-30T17:00:00.0000000\", \"timeZone\": \"UTC\" }, \"location\": { \"displayName\": \"Meeting Room 3\" } },"
                        + "{ \"id\": \"7\", \"subject\": \"Sales Strategy\", \"start\": { \"dateTime\": \"2024-08-30T09:30:00.0000000\", \"timeZone\": \"UTC\" }, \"end\": { \"dateTime\": \"2024-08-30T10:30:00.0000000\", \"timeZone\": \"UTC\" }, \"location\": { \"displayName\": \"Conference Room D\" } },"
                        + "{ \"id\": \"8\", \"subject\": \"Budget Planning\", \"start\": { \"dateTime\": \"2024-08-30T11:00:00.0000000\", \"timeZone\": \"UTC\" }, \"end\": { \"dateTime\": \"2024-08-30T12:00:00.0000000\", \"timeZone\": \"UTC\" }, \"location\": { \"displayName\": \"Meeting Room 4\" } },"
                        + "{ \"id\": \"9\", \"subject\": \"Team Building\", \"start\": { \"dateTime\": \"2024-08-30T13:30:00.0000000\", \"timeZone\": \"UTC\" }, \"end\": { \"dateTime\": \"2024-08-30T15:00:00.0000000\", \"timeZone\": \"UTC\" }, \"location\": { \"displayName\": \"Outdoor Area\" } },"
                        + "{ \"id\": \"10\", \"subject\": \"Marketing Review\", \"start\": { \"dateTime\": \"2024-08-30T15:30:00.0000000\", \"timeZone\": \"UTC\" }, \"end\": { \"dateTime\": \"2024-08-30T16:30:00.0000000\", \"timeZone\": \"UTC\" }, \"location\": { \"displayName\": \"Conference Room E\" } },"
                        + "{ \"id\": \"11\", \"subject\": \"New Meeting\", \"start\": { \"dateTime\": \"2024-08-30T04:00:00.0000000\", \"timeZone\": \"UTC\" }, \"end\": { \"dateTime\": \"2024-08-30T16:30:00.0000000\", \"timeZone\": \"UTC\" }, \"location\": { \"displayName\": \"Meeting Room F\" } }"
                        + "] }";


                Gson gson = new Gson();

//                JsonObject todayMeetings = gson.fromJson(dummyJsonResponse, JsonObject.class);

                // Example to get today's meetings for a specific room
                JsonObject todayMeetings = GraphApiUtils.getTodayMeetings(accessToken, roomEmail);

                if (todayMeetings != null) {
                    Log.d(TAG, "today meetings: " + todayMeetings);


                    // Parse JSON
                    JsonArray meetingsArray = todayMeetings.getAsJsonArray("value");

                    Log.d(TAG, "meeting array : " + meetingsArray);



                    // Inflate layout and add meetings
                    LayoutInflater inflater = LayoutInflater.from(this);

                    runOnUiThread(() -> {
                        meetingsLoading.setVisibility(View.GONE);
                        meetingsContainer.removeAllViews(); // Clear previous views if needed
                        ongoingMeetingContainer.removeAllViews(); // Clear previous views if needed

                        if (meetingsArray.isEmpty()) {
                            noMeetingTodayMsg.setVisibility(View.VISIBLE);
                            currentMeetingMsg.setVisibility(View.GONE);


                        } else {
                            noMeetingTodayMsg.setVisibility(View.GONE);
                        }

                        JsonObject ongoingMeetingObject = findOngoingMeeting(meetingsArray);


                        for (JsonElement meetingElement : meetingsArray) {

                            View meetingItemView = inflater.inflate(R.layout.notification_bar, meetingsContainer, false);

                            JsonObject meetingObject = meetingElement.getAsJsonObject();

                            // Extract fields
                            String id = meetingObject.get("id").getAsString();
                            String subject = meetingObject.get("subject").getAsString();
                            String startDateTime = meetingObject.getAsJsonObject("start").get("dateTime").getAsString();
                            String startTimeZone = meetingObject.getAsJsonObject("start").get("timeZone").getAsString();
                            String endDateTime = meetingObject.getAsJsonObject("end").get("dateTime").getAsString();
                            String endTimeZone = meetingObject.getAsJsonObject("end").get("timeZone").getAsString();
                            String location = meetingObject.getAsJsonObject("location").get("displayName").getAsString();

                            // Log for debugging
                            Log.d(TAG, "meeting obj: " + meetingObject);
                            Log.d(TAG, "Subject: " + subject);

                            TextView subjectView = meetingItemView.findViewById(R.id.meeting_subject);
                            TextView dateView = meetingItemView.findViewById(R.id.meeting_date);
                            TextView timeView = meetingItemView.findViewById(R.id.meeting_time);
                            TextView durationView = meetingItemView.findViewById(R.id.meeting_duration);

                            String formattedDate = formatDate(startDateTime);
                            String formattedTime = formatTime(startDateTime,startTimeZone,endDateTime,endTimeZone);
                            String duration = calculateDuration(startDateTime, endDateTime);

                            Log.d(TAG, "duration: " + duration);

                            // Set the data to the views
                            subjectView.setText(subject);
                            dateView.setText(formattedDate); // Format as needed
                            timeView.setText(formattedTime); // Format as needed
                            durationView.setText(calculateDuration(startDateTime, endDateTime));

                            // Add the view to the container
                            meetingsContainer.addView(meetingItemView);
                        }

                        if (!meetingsArray.isEmpty()){
                            if (ongoingMeetingObject != null) {
                                View ongoingMeetingView = inflater.inflate(R.layout.ongoing_meeting, ongoingMeetingContainer, false);
                                TextView ongoingSubjectView = ongoingMeetingView.findViewById(R.id.meeting_subject);
                                TextView ongoingDateView = ongoingMeetingView.findViewById(R.id.meeting_date);
                                TextView ongoingTimeView = ongoingMeetingView.findViewById(R.id.meeting_time);
                                TextView ongoingDurationView = ongoingMeetingView.findViewById(R.id.meeting_duration);

                                // Extract fields
                                String subject = ongoingMeetingObject.get("subject").getAsString();
                                String startDateTime = ongoingMeetingObject.getAsJsonObject("start").get("dateTime").getAsString();
                                String endDateTime = ongoingMeetingObject.getAsJsonObject("end").get("dateTime").getAsString();
                                String startTimeZone = ongoingMeetingObject.getAsJsonObject("start").get("timeZone").getAsString();
                                String endTimeZone = ongoingMeetingObject.getAsJsonObject("end").get("timeZone").getAsString();

                                // Set the data to the views
                                ongoingSubjectView.setText(subject);
                                ongoingDateView.setText(formatDate(startDateTime));
                                ongoingTimeView.setText(formatTime(startDateTime, startTimeZone, endDateTime, endTimeZone));
                                ongoingDurationView.setText(calculateDuration(startDateTime, endDateTime));

                                ongoingMeetingContainer.addView(ongoingMeetingView);
                            } else {
                                currentMeetingMsg.setVisibility(View.VISIBLE);
                            }
                        }

                    });

                } else {
                    Log.e(TAG, "Failed to retrieve meetings");
                }
            } catch (IOException e) {
                Log.d(TAG, "IOException");
                e.printStackTrace();
            }
        }).start();
    }

    private JsonObject findOngoingMeeting(JsonArray meetingsArray) {
        // Get current time
        DateTime now = DateTime.now();

        // Iterate through each meeting in the array
        for (JsonElement meetingElement : meetingsArray) {
            JsonObject meetingObject = meetingElement.getAsJsonObject();
            // Extract fields
            String startDateTime = meetingObject.getAsJsonObject("start").get("dateTime").getAsString();
            String endDateTime = meetingObject.getAsJsonObject("end").get("dateTime").getAsString();
            String startTimeZone = meetingObject.getAsJsonObject("start").get("timeZone").getAsString();
            String endTimeZone = meetingObject.getAsJsonObject("end").get("timeZone").getAsString();

            // Check if this meeting is ongoing
            if (isOngoingMeeting(startDateTime, endDateTime, startTimeZone, endTimeZone, now)) {
                return meetingObject; // Return the first ongoing meeting found
            }
        }

        return null; // Return null if no ongoing meetings are found
    }

    private boolean isOngoingMeeting(String startDateTime, String endDateTime, String startTimeZone, String endTimeZone, DateTime now) {
        // Parse start and end date-times with their respective time zones
//        DateTime start = DateTime.parse(startDateTime).withZone(DateTimeZone.forID(startTimeZone));
//        DateTime end = DateTime.parse(endDateTime).withZone(DateTimeZone.forID(endTimeZone));


        DateTimeFormatter isoFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");

        DateTime formattedStartDateTime = isoFormatter.parseDateTime(startDateTime);
        DateTime formattedEndDateTime = isoFormatter.parseDateTime(endDateTime);

        DateTime start = formattedStartDateTime.plusHours(5).plusMinutes(30);
        DateTime end = formattedEndDateTime.plusHours(5).plusMinutes(30);


        Log.d(TAG, "start: " + start);
        Log.d(TAG, "end: " + end);



        // Get today's date at midnight for comparison
        LocalDate today = now.toLocalDate();
        DateTime todayStart = today.toDateTimeAtStartOfDay(DateTimeZone.forID(startTimeZone));
        DateTime todayEnd = today.toDateTimeAtStartOfDay(DateTimeZone.forID(endTimeZone)).plusDays(1).minusMillis(1);


        Log.d(TAG, "today: " + today);
        Log.d(TAG, "today start: " + todayStart);
        Log.d(TAG, "today end: " + todayEnd);
        Log.d(TAG, "check: " + (now.isAfter(todayStart) && now.isBefore(todayEnd) && now.isAfter(start) && now.isBefore(end)));

        // Check if the current time is between the start and end times for today
        return now.isAfter(todayStart) && now.isBefore(todayEnd) && now.isAfter(start) && now.isBefore(end);
    }

    private String formatDate(String dateTime) {
        DateTime dateTimeObj = DateTime.parse(dateTime);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        return dateTimeObj.toString(formatter);
    }

    public String formatTime(String startDateTime, String startTimeZone, String endDateTime, String endTimeZone) {
        try {

            DateTimeFormatter isoFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");

            DateTime formattedStartDateTime = isoFormatter.parseDateTime(startDateTime);
            DateTime formattedEndDateTime = isoFormatter.parseDateTime(endDateTime);

            DateTime startDateTimeInSriLanka = formattedStartDateTime.plusHours(5).plusMinutes(30);
            DateTime endDateTimeInSriLanka = formattedEndDateTime.plusHours(5).plusMinutes(30);

//
//            Log.d(TAG, "startDateTimeInSriLanka: " + startDateTimeInSriLanka);
//            Log.d(TAG, "endDateTimeInSriLanka: " + endDateTimeInSriLanka);

            DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("h:mm a");

            // Format the start and end times
            String formattedStartTime = startDateTimeInSriLanka.toString(outputFormatter);
            String formattedEndTime = endDateTimeInSriLanka.toString(outputFormatter);

            // Return the formatted time range
            return formattedStartTime + " - " + formattedEndTime;

        } catch (IllegalArgumentException e) {
            // Handle parsing exceptions
            return "Invalid date/time format or time zone";
        }
    }


    private String calculateDuration(String startDateTime, String endDateTime) {
        // Parse the date-time strings
        DateTime start = DateTime.parse(startDateTime);
        DateTime end = DateTime.parse(endDateTime);

        // Calculate duration
        Duration duration = new Duration(start, end);
        long hours = duration.toStandardHours().getHours();
        long minutes = duration.toStandardMinutes().getMinutes() % 60;

        return hours + "hr " + minutes + "min";
    }

    private void updateDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        String date = dateFormat.format(new Date());
        dateText.setText(date);
    }

    private void updateTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String time = timeFormat.format(new Date());
        timeText.setText(time);
    }

    private void openLoginPage() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void showMeetingRoomDialog() {
//        noMeetingTodayMsg.setVisibility(View.GONE);
//        currentMeetingMsg.setVisibility(View.GONE);

        FragmentManager fragmentManager = getSupportFragmentManager();
        MeetingRoomDialogFragment dialog = MeetingRoomDialogFragment.newInstance(meetingRooms);
        dialog.show(fragmentManager, "meetingRoomDialog");
    }

}
