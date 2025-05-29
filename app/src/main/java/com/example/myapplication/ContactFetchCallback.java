package com.example.myapplication;

import java.util.ArrayList;

public interface ContactFetchCallback {
    void onContactsFetched(ArrayList<Contact> contacts);
    void onFailure(String errorMessage);
}
