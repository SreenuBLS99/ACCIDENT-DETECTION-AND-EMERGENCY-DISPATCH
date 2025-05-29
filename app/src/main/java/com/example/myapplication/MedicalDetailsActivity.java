package com.example.myapplication;
import com.google.firebase.database.DatabaseError;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MedicalDetailsActivity extends AppCompatActivity {

    private EditText bloodGroupEditText, weightEditText, ageEditText;
    private Spinner genderSpinner;
    private Button saveButton, deleteButton;
    private TextView savedDetailsTextView;

    private DatabaseReference databaseReference;
    private String recordId; // To store the record ID for deletion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_details);

        // Initialize UI elements
        bloodGroupEditText = findViewById(R.id.bloodGroupEditText);
        weightEditText = findViewById(R.id.weightEditText);
        ageEditText = findViewById(R.id.ageEditText);
        genderSpinner = findViewById(R.id.genderSpinner);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        savedDetailsTextView = findViewById(R.id.savedDetailsTextView);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("medical_details");

        // Set up gender options in Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // Load saved medical details if they exist
        loadSavedDetails();

        // Set up button actions
        saveButton.setOnClickListener(v -> saveDetails());
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        // Setting focus change listeners to handle focus manually
        setFocusChangeListeners();
    }

    private void setFocusChangeListeners() {
        bloodGroupEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                clearFocusExcept(bloodGroupEditText);
            }
        });

        weightEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                clearFocusExcept(weightEditText);
            }
        });

        ageEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                clearFocusExcept(ageEditText);
            }
        });
    }

    private void clearFocusExcept(EditText current) {
        if (current != bloodGroupEditText) {
            bloodGroupEditText.clearFocus();
        }
        if (current != weightEditText) {
            weightEditText.clearFocus();
        }
        if (current != ageEditText) {
            ageEditText.clearFocus();
        }
    }

    private void loadSavedDetails() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        MedicalDetails medicalDetails = snapshot.getValue(MedicalDetails.class);
                        if (medicalDetails != null) {
                            recordId = snapshot.getKey(); // Get the record ID
                            displaySavedDetails(medicalDetails);
                            break; // Only display the first record for now
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MedicalDetailsActivity.this, "Failed to load details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDetails() {
        String bloodGroup = bloodGroupEditText.getText().toString().trim();
        String weight = weightEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();

        // Validate input
        if (bloodGroup.isEmpty() || weight.isEmpty() || age.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create object and save to Firebase
        MedicalDetails medicalDetails = new MedicalDetails(bloodGroup, weight, age, gender);
        recordId = databaseReference.push().getKey();
        if (recordId != null) {
            databaseReference.child(recordId).setValue(medicalDetails).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Details saved successfully!", Toast.LENGTH_SHORT).show();
                    displaySavedDetails(medicalDetails);
                    clearFields();
                } else {
                    Toast.makeText(this, "Failed to save details.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void displaySavedDetails(MedicalDetails medicalDetails) {
        String details = "Blood Group: " + medicalDetails.getBloodGroup() +
                "\nWeight: " + medicalDetails.getWeight() +
                "\nAge: " + medicalDetails.getAge() +
                "\nGender: " + medicalDetails.getGender();
        savedDetailsTextView.setText(details);
        savedDetailsTextView.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
    }

    private void clearFields() {
        bloodGroupEditText.setText("");
        weightEditText.setText("");
        ageEditText.setText("");
        genderSpinner.setSelection(0);

        // Hide the keyboard
        hideKeyboard();

        // Clear focus
        bloodGroupEditText.clearFocus();
        weightEditText.clearFocus();
        ageEditText.clearFocus();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Details")
                .setMessage("Are you sure you want to delete these details?")
                .setPositiveButton("Yes", (dialog, which) -> deleteDetails())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteDetails() {
        if (recordId != null) {
            databaseReference.child(recordId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Details deleted successfully.", Toast.LENGTH_SHORT).show();
                    clearFields();
                    savedDetailsTextView.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.GONE);
                }
            });
        }
    }
}
