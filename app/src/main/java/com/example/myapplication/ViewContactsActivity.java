package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewContactsActivity extends AppCompatActivity {

    private ListView contactsListView;
    private DatabaseReference databaseReference;
    private ArrayList<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contacts);

        contactsListView = findViewById(R.id.contactsListView);
        databaseReference = FirebaseDatabase.getInstance().getReference("contacts"); // Reference to "contacts" node
        contacts = new ArrayList<>();

        displayContacts();
    }

    private void displayContacts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contacts.clear(); // Clear the list before adding new data
                for (DataSnapshot contactSnapshot : dataSnapshot.getChildren()) {
                    Contact contact = contactSnapshot.getValue(Contact.class);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                }

                if (contacts.isEmpty()) {
                    Toast.makeText(ViewContactsActivity.this, "No contacts found. Please add a contact number first.", Toast.LENGTH_SHORT).show();
                    // Go back to the previous activity after showing the toast
                    new Handler().postDelayed(ViewContactsActivity.this::finish, 2000); // Delay of 2 seconds
                } else {
                    ArrayAdapter<Contact> adapter = new ArrayAdapter<>(ViewContactsActivity.this, android.R.layout.simple_list_item_1, contacts);
                    contactsListView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewContactsActivity.this, "Failed to load contacts.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
