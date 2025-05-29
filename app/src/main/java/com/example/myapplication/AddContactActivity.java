package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class AddContactActivity extends AppCompatActivity {

    private EditText contactNameEditText;
    private EditText contactNumberEditText;
    private Button saveContactButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        contactNameEditText = findViewById(R.id.contactNameEditText);
        contactNumberEditText = findViewById(R.id.contactNumberEditText);
        saveContactButton = findViewById(R.id.saveContactButton);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("contacts");

        saveContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactName = contactNameEditText.getText().toString().trim();
                String contactNumber = contactNumberEditText.getText().toString().trim();

                if (!contactName.isEmpty() && !contactNumber.isEmpty()) {
                    saveContactToFirebase(contactName, contactNumber);
                } else {
                    Toast.makeText(AddContactActivity.this, "Please enter both name and number.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveContactToFirebase(String name, String number) {
        // Use the contact name as the unique key (ID)
        String contactId = name; // Directly use the contact name

        // Create a new contact object
        Contact contact = new Contact(name, number);

        // Save contact under the generated ID
        databaseReference.child(contactId).setValue(contact)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddContactActivity.this, "Contact added!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after saving
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddContactActivity.this, "Failed to add contact. Try again.", Toast.LENGTH_SHORT).show();
                });
    }
}
