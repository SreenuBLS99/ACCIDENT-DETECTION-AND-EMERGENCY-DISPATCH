#include <ESP8266WiFi.h>
#include <FirebaseESP8266.h>  // Firebase library for ESP8266

// Wi-Fi credentials
#define WIFI_SSID ""
#define WIFI_PASSWORD ""

// Firebase project credentials
#define FIREBASE_HOST ""
#define FIREBASE_AUTH ""

FirebaseData firebaseData;
FirebaseConfig config;
FirebaseAuth auth;

const int irPin = D1; // GPIO pin connected to IR sensor
bool objectDetected = false; // To track the detection status

void setup() {
  Serial.begin(115200);

  // Connect to Wi-Fi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println("\nConnected to Wi-Fi");

  // Configure Firebase
  config.host = FIREBASE_HOST;
  config.signer.tokens.legacy_token = FIREBASE_AUTH;

  // Initialize Firebase
  Firebase.begin(&config, &auth);
  pinMode(irPin, INPUT);  // Set IR pin as input
}

void loop() {
  // Read IR sensor value
  int irValue = digitalRead(irPin);

  // Check if the IR sensor detects an object
  if (irValue == LOW && !objectDetected) { // Assuming LOW means detection
    objectDetected = true;  // Update detection status

    // Print and send data to Firebase
    Serial.println("Object detected!");
    if (Firebase.setInt(firebaseData, "/irSensor/value", 1)) {
      Serial.println("Detection status sent to Firebase");
    } else {
      Serial.print("Error sending data: ");
      Serial.println(firebaseData.errorReason());
    }
  } else if (irValue == HIGH && objectDetected) {  // If no object is detected anymore
    objectDetected = false;  // Update detection status

    // Send data to Firebase to indicate no detection
    if (Firebase.setInt(firebaseData, "/irSensor/value", 0)) {
      Serial.println("No detection status sent to Firebase");
    } else {
      Serial.print("Error sending data: ");
      Serial.println(firebaseData.errorReason());
    }
  }

  delay(100);  // Small delay for sensor reading stability
}