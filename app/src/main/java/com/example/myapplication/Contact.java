package com.example.myapplication;

public class Contact {
    private String name;
    private String number;

    // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    public Contact() {
    }

    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return name + " - " + number; // Format the output as "Name - PhoneNumber"
    }
}
