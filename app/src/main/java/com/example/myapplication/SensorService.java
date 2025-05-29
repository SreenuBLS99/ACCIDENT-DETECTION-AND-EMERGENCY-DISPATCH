package com.example.myapplication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

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

public class SensorService extends Service {
    private static final String TAG = "SensorService";
    private static final String CHANNEL_ID = "SensorServiceChannel";
    private FusedLocationProviderClient fusedLocationClient;
    private int lastIrSensorValue = 0;
    private List<Contact> contacts = new ArrayList<>();
    private String medicalDetails = "";

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
        monitorFirebaseData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, createNotification());
        }
        return START_STICKY;
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sensor Service")
                .setContentText("Monitoring IR Sensor")
                .setSmallIcon(R.drawable.notification_icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Sensor Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void monitorFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Monitor IR sensor value
        databaseReference.child("irSensor").child("value")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            int irSensorValue = snapshot.getValue(Integer.class);
                            Log.d(TAG, "IR Sensor Value: " + irSensorValue);

                            if (irSensorValue == 1 && lastIrSensorValue == 0) {
                                sendMedicalDetailsWithLocation();
                            }
                            lastIrSensorValue = irSensorValue;
                        } else {
                            Log.d(TAG, "IR sensor value does not exist.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to read IR sensor value: " + error.getMessage());
                    }
                });

        // Monitor all contacts
        databaseReference.child("contacts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        contacts.clear(); // Clear the list before updating
                        for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                            String name = contactSnapshot.child("name").getValue(String.class);
                            String number = contactSnapshot.child("number").getValue(String.class);
                            if (name != null && number != null) {
                                contacts.add(new Contact(name, number));
                            }
                        }
                        Log.d(TAG, "Contacts: " + contacts);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to read contacts: " + error.getMessage());
                    }
                });

        // Monitor medical details
        databaseReference.child("medical_details")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StringBuilder medicalDetailsBuilder = new StringBuilder();
                        for (DataSnapshot detailSnapshot : snapshot.getChildren()) {
                            String age = detailSnapshot.child("age").getValue(String.class);
                            String bloodGroup = detailSnapshot.child("bloodGroup").getValue(String.class);
                            String gender = detailSnapshot.child("gender").getValue(String.class);
                            String weight = detailSnapshot.child("weight").getValue(String.class);
                            medicalDetailsBuilder.append(String.format("Age: %s, Blood Group: %s, Gender: %s, Weight: %s\n",
                                    age, bloodGroup, gender, weight));
                        }
                        medicalDetails = medicalDetailsBuilder.toString();
                        Log.d(TAG, "Medical Details: " + medicalDetails);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to read medical details: " + error.getMessage());
                    }
                });
    }

    private void sendMedicalDetailsWithLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted.");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            for (Contact contact : contacts) {
                                sendMedicalDetails(contact, location);
                            }
                        } else {
                            Log.e(TAG, "Location is null. Unable to send SMS.");
                        }
                    }
                });
    }

    private void sendMedicalDetails(Contact contact, Location location) {
        if (contact.number == null || contact.number.isEmpty()) {
            Log.e(TAG, "No contact number available for " + contact.name);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "SMS permission not granted.");
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            String locationLink = String.format("https://www.google.com/maps?q=%s,%s", location.getLatitude(), location.getLongitude());
            String message = String.format("Contact: %s\n%s\nLocation: %s", contact.name, medicalDetails, locationLink);
            smsManager.sendTextMessage(contact.number, null, message, null, null);
            Log.d(TAG, "SMS sent to " + contact.number + " with message: " + message);
            Toast.makeText(SensorService.this, "SMS sent successfully to " + contact.name, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error sending SMS: " + e.getMessage());
            Toast.makeText(SensorService.this, "Failed to send SMS to " + contact.name, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class Contact {
        String name;
        String number;

        Contact(String name, String number) {
            this.name = name;
            this.number = number;
        }

        @Override
        public String toString() {
            return "Contact{name='" + name + "', number='" + number + "'}";
        }
    }
}
