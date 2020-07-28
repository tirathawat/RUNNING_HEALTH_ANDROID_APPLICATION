package com.example.runninghealth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class SensorFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);

        final ImageView foreleft_left = view.findViewById(R.id.foreleft_leftIv);
        final ImageView foremid_left = view.findViewById(R.id.foremid_leftIv);
        final ImageView foreright_left = view.findViewById(R.id.foreright_leftIv);
        final ImageView mid_left = view.findViewById(R.id.mid_leftIv);
        final ImageView heel_left = view.findViewById(R.id.heel_leftIv);
        final ImageView foreleft_right = view.findViewById(R.id.foreleft_rightIv);
        final ImageView foremid_right = view.findViewById(R.id.foremid_rightIv);
        final ImageView foreright_right = view.findViewById(R.id.foreright_rightIv);
        final ImageView mid_right = view.findViewById(R.id.mid_rightIv);
        final ImageView heel_right = view.findViewById(R.id.heel_rightIv);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference sensor_left = database.getReference("sensor_left");
        DatabaseReference sensor_right = database.getReference("sensor_right");

        sensor_left.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map map = (Map)dataSnapshot.getValue();
                String value = null;
                if (map != null) {
                    value = String.valueOf(map.get("sensor_left"));
                }
                int allsensor = 0;
                if (value != null) {
                    allsensor = Integer.parseInt(value);
                }
                int foreL = allsensor / 10000;
                int foreM = (allsensor % 10000) / 1000;
                int foreR = (allsensor % 1000) / 100;
                int mid = (allsensor % 100) / 10;
                int heel = allsensor % 10;

                if (foreL == 2) {
                    foreleft_left.setColorFilter(getResources().getColor(R.color.colorRed));
                }
                else {
                    foreleft_left.setColorFilter(getResources().getColor(R.color.colorGreen));
                }

                if (foreM == 2) {
                    foremid_left.setColorFilter(getResources().getColor(R.color.colorRed));
                }
                else {
                    foremid_left.setColorFilter(getResources().getColor(R.color.colorGreen));
                }

                if (foreR == 2) {
                    foreright_left.setColorFilter(getResources().getColor(R.color.colorRed));
                }
                else {
                    foreright_left.setColorFilter(getResources().getColor(R.color.colorGreen));
                }

                if (mid == 2) {
                    mid_left.setColorFilter(getResources().getColor(R.color.colorRed));
                }
                else {
                    mid_left.setColorFilter(getResources().getColor(R.color.colorGreen));
                }

                if (heel == 2) {
                    heel_left.setColorFilter(getResources().getColor(R.color.colorRed));
                }
                else {
                    heel_left.setColorFilter(getResources().getColor(R.color.colorGreen));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sensor_right.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map map = (Map)dataSnapshot.getValue();
                String value = null;
                if (map != null) {
                    value = String.valueOf(map.get("sensor_right"));
                }
                int allsensor = 0;
                if (value != null) {
                    allsensor = Integer.parseInt(value);
                }

                int foreL = allsensor / 10000;
                int foreM = (allsensor % 10000) / 1000;
                int foreR = (allsensor % 1000) / 100;
                int mid = (allsensor % 100) / 10;
                int heel = allsensor % 10;

                if (foreL == 2) {
                    foreleft_right.setColorFilter(getResources().getColor(R.color.colorRed));
                }
                else {
                    foreleft_right.setColorFilter(getResources().getColor(R.color.colorGreen));
                }

                if (foreM == 2) {
                    foremid_right.setColorFilter(getResources().getColor(R.color.colorRed));
                }
                else {
                    foremid_right.setColorFilter(getResources().getColor(R.color.colorGreen));
                }

                if (foreR == 2) {
                    foreright_right.setColorFilter(getResources().getColor(R.color.colorRed));
                }
                else {
                    foreright_right.setColorFilter(getResources().getColor(R.color.colorGreen));
                }

                if (mid == 2) {
                    mid_right.setColorFilter(getResources().getColor(R.color.colorRed));
                }
                else {
                    mid_right.setColorFilter(getResources().getColor(R.color.colorGreen));
                }

                if (heel == 2) {
                    heel_right.setColorFilter(getResources().getColor(R.color.colorRed));
                }
                else {
                    heel_right.setColorFilter(getResources().getColor(R.color.colorGreen));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }
}
