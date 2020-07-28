package com.example.runninghealth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.database.FirebaseDatabase.getInstance;

public class RunningFragment extends Fragment {

    //firebase user
    private FirebaseUser user;

    //database reference
    private DatabaseReference databaseReference;
    private DatabaseReference mRefSensor_left, mRefSensor_right;

    //storage
    private StorageReference storageReference;

    //date and time
    private Calendar c = Calendar.getInstance();
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String formattedDate;

    //views form xml
    private ImageView avatarIv;
    private TextView nameTv, emailTv;
    private TextView count_fore_leftTv, count_mid_leftTv, count_heel_leftTv;
    private TextView count_fore_rightTv, count_mid_rightTv, count_heel_rightTv;
    private TextView arch_type_leftTv, arch_type_rightTv;

    //progress dialog
    private ProgressDialog pd;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //arrays of permission to be requested
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //uri of picked image
    private Uri image_uri;

    //for checking profile or cover photo
    private String profileOrCoverPhoto;

    //count data
    private int count_fore_left;
    private int count_mid_left;
    private int count_heel_left;
    private int count_fore_right;
    private int count_mid_right;
    private int count_heel_right;

    private int count_over_left;
    private int count_under_left;
    private int count_over_right;
    private int count_under_right;
    private int count_neutral_left;
    private int count_neutral_right;

    private double perfore_left;
    private double permid_left;
    private double perheel_left;
    private double perfore_right;
    private double permid_right;
    private double perheel_right;
    private double perover_left;
    private double perunder_left;
    private double perover_right;
    private double perunder_right;
    private double perneutral_left;
    private double perneutral_right;

    public RunningFragment() {
        //Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //inflate the layout for this fragment
        View  view = inflater.inflate(R.layout.fragment_running, container, false);

        //init firebase
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        mRefSensor_left = firebaseDatabase.getReference("sensor_left");
        mRefSensor_right = firebaseDatabase.getReference("sensor_right");
        storageReference = FirebaseStorage.getInstance().getReference();

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init views
        avatarIv = view.findViewById(R.id.avatarIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        Button edit_profileBtn = view.findViewById(R.id.edit_profileBtn);
        count_fore_leftTv = view.findViewById(R.id.count_fore_leftTv);
        count_mid_leftTv = view.findViewById(R.id.count_mid_leftTv);
        count_heel_leftTv = view.findViewById(R.id.count_heel_leftTv);
        count_fore_rightTv = view.findViewById(R.id.count_fore_rightTv);
        count_mid_rightTv = view.findViewById(R.id.count_mid_rightTv);
        count_heel_rightTv = view.findViewById(R.id.count_heel_rightTv);
        ToggleButton start_stopBtn = view.findViewById(R.id.start_stopBtn);
        arch_type_leftTv = view.findViewById(R.id.arch_type_leftTv);
        arch_type_rightTv = view.findViewById(R.id.arch_type_rightTv);

        //init progress dialog
        pd = new ProgressDialog(getActivity());

        //handle start/stop ToggleButton clicks
        start_stopBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("SetTextI18n")
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled

                    //get current time
                    formattedDate = df.format(c.getTime());

                    //set initial count
                    count_fore_left = count_mid_left = count_heel_left
                            = count_fore_right = count_mid_right = count_heel_right
                            = count_over_left = count_under_left = count_over_right
                            = count_under_right = count_neutral_left = count_neutral_right =0;


                    //sensor left listening
                    mRefSensor_left.addValueEventListener(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //get data from database
                            String sensor_left = ""+ dataSnapshot.child("sensor_left").getValue();

                            //count and show strike pattern
                            if (Objects.equals(checkStrikePattern(sensor_left), "fore")){
                                count_fore_left++;
                                count_fore_leftTv.setText(count_fore_left +" times");
                            }
                            else if (Objects.equals(checkStrikePattern(sensor_left), "mid")) {
                                count_mid_left++;
                                count_mid_leftTv.setText(count_mid_left +" times");

                            }
                            else if (Objects.equals(checkStrikePattern(sensor_left), "heel")) {
                                count_heel_left++;
                                count_heel_leftTv.setText(count_heel_left +" times");
                            }

                            //count and show pronation
                            if (Objects.equals(checkPronation(sensor_left, "left"), "over")) {
                                arch_type_leftTv.setText("Over pronation");
                                count_over_left++;
                            }
                            else if (Objects.equals(checkPronation(sensor_left, "left"), "under")) {
                                arch_type_leftTv.setText("Under pronation");
                                count_under_left++;
                            }
                            else if (Objects.equals(checkPronation(sensor_left, "left"), "neutral")) {
                                count_neutral_left++;
                                arch_type_leftTv.setText("Neutral");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //sensor right listening
                    mRefSensor_right.addValueEventListener(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //get data from database
                            String sensor_right = ""+ dataSnapshot.child("sensor_right").getValue();

                            //count and show strike pattern
                            if (Objects.equals(checkStrikePattern(sensor_right), "fore")){
                                count_fore_right++;
                                count_fore_rightTv.setText(count_fore_right +" times");
                            }
                            else if (Objects.equals(checkStrikePattern(sensor_right), "mid")) {
                                count_mid_right++;
                                count_mid_rightTv.setText(count_mid_right +" times");
                            }
                            else if (Objects.equals(checkStrikePattern(sensor_right), "heel")) {
                                count_heel_right++;
                                count_heel_rightTv.setText(count_heel_right +" times");
                            }

                            //count and show pronation
                            if (Objects.equals(checkPronation(sensor_right, "right"), "over")) {
                                arch_type_rightTv.setText("Over pronation");
                                count_over_right++;
                            }
                            else if (Objects.equals(checkPronation(sensor_right, "right"), "under")) {
                                arch_type_rightTv.setText("Under pronation");
                                count_under_right++;
                            }
                            else if (Objects.equals(checkPronation(sensor_right, "right"), "neutral")) {
                                count_neutral_right++;
                                arch_type_rightTv.setText("Neutral");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    calculateStat();

                    //map stat to prepare to send to the database
                    Map<String, Object> running_stat = new HashMap<>();
                    running_stat.put("perfore_left", perfore_left);
                    running_stat.put("permid_left", permid_left);
                    running_stat.put("perheel_left", perheel_left);
                    running_stat.put("perfore_right", perfore_right);
                    running_stat.put("permid_right", permid_right);
                    running_stat.put("perheel_right", perheel_right);
                    running_stat.put("perover_left", perover_left);
                    running_stat.put("perunder_left", perunder_left);
                    running_stat.put("perneutral_left", perneutral_left);
                    running_stat.put("perover_right", perover_right);
                    running_stat.put("perunder_right", perunder_right);
                    running_stat.put("perneutral_right", perneutral_right);

                    // Add a new document with a generated ID
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    //save stat to database
                    db.collection(Objects.requireNonNull(user.getEmail()))
                            .document(formattedDate)
                            .set(running_stat);

                    //reset and show count
                    count_fore_left = count_mid_left = count_heel_left
                            = count_fore_right = count_mid_right = count_heel_right = 0;

                    count_fore_leftTv.setText(count_fore_left +" times");
                    count_mid_leftTv.setText(count_mid_left +" times");
                    count_heel_leftTv.setText(count_heel_left +" times");

                    count_fore_rightTv.setText(count_fore_right +" times");
                    count_mid_rightTv.setText(count_mid_right +" times");
                    count_heel_rightTv.setText(count_heel_right +" times");

                    arch_type_rightTv.setText("Neutral");
                    arch_type_leftTv.setText("Neutral");
                }
            }
        });

        //Query database to show user profile
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String image = ""+ ds.child("image").getValue();

                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);

                    try {
                        //if image is received then set
                        Picasso.get().load(image).into(avatarIv);
                    }
                    catch (Exception e) {
                        //if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //fab button click
        edit_profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        return view;
    }

    /**
     * This function is used to check the strike pattern from sensor data in the database.
     *
     * The meaning of the value read from the sensor on database
     *     22211 is fore foot strike
     *     22222 is mid foot strike
     *     11112 is heel strike
     * @param data is sensor data in the database
     * @return strike pattern
     */
    private String checkStrikePattern(String data) {
        switch (data) {
            case "22211":
                return "fore";
            case "22222":
                return "mid";
            case "11112":
                return "heel";
            default:
                return null;
        }
    }

    /**
     * This function is used to check the type of pronation from sensor data in the database.
     *
     * The meaning of the value read from the sensor on database
     *     1.neutral pronation is 22211, 22222 or 11112
     *     2.over pronation for left foot is 11212 or 11211
     *     3.over pronation for right foot is 21112 or 21111
     *     4.under pronation for left foot is 21122 or 21111
     *     5.under pronation for right foot is 11222 or 11211
     *
     * @param data is sensor data in the database
     * @param foot is which foot
     * @return type of pronation
     */
    private String checkPronation(String data, String foot) {
        if (data.equals("22211") || data.equals("22222") || data.equals("11112")) return "neutral";
        else if(data.equals("11212") || data.equals("11211") && foot.equals("left") || data.equals("21112")) return "over";
        else if (data.equals("21122") || data.equals("21111") && foot.equals("left") || data.equals("11222")) return "under";
        else return null;
    }

    private boolean checkStoragePermissions(){
        return ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions(){
        boolean result = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        //request runtime storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {

        //options to show in dialog
        String[] options = {"Edit Profile Picture", "Edit Name"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //set title
        builder.setTitle("Choose Action");

        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clicks
                if (which == 0) {
                    //edit profile clicked
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();
                }
                else if (which == 1) {
                    //edit name clicked
                    pd.setMessage("Updating Profile Name");
                    showNameUpdateDialog();
                }
            }
        });

        //create and show dialog
        builder.create().show();


    }

    private void showNameUpdateDialog() {
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
        builder.setTitle("Update name");

        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        //add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter Name");
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add button in dialog to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();

                //validate if user has entered something or not
                if (!TextUtils.isEmpty(value)) {
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("name", value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //updated, dismiss progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed, dismiss progress, get and show error message
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else {
                    Toast.makeText(getContext(), "Please enter name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //add button in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        //create and show dialog
        builder.create().show();
    }

    private void showImagePicDialog() {
        //show dialog containing options Camera and Gallery to pick the image

        //options to show in dialog
        String[] options = {"Camera", "Gallery"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //set title
        builder.setTitle("Pick Image From");

        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clicks
                if (which == 0) {
                    //camera clicked
                    if (!checkCameraPermissions()) {
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                else if (which == 1) {
                    //gallery clicked
                    if(!checkStoragePermissions()) {
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });

        //create and show dialog
        builder.create().show();

    }

    @SuppressLint("ShowToast")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*this method called when user press allow or deny from permission request dialog
        here we will handle permission cases (allowed & denied)*/

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                //picking from camera, first check if camera and storage permissions allowed or not
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //permission enabled
                        pickFromCamera();
                    }
                    else {
                        //permission denied
                        Toast.makeText(getActivity(), "Please enable camera and storage permission", Toast.LENGTH_SHORT);
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                //picking from gallery, first check if storage permissions allowed or not
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //permission enabled
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT);
                    }
                }
            }
            break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking image from camera or gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery, get uri of image
                if (data != null) {
                    image_uri = data.getData();
                }
                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image is picked from camera, get uri of image
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        //show progress
        pd.show();


        //path and name of image to be stored in firebase storage
        //path where images of user profile and cover will be stored
        String storagePath = "Users_Profile_Cover_Imgs/";
        String filePathAndName = storagePath + ""+ profileOrCoverPhoto +"_"+ user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage, now get it is uri and store in user is database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        Uri downloadUri = uriTask.getResult();

                        //check if image is uploaded or not
                        if (uriTask.isSuccessful()) {
                            //image uploaded
                            //add/update uri in user is database
                            HashMap<String, Object> results = new HashMap<>();
                            if (downloadUri != null) {
                                results.put(profileOrCoverPhoto, downloadUri.toString());
                            }

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //url in database of user is added successfully
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error adding url in database of user
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error Updated Image...", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some error occure", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were some error(s), get and show error message, dismiss progress dialog
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickFromCamera() {
            //intent of picking image from device camera
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Temp pic");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

            //put image uri
            image_uri = Objects.requireNonNull(getActivity()).getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            //intent to start camera
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
            startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
        }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void calculateStat() {
        ///calculate percentage foot strike

        //right foot
        int total_right = count_fore_right + count_mid_right + count_heel_right;
        if (total_right == 0) {
            perfore_right = 0;
            perheel_right = 0;
            permid_right = 0;
        }
        else {
            perfore_right = (double)count_fore_right / total_right * 100;
            perheel_right = (double)count_heel_right / total_right * 100;
            permid_right = (double)count_mid_right / total_right * 100;
        }

        //left foot
        int total_left = count_fore_left + count_mid_left + count_heel_left;
        if (total_left == 0) {
            perfore_left = 0;
            perheel_left = 0;
            permid_left = 0;
        }
        else {
            perfore_left = (double)count_fore_left / total_left * 100;
            perheel_left = (double)count_heel_left / total_left * 100;
            permid_left = (double)count_mid_left / total_left * 100;
        }

        ///calculate percentage pronation

        //left foot
        int total_arch_left = count_over_left + count_neutral_left + count_under_left;
        if (total_arch_left == 0) {
            perover_left = 0;
            perunder_left = 0;
            perneutral_left = 0;
        }
        else {
            perover_left = (double)count_over_left / total_arch_left * 100;
            perunder_left = (double)count_under_left / total_arch_left * 100;
            perneutral_left = (double)count_neutral_left / total_arch_left * 100;
        }

        //right foot
        int total_arch_right = count_over_right + count_neutral_right + count_under_right;
        if (total_arch_right == 0) {
            perover_right = 0;
            perunder_right = 0;
            perneutral_right = 0;
        }
        else {
            perover_right = (double)count_over_right / total_arch_right * 100;
            perunder_right = (double)count_under_right / total_arch_right * 100;
            perneutral_right = (double)count_neutral_right / total_arch_right * 100;
        }
    }
}


