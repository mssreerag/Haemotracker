package com.example.haemotracker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class homeActivity extends AppCompatActivity {


    private ArrayList<String> mnames = new ArrayList<>();

    {
        mnames.add("Geeks");
        mnames.add("for");
        mnames.add("Geeks");
        mnames.add("Geeks");
        mnames.add("Geks");
        mnames.add("Geeks");
        mnames.add("Geeks");
        mnames.add("eeks");
        mnames.add("Geeks");
        mnames.add("Gees");
        mnames.add("Geeks");
        mnames.add("Geek");
        mnames.add("Geasdeks");

    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String uid, uName;
    private FusedLocationProviderClient client;
    double lat=0,lon=0;
    String chosenOne;
    String chosenId;
    double chosenMin=-1;
    double dist=0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final TextView userName = (TextView) findViewById(R.id.textView2);
        final TextView chosenUser=(TextView) findViewById(R.id.user1);
        Button choose=findViewById(R.id.choose);

        Button logout = findViewById(R.id.logout);

        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(this);


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signinCheck(false);
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(homeActivity.this, MainActivity.class));

            }
        });

        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        if (users != null) {

            //Name, email address, and profile photo Url
            uName = users.getDisplayName().toString();
            String userMail = users.getEmail();
            Uri photoUrl = users.getPhotoUrl();
//            // Check if user's email is verified
//            boolean emailVerified = users.isEmailVerified();
            uid = users.getUid();
            Log.wtf("tas", "sdMasMMM" + uid);
            userName.setText(uName + uid);
            signinCheck(true);
        }
//
        // Create a new user with a first and last name


        getLocation();









        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.wtf("tag", "DocumentSnapshot data: " + document.getData());
                    } else {

                        Log.wtf("tag", "No such document");
                        addData();

                    }
                } else {
                    Log.d("tag", "get failed with ", task.getException());
                }
            }
        });







        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                getLocation();
                updateDatabase();
                Log.wtf("tag","running update"+lat+" "+lon);
            }
        },4000,60000);


//        initRecyclerView();






        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchNearest();
                chosenUser.setText(chosenOne +" at a distance "+dist*1000);
            }
        });


    }


    //END OF ONCREATE








    private void signinCheck(boolean b) {

        DocumentReference userrRef = db.collection("users").document(uid);
        userrRef
                .update("logged_in",b)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.wtf("tag", " successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.wtf("tag", "Error updating document", e);
                    }
                });


    }

    private void searchNearest() {

            db.collection("users")
                .whereEqualTo("logged_in", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {


                                    double calclat = Double.parseDouble(document.getData().get("lat").toString());
                                    double calclon = Double.parseDouble(document.getData().get("lon").toString());

                                    dist=Math.sqrt(Math.pow(69.1*(calclat-lat),2.0)+Math.pow(69.1*(calclon-lon)*Math.cos(calclat/57.3),2.0));
                                if(dist != 0.0){
                                    if( dist < chosenMin || chosenMin==-1){
                                        chosenMin=dist;
                                        chosenId=document.getId();
                                        chosenOne=document.getData().get("name").toString();

                                        Log.wtf("tag", document.getId() + " => " + document.getData());

                                    }
                                    else{
                                        Log.wtf("skip","Skipped : "+document.getData());
                                    }
                                }
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    private void updateDatabase() {
        DocumentReference washingtonRef = db.collection("users").document(uid);
// Set the "isCapital" field of the city 'DC'
        washingtonRef
                .update("lat", lat)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.wtf("tag", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.wtf("tag", "Error updating document", e);
                    }
                });
        washingtonRef
                .update("lon", lon);
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(homeActivity.this, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
        }
        client.getLastLocation().addOnSuccessListener(homeActivity.this, new OnSuccessListener<Location>() {

            @Override
            public void onSuccess(Location location) {

                if(location!=null){
                    lat=location.getLatitude();
                    lon=location.getLongitude();
//                    Log.wtf("tag",""+lat);

                }
            }
        });
    }
//    location: new firebase.firestore.GeoPoint(latitude, longitude)

            private void addData() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", uName);
        user.put("lon",lon);
        user.put("lat",lat);
        user.put("logged_in",true);


        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.wtf("TAG", "Dataof "+ uName + "successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.wtf("TAG", "Error writing document of " +uName, e);
                    }
                });
    }

    private void initRecyclerView(){
        RecyclerView recyclerView=findViewById(R.id.recycler_view);
        recyclerViewAdapter adapter=new recyclerViewAdapter(this,mnames);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    private void requestPermission(){
        ActivityCompat.requestPermissions(this ,new String[]{ACCESS_FINE_LOCATION}, 1);
    }
}
