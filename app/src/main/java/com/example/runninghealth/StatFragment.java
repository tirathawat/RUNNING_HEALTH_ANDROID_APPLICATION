package com.example.runninghealth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class StatFragment extends Fragment {

    //firebase
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = firebaseAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    Spinner date, footSp;

    TextView foreperTv, midperTv, heelperTv, overperTv, underperTv, neuperTv;
    TextView advice1, advice2;

    String leftright;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_stat, container, false);

        date = view.findViewById(R.id.dateSp);
        footSp = view.findViewById(R.id.footSp);
        foreperTv = view.findViewById(R.id.forePerTv);
        midperTv = view.findViewById(R.id.midPerTv);
        heelperTv = view.findViewById(R.id.heelPerTv);
        overperTv = view.findViewById(R.id.overPerTv);
        underperTv = view.findViewById(R.id.underPerTv);
        neuperTv = view.findViewById(R.id.neuPerTv);
        advice1 = view.findViewById(R.id.adviceTv1);
        advice2 = view.findViewById(R.id.adviceTv2);

        leftright = "Left";

        List<String> foot = new ArrayList<>();
        foot.add("Left");
        foot.add("Right");

        ArrayAdapter<String> arrayAdapterfoot = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, foot);
        arrayAdapterfoot.setDropDownViewResource(android.R.layout.simple_spinner_item);
        footSp.setAdapter(arrayAdapterfoot);

        final List<String> list = new ArrayList<>();
        list.add("select");


        db.collection(user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                list.add(document.getId());
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        date.setAdapter(arrayAdapter);

       // Toast.makeText(getActivity(), ""+ list.get(0), Toast.LENGTH_SHORT).show();

        date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String itemvalue  = parent.getItemAtPosition(position).toString();

                DocumentReference docRef = db.collection(user.getEmail()).document(itemvalue);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                final Map<String, Object> st = document.getData();
                                if (leftright.equals("Left")) {
                                    double forePer = (double) st.get("perfore_left");
                                    double midPer = (double) st.get("permid_left");
                                    double heelPer = (double) st.get("perheel_left");
                                    double overPer = (double) st.get("perover_left");
                                    double neuPer = (double) st.get("perunder_left");
                                    double underPer = (double) st.get("perneutral_left");


                                    foreperTv.setText(String.format("%.1f", forePer)+"%");
                                    midperTv.setText(String.format("%.1f", midPer)+"%");
                                    heelperTv.setText(String.format("%.1f", heelPer)+"%");
                                    overperTv.setText(String.format("%.1f", overPer)+"%");
                                    underperTv.setText(String.format("%.1f", neuPer)+"%");
                                    neuperTv.setText(String.format("%.1f", underPer)+"%");

                                    if (forePer > midPer && forePer > heelPer) {
                                        advice1.setText("Fore-Foot strike");
                                    }
                                    else if (midPer > forePer && midPer > heelPer) {
                                        advice1.setText("Mid-Foot strike");
                                    }
                                    else if (heelPer > midPer && heelPer > forePer) {
                                        advice1.setText("Heel strike");
                                    }
                                    else {
                                        advice1.setText("");
                                    }

                                    if (underPer > overPer && underPer > neuPer) {
                                        advice2.setText("Arch-type Underpronation");
                                    }
                                    else if (overPer > underPer && overPer > neuPer) {
                                        advice2.setText("Arch-type Overpronation");
                                    }
                                    else if (neuPer > underPer && neuPer > overPer) {
                                        advice2.setText("Arch-type Neutralpronation");
                                    }
                                    else {
                                        advice2.setText("");
                                    }
                                }
                                else {
                                    double forePer = (double) st.get("perfore_right");
                                    double midPer = (double) st.get("permid_right");
                                    double heelPer = (double) st.get("perheel_right");
                                    double overPer = (double) st.get("perover_right");
                                    double neuPer = (double) st.get("perunder_right");
                                    double underPer = (double) st.get("perneutral_right");

                                    foreperTv.setText(String.format("%.1f", forePer)+"%");
                                    midperTv.setText(String.format("%.1f", midPer)+"%");
                                    heelperTv.setText(String.format("%.1f", heelPer)+"%");
                                    overperTv.setText(String.format("%.1f", overPer)+"%");
                                    underperTv.setText(String.format("%.1f", neuPer)+"%");
                                    neuperTv.setText(String.format("%.1f", underPer)+"%");

                                    if (forePer > midPer && forePer > heelPer) {
                                        advice1.setText("Fore-Foot strike");
                                    }
                                    else if (midPer > forePer && midPer > heelPer) {
                                        advice1.setText("Mid-Foot strike");
                                    }
                                    else if (heelPer > midPer && heelPer > forePer) {
                                        advice1.setText("Heel strike");
                                    }
                                    else {
                                        advice1.setText("");
                                    }

                                    if (underPer > overPer && underPer > neuPer) {
                                        advice2.setText("Arch-type Underpronation");
                                    }
                                    else if (overPer > underPer && overPer > neuPer) {
                                        advice2.setText("Arch-type Overpronation");
                                    }
                                    else if (neuPer > underPer && neuPer > overPer) {
                                        advice2.setText("Arch-type Neutralpronation");
                                    }
                                    else {
                                        advice2.setText("");
                                    }
                                }

                                footSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        String itemfoot = parent.getItemAtPosition(position).toString();
                                        if (itemfoot.equals("Left")) {
                                            double forePer = (double) st.get("perfore_left");
                                            double midPer = (double) st.get("permid_left");
                                            double heelPer = (double) st.get("perheel_left");
                                            double overPer = (double) st.get("perover_left");
                                            double neuPer = (double) st.get("perunder_left");
                                            double underPer = (double) st.get("perneutral_left");

                                            foreperTv.setText(String.format("%.1f", forePer)+"%");
                                            midperTv.setText(String.format("%.1f", midPer)+"%");
                                            heelperTv.setText(String.format("%.1f", heelPer)+"%");
                                            overperTv.setText(String.format("%.1f", overPer)+"%");
                                            underperTv.setText(String.format("%.1f", neuPer)+"%");
                                            neuperTv.setText(String.format("%.1f", underPer)+"%");

                                            if (forePer > midPer && forePer > heelPer) {
                                                advice1.setText("Fore-Foot strike");
                                            }
                                            else if (midPer > forePer && midPer > heelPer) {
                                                advice1.setText("Mid-Foot strike");
                                            }
                                            else if (heelPer > midPer && heelPer > forePer) {
                                                advice1.setText("Heel strike");
                                            }
                                            else {
                                                advice1.setText("");
                                            }

                                            if (underPer > overPer && underPer > neuPer) {
                                                advice2.setText("Arch-type Underpronation");
                                            }
                                            else if (overPer > underPer && overPer > neuPer) {
                                                advice2.setText("Arch-type Overpronation");
                                            }
                                            else if (neuPer > underPer && neuPer > overPer) {
                                                advice2.setText("Arch-type Neutralpronation");
                                            }
                                            else {
                                                advice2.setText("");
                                            }

                                            leftright = "Left";
                                        }
                                        else {
                                            double forePer = (double) st.get("perfore_right");
                                            double midPer = (double) st.get("permid_right");
                                            double heelPer = (double) st.get("perheel_right");
                                            double overPer = (double) st.get("perover_right");
                                            double neuPer = (double) st.get("perunder_right");
                                            double underPer = (double) st.get("perneutral_right");

                                            foreperTv.setText(String.format("%.1f", forePer)+"%");
                                            midperTv.setText(String.format("%.1f", midPer)+"%");
                                            heelperTv.setText(String.format("%.1f", heelPer)+"%");
                                            overperTv.setText(String.format("%.1f", overPer)+"%");
                                            underperTv.setText(String.format("%.1f", neuPer)+"%");
                                            neuperTv.setText(String.format("%.1f", underPer)+"%");

                                            if (forePer > midPer && forePer > heelPer) {
                                                advice1.setText("Fore-Foot strike");
                                            }
                                            else if (midPer > forePer && midPer > heelPer) {
                                                advice1.setText("Mid-Foot strike");
                                            }
                                            else if (heelPer > midPer && heelPer > forePer) {
                                                advice1.setText("Heel strike");
                                            }
                                            else {
                                                advice1.setText("");
                                            }

                                            if (underPer > overPer && underPer > neuPer) {
                                                advice2.setText("Arch-type Underpronation");
                                            }
                                            else if (overPer > underPer && overPer > neuPer) {
                                                advice2.setText("Arch-type Overpronation");
                                            }
                                            else if (neuPer > underPer && neuPer > overPer) {
                                                advice2.setText("Arch-type Neutralpronation");
                                            }
                                            else {
                                                advice2.setText("");
                                            }

                                            leftright = "Right";
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });

                              //  Toast.makeText(getActivity(), ""+st.get("permid_left"), Toast.LENGTH_SHORT).show();
                              //  Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        footSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemfoot = parent.getItemAtPosition(position).toString();
                if (itemfoot.equals("Left")) {
                    leftright = "Left";
                }
                else {
                    leftright = "Right";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }
}
