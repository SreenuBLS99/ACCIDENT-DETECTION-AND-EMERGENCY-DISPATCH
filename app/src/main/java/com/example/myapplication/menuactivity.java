package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class menuactivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        ImageButton informationButton = findViewById(R.id.informationButton);
        ImageButton addContactButton = findViewById(R.id.addContactButton);
        ImageButton viewContactsButton = findViewById(R.id.viewContactsButton);
        ImageButton deleteContactsButton = findViewById(R.id.deleteContactsButton);
        ImageButton medicalDetailsButton = findViewById(R.id.medicalDetailsButton);

        informationButton.setOnClickListener(v -> {
            Intent intent = new Intent(menuactivity.this, VehicleInfoActivity.class);
            startActivity(intent);
        });

        addContactButton.setOnClickListener(v -> {
            Intent intent = new Intent(menuactivity.this, AddContactActivity.class);
            startActivity(intent);
        });

        viewContactsButton.setOnClickListener(v -> {
            Intent intent = new Intent(menuactivity.this, ViewContactsActivity.class);
            startActivity(intent);
        });

        deleteContactsButton.setOnClickListener(v -> {
            Intent intent = new Intent(menuactivity.this, DeleteContactsActivity.class);
            startActivity(intent);
        });

        medicalDetailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(menuactivity.this, MedicalDetailsActivity.class);
            startActivity(intent);
        });
    }
}
