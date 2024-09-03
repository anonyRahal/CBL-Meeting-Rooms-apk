package com.example.cbl_teams_rooms.api;

import android.util.Log;

import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Objects;

public class GraphApiUtils {

    private static final String TAG = "api";


    private static final String TENANT_ID = "d1f10aba-7a7e-4c41-92ae-10c2c053f6f1";
    private static final String CLIENT_ID = "16c5ef43-7310-4f46-abfc-89858a171b10";
    private static final String CLIENT_SECRET = "nLF8Q~dn8PMh5-Xl.fMbaqC2HLm4JvkNftF8UbiF";
    private static final String TOKEN_ENDPOINT = "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
    private static final String GRAPH_API_BASE_URL = "https://graph.microsoft.com/v1.0";

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static String getAccessToken() throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(String.format(TOKEN_ENDPOINT, TENANT_ID))).newBuilder().build();

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("scope", "https://graph.microsoft.com/.default")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            JsonObject jsonResponse = gson.fromJson(response.body().string(), JsonObject.class);
            return jsonResponse.get("access_token").getAsString();
        }
    }

    public static JsonObject getMeetingRooms(String accessToken) throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(GRAPH_API_BASE_URL + "/places/microsoft.graph.room")).newBuilder().build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return gson.fromJson(response.body().string(), JsonObject.class);
        }
    }

    public static JsonObject getTodayMeetings(String accessToken, String roomEmail) throws IOException {

        // Get current date in UTC
        DateTime now = new DateTime(DateTimeZone.UTC);
        Log.e(TAG, "today "+ now);

        // Create a Duration of 5 hours and 30 minutes
        Duration duration = new Duration(5 * 3600 * 1000 + 30 * 60 * 1000);

        // Add the duration to the current time
        DateTime newTime = now.plus(duration);

        // Format current date
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        String today = newTime.toString(dateFormatter);

        Log.e(TAG, "today "+ today);


        // Create start and end DateTime for today
        DateTime startUtc = new DateTime(today + "T00:00:00Z", DateTimeZone.UTC);
        DateTime endUtc = new DateTime(today + "T23:59:59Z", DateTimeZone.UTC);

        Log.e(TAG, "start "+ startUtc);
        Log.e(TAG, "end "+ endUtc);


        // Format dates for API request
        DateTimeFormatter isoFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String startDateTime = startUtc.toString(isoFormatter);
        String endDateTime = endUtc.toString(isoFormatter);

        Log.e(TAG, "start date "+ startDateTime);
        Log.e(TAG, "end date "+ endDateTime);


        // Build the URL with query parameters
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(GRAPH_API_BASE_URL + "/users/" + roomEmail + "/calendar/calendarView"))
                .newBuilder()
                .addQueryParameter("startDateTime", startDateTime)
                .addQueryParameter("endDateTime", endDateTime)
                .build();

        // Print URL for debugging
        System.out.println("Request URL: " + url);

        // Create the request
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        Log.e(TAG, "request "+ request);


        // Execute the request and parse the response
        OkHttpClient client = new OkHttpClient(); // Ensure you have an OkHttpClient instance
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return gson.fromJson(response.body().string(), JsonObject.class);
        }
    }


}


