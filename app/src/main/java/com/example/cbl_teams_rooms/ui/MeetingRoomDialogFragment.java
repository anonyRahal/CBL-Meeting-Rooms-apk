package com.example.cbl_teams_rooms.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.cbl_teams_rooms.R;
import com.example.cbl_teams_rooms.models.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MeetingRoomDialogFragment extends DialogFragment {

    public interface OnRoomSelectedListener {
        void onRoomSelected(String roomEmail,String displayName);
    }

    private static final String ARG_ROOMS = "rooms";
    private List<Room> meetingRooms;
    private OnRoomSelectedListener listener;


    public static MeetingRoomDialogFragment newInstance(List<Room> rooms) {
        MeetingRoomDialogFragment fragment = new MeetingRoomDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_ROOMS, new ArrayList<>(rooms));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_meeting_room, container, false);
        ListView listView = view.findViewById(R.id.list_meeting_rooms);
        TextView title = view.findViewById(R.id.dialog_title);
        LottieAnimationView loadingAnimationView = view.findViewById(R.id.meeting_room_loading);

        if (getArguments() != null) {
            meetingRooms = getArguments().getParcelableArrayList(ARG_ROOMS);
            if (meetingRooms != null) {
                // Show the loading animation
                loadingAnimationView.setVisibility(View.VISIBLE);

                // Simulate a delay for loading
                new Thread(() -> {
                    try {
                        // Simulate a short loading delay
                        Thread.sleep(1500); // Adjust time if needed

                        // On data load complete, hide the animation and update UI
                        requireActivity().runOnUiThread(() -> {
                            loadingAnimationView.setVisibility(View.GONE);

                            ArrayAdapter<Room> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, meetingRooms);
                            listView.setAdapter(adapter);

                            listView.setOnItemClickListener((parent, view1, position, id) -> {
                                // Handle item click
                                Room selectedRoom = meetingRooms.get(position);
                                if (listener != null) {
                                    listener.onRoomSelected(selectedRoom.getEmailAddress(), selectedRoom.getDisplayName());
                                }
                                dismiss();
                            });
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }


//        if (getArguments() != null) {
//            meetingRooms = getArguments().getParcelableArrayList(ARG_ROOMS);
//            if (meetingRooms != null) {
//                ArrayAdapter<Room> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, meetingRooms);
//                listView.setAdapter(adapter);
//
//                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        // Handle item click
//                        Room selectedRoom = meetingRooms.get(position);
//                        if (listener != null) {
//                            listener.onRoomSelected(selectedRoom.getEmailAddress(),selectedRoom.getDisplayName());
//                        }
//                        dismiss();
//                    }
//                });
//            }
//        }

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRoomSelectedListener) {
            listener = (OnRoomSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnRoomSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
