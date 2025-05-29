package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class DeleteContactsActivity extends AppCompatActivity {

    private ListView contactsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_contacts);

        contactsListView = findViewById(R.id.contactsListView);
        fetchAndDisplayContacts();
    }

    private void fetchAndDisplayContacts() {
        ContactsManager.getInstance().fetchContactsFromDatabase(new ContactFetchCallback() {
            @Override
            public void onContactsFetched(ArrayList<Contact> contacts) {
                if (contacts.isEmpty()) {
                    showNoContactsMessage();
                } else {
                    ContactAdapter adapter = new ContactAdapter(DeleteContactsActivity.this, contacts);
                    contactsListView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(DeleteContactsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoContactsMessage() {
        Toast.makeText(this, "No contacts available to delete.", Toast.LENGTH_SHORT).show();

        // Go back to the previous activity after showing the toast
        new Handler().postDelayed(this::finish, 2000); // Delay of 2 seconds
    }

    // Custom Adapter for Contacts with delete button functionality
    private class ContactAdapter extends ArrayAdapter<Contact> {
        private final ArrayList<Contact> contacts;

        public ContactAdapter(DeleteContactsActivity context, ArrayList<Contact> contacts) {
            super(context, 0, contacts);
            this.contacts = contacts;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_list_item, parent, false);
            }

            Contact contact = getItem(position);
            TextView contactName = convertView.findViewById(R.id.contactName);
            TextView contactNumber = convertView.findViewById(R.id.contactNumber);
            Button deleteButton = convertView.findViewById(R.id.deleteButton);

            contactName.setText(contact.getName());
            contactNumber.setText(contact.getNumber()); // Display contact number

            deleteButton.setOnClickListener(v -> {
                // Show confirmation dialog
                new AlertDialog.Builder(DeleteContactsActivity.this)
                        .setTitle("Delete Contact")
                        .setMessage("Are you sure you want to delete " + contact.getName() + " (" + contact.getNumber() + ")?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            ContactsManager.getInstance().deleteContact(contact.getNumber());
                            Toast.makeText(DeleteContactsActivity.this, "Contact deleted: " + contact.getName(), Toast.LENGTH_SHORT).show();
                            fetchAndDisplayContacts(); // Refresh the list
                        })
                        .setNegativeButton("No", null)
                        .show();
            });

            return convertView;
        }
    }
}
