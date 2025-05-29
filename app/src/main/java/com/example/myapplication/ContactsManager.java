package com.example.myapplication;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ContactsManager {

    private static ContactsManager instance;
    private final DatabaseReference databaseReference;

    // Constructor
    private ContactsManager() {
        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("contacts");
    }

    public static synchronized ContactsManager getInstance() {
        if (instance == null) {
            instance = new ContactsManager();
        }
        return instance;
    }

    // Fetch contacts from the database asynchronously
    public void fetchContactsFromDatabase(ContactFetchCallback callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Contact> contacts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Contact contact = snapshot.getValue(Contact.class);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                }
                callback.onContactsFetched(contacts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure("Failed to load contacts: " + databaseError.getMessage());
            }
        });
    }

    // Method to delete a contact
    public void deleteContact(String contactNumber) {
        databaseReference.orderByChild("number").equalTo(contactNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}
