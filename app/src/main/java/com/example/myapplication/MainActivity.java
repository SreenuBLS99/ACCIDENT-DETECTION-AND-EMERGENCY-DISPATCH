package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;
    private static final int LOCATION_PERMISSION_CODE = 102;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference contactsRef;
    private List<String> contactNumbers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Reference to Firebase contacts
        contactsRef = FirebaseDatabase.getInstance().getReference("contacts");

        Button menuButton = findViewById(R.id.menuButton);
        ImageView appLogo = findViewById(R.id.appLogo);

        // Set click listener for the menu button
        menuButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, menuactivity.class);
            startActivity(intent);
        });

        // Set click listener for the app logo
        appLogo.setOnClickListener(view -> {
            Log.d("MainActivity", "App logo clicked.");
            if (checkPermissions()) {
                fetchContactsAndSendSMS();
            } else {
                requestPermissions();
            }
        });
    }

    // Check if SMS and location permissions are granted
    private boolean checkPermissions() {
        boolean smsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!smsPermission) {
            Log.d("MainActivity", "SMS permission not granted.");
        }
        if (!locationPermission) {
            Log.d("MainActivity", "Location permission not granted.");
        }

        return smsPermission && locationPermission;
    }

    // Request SMS and location permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION}, SMS_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Permissions granted.");
                fetchContactsAndSendSMS();
            } else {
                Toast.makeText(this, "Permissions are required to send SMS and access location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Fetch contacts from Firebase and send SMS
    private void fetchContactsAndSendSMS() {
        // Retrieve contact numbers from Firebase
        contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactNumbers.clear();
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    String phoneNumber = contactSnapshot.child("number").getValue(String.class);
                    if (phoneNumber != null) {
                        contactNumbers.add(phoneNumber);
                        Log.d("MainActivity", "Retrieved contact: " + phoneNumber);
                    }
                }
                if (contactNumbers.isEmpty()) {
                    Log.e("MainActivity", "No contacts found in Firebase.");
                    Toast.makeText(MainActivity.this, "No contacts found to send SMS.", Toast.LENGTH_SHORT).show();
                } else {
                    sendEmergencySMS();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to retrieve contacts", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Error fetching contacts: " + error.getMessage());
            }
        });
    }

    // Send emergency SMS with location
    private void sendEmergencySMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                String locationLink = "https://www.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
                                String message = "Help me, it's an emergency! Location: " + locationLink;

                                for (String phoneNumber : contactNumbers) {
                                    try {
                                        SmsManager smsManager = SmsManager.getDefault();
                                        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                                        Log.d("MainActivity", "SMS sent to " + phoneNumber + " with message: " + message);
                                    } catch (Exception e) {
                                        Log.e("MainActivity", "Error sending SMS to " + phoneNumber + ": " + e.getMessage());
                                    }
                                }

                                Toast.makeText(MainActivity.this, "Emergency SMS sent to contacts", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                                Log.e("MainActivity", "Location is null");
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Location permission is required to send SMS", Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Location permission not granted.");
        }
    }
}
