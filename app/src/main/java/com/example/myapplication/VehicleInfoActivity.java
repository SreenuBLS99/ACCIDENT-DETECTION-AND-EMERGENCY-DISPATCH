package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View; // Import View
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VehicleInfoActivity extends AppCompatActivity {

    private EditText rcNumberEditText;
    private Button confirmButton;
    private FirebaseFirestore db;
    private LinearLayout mainLayout;
    private SharedPreferences sharedPreferences; // SharedPreferences for persisting data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_info);

        rcNumberEditText = findViewById(R.id.rcNumberEditText);
        confirmButton = findViewById(R.id.confirmButton);
        mainLayout = findViewById(R.id.mainLayout);

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("VehicleDetails", MODE_PRIVATE); // Initialize SharedPreferences

        confirmButton.setOnClickListener(v -> fetchVehicleDetails());

        loadSavedVehicleDetails(); // Load previously saved details
    }

    private void fetchVehicleDetails() {
        String rcNumber = rcNumberEditText.getText().toString().trim();

        if (rcNumber.isEmpty()) {
            Toast.makeText(this, "Please enter an RC number", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference docRef = db.collection("vehicles").document(rcNumber);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null && task.getResult().exists()) {
                    // Extract vehicle details
                    String engineNumber = task.getResult().getString("Engine Number");
                    String fatherName = task.getResult().getString("Father Name");
                    String fuelType = task.getResult().getString("Fuel Type");
                    String issueDate = task.getResult().getString("Issue Date");
                    String model = task.getResult().getString("Model");
                    String name = task.getResult().getString("Name");
                    String presentAddress = task.getResult().getString("Present Address");
                    String registeredAt = task.getResult().getString("Registered At");

                    // Store the details in SharedPreferences
                    saveVehicleDetails(rcNumber, engineNumber, fatherName, fuelType, issueDate, model, name, presentAddress, registeredAt);

                    // Create UI for the vehicle details
                    createVehicleDetailLayout(model, engineNumber, fatherName, fuelType, issueDate, name, presentAddress, registeredAt);
                } else {
                    Log.w("VehicleInfoActivity", "No document found for RC number: " + rcNumber);
                    Toast.makeText(this, "No details found for the given RC number", Toast.LENGTH_SHORT).show();
                }
            } else {
                Exception e = task.getException();
                Log.e("VehicleInfoActivity", "Error getting documents: ", e);
                Toast.makeText(this, "Unable to fetch the details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveVehicleDetails(String rcNumber, String engineNumber, String fatherName, String fuelType, String issueDate, String model, String name, String presentAddress, String registeredAt) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("RCNumber", rcNumber);
        editor.putString("EngineNumber", engineNumber);
        editor.putString("FatherName", fatherName);
        editor.putString("FuelType", fuelType);
        editor.putString("IssueDate", issueDate);
        editor.putString("Model", model);
        editor.putString("Name", name);
        editor.putString("PresentAddress", presentAddress);
        editor.putString("RegisteredAt", registeredAt);
        editor.apply();
    }

    private void loadSavedVehicleDetails() {
        String rcNumber = sharedPreferences.getString("RCNumber", null);
        if (rcNumber != null) {
            // Fetch other details from SharedPreferences
            String engineNumber = sharedPreferences.getString("EngineNumber", "");
            String fatherName = sharedPreferences.getString("FatherName", "");
            String fuelType = sharedPreferences.getString("FuelType", "");
            String issueDate = sharedPreferences.getString("IssueDate", "");
            String model = sharedPreferences.getString("Model", "");
            String name = sharedPreferences.getString("Name", "");
            String presentAddress = sharedPreferences.getString("PresentAddress", "");
            String registeredAt = sharedPreferences.getString("RegisteredAt", "");

            // Recreate the UI for previously fetched details
            createVehicleDetailLayout(model, engineNumber, fatherName, fuelType, issueDate, name, presentAddress, registeredAt);
        }
    }

    private void createVehicleDetailLayout(String model, String engineNumber, String fatherName, String fuelType, String issueDate, String name, String presentAddress, String registeredAt) {
        RelativeLayout modelButtonLayout = new RelativeLayout(this);
        modelButtonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        Button vehicleModelButton = new Button(this);
        vehicleModelButton.setText(model);
        RelativeLayout.LayoutParams modelButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        modelButtonParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        modelButtonLayout.addView(vehicleModelButton, modelButtonParams);

        Button deleteButton = new Button(this);
        deleteButton.setText("X");
        deleteButton.setBackgroundColor(Color.TRANSPARENT);
        deleteButton.setTextColor(Color.RED);
        RelativeLayout.LayoutParams deleteButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        deleteButtonParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        modelButtonLayout.addView(deleteButton, deleteButtonParams);

        mainLayout.addView(modelButtonLayout);

        vehicleModelButton.setOnClickListener(v -> {
            Intent intent = new Intent(VehicleInfoActivity.this, VehicleDetailsActivity.class);
            intent.putExtra("engineNumber", engineNumber);
            intent.putExtra("fatherName", fatherName);
            intent.putExtra("fuelType", fuelType);
            intent.putExtra("issueDate", issueDate);
            intent.putExtra("model", model);
            intent.putExtra("name", name);
            intent.putExtra("presentAddress", presentAddress);
            intent.putExtra("registeredAt", registeredAt);
            startActivity(intent);
        });

        // Update method call
        deleteButton.setOnClickListener(v -> showDeleteConfirmation(modelButtonLayout));
    }

    private void showDeleteConfirmation(View modelButtonLayout) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this vehicle detail?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mainLayout.removeView(modelButtonLayout);
                    clearVehicleDetails(); // Clear details from SharedPreferences
                    Toast.makeText(this, "Vehicle detail deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void clearVehicleDetails() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
