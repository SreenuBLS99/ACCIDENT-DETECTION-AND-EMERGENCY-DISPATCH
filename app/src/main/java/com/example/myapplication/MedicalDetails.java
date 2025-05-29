package com.example.myapplication;

public class MedicalDetails {
    private String bloodGroup;
    private String weight;
    private String age;
    private String gender;

    public MedicalDetails() {
        // Default constructor required for calls to DataSnapshot.getValue(MedicalDetails.class)
    }

    public MedicalDetails(String bloodGroup, String weight, String age, String gender) {
        this.bloodGroup = bloodGroup;
        this.weight = weight;
        this.age = age;
        this.gender = gender;
    }

    // Getters and Setters
    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
