package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class VehicleDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_details);

        TextView detailsTextView = findViewById(R.id.detailsTextView);

        // Retrieve data from the intent
        String engineNumber = getIntent().getStringExtra("engineNumber");
        String fatherName = getIntent().getStringExtra("fatherName");
        String fuelType = getIntent().getStringExtra("fuelType");
        String issueDate = getIntent().getStringExtra("issueDate");
        String model = getIntent().getStringExtra("model");
        String name = getIntent().getStringExtra("name");
        String presentAddress = getIntent().getStringExtra("presentAddress");
        String registeredAt = getIntent().getStringExtra("registeredAt");

        // Format details
        String detailsMessage = "Name: " + name +
                "\nFather's Name: " + fatherName +
                "\nModel: " + model +
                "\nEngine Number: " + engineNumber +
                "\nFuel Type: " + fuelType +
                "\nIssue Date: " + issueDate +
                "\nPresent Address: " + presentAddress +
                "\nRegistered At: " + registeredAt;

        // Display details
        detailsTextView.setText(detailsMessage);
    }
}
