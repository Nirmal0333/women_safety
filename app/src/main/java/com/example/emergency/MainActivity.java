package com.example.emergency;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private EditText editText1;
    private EditText editText2;
    private Button btnSendMessage;
    private SharedPreferences sharedPreferences;

    // Variables for floating icon
    private WindowManager windowManager;
    private View floatingView;
    private int clickCounter = 0;
    private static final int OVERLAY_PERMISSION_CODE = 1234;
    private static final int SMS_PERMISSION_CODE = 5678;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        // Initialize SharedPreferences
        sharedPreferences = getPreferences(MODE_PRIVATE);
        String savedText1 = sharedPreferences.getString("text1", "");
        String savedText2 = sharedPreferences.getString("text2", "");
        editText1.setText(savedText1);
        editText2.setText(savedText2);

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDataToSharedPreferences();
                initializeFloatingWidget(); // Initialize the floating widget
                showFloatingWidget();
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                startActivity(homeIntent);

            }
        });
    }

    private void initializeFloatingWidget() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.widget_layout, null);

        // Set the long click listener for the floating widget
        floatingView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Handle triple click action here
                handleTripleClick();
                return true;
            }
        });

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    private void handleTripleClick() {
        Log.d("TripleClick", "Click Counter: " + clickCounter);

        if (clickCounter == 3) {
            sendEmergencyMessages();
            hideFloatingWidget();
            clickCounter = 0;
        }
    }

    private void sendEmergencyMessages() {
        // Add the code to send emergency messages
        String phoneNumber1 = sharedPreferences.getString("text1", "");
        String phoneNumber2 = sharedPreferences.getString("text2", "");

        // Your emergency message content
        String emergencyMessage = "Emergency: Please help!";

        try {
            // Use SmsManager to send messages
            SmsManager smsManager = SmsManager.getDefault();
            // Send messages to phone numbers
            smsManager.sendTextMessage(phoneNumber1, null, emergencyMessage, null, null);
            smsManager.sendTextMessage(phoneNumber2, null, emergencyMessage, null, null);
            // Display a toast or alert that emergency messages have been sent
            Toast.makeText(this, "Emergency messages sent!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception, show a message, or log it
            Toast.makeText(this, "Failed to send emergency messages", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndRequestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE
            );
        } else {
            // Permission is already granted, send text messages
            sendTextMessages();
            // Reset the counter after sending the messages
            clickCounter = 0;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            // Check if the SMS permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, send text messages
                sendTextMessages();
            } else {
                // Permission is denied, show a message or handle accordingly
                Toast.makeText(this, "SMS permission is required to send messages", Toast.LENGTH_SHORT).show();
            }
            // Reset the counter regardless of permission status
            clickCounter = 0;
        }
    }

    private void sendTextMessages() {
        String phoneNumber1 = sharedPreferences.getString("text1", "");
        String phoneNumber2 = sharedPreferences.getString("text2", "");

        // Your message content
        String message = "Your custom message here.";

        try {
            // Use SmsManager to send messages
            SmsManager smsManager = SmsManager.getDefault();
            // Send messages to phone numbers
            smsManager.sendTextMessage(phoneNumber1, null, message, null, null);
            smsManager.sendTextMessage(phoneNumber2, null, message, null, null);
            // Display a toast or alert that messages have been sent
            Toast.makeText(this, "Emergency: Please help!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception, show a message, or log it
            Toast.makeText(this, "Failed to send messages", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_CODE);
            } else {
                showOverlay();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // Permission granted, show the floating widget
                    showOverlay();
                } else {
                    // Permission not granted
                    Toast.makeText(this, "Overlay permission is required for the widget", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showFloatingWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Request overlay permission if not granted
            requestOverlayPermission();
        } else {
            // Overlay permission is granted, show the floating widget
            showOverlay();
        }
    }

    private void showOverlay() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.widget_layout, null);

        // Set the long click listener for the floating widget
        floatingView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Handle triple click action here
                clickCounter++;

                if (clickCounter == 3) {
                    checkAndRequestSmsPermission(); // Check and request SMS permission
                    sendEmergencyMessages();
                    hideFloatingWidget();
                    clickCounter = 0;

                }

                return true;
            }
        });

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                500,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        // Set the gravity of the widget view (e.g., top | right)
        params.gravity = Gravity.TOP;

        // Add the widget view to the window
        windowManager.addView(floatingView, params);
    }

    private void saveDataToSharedPreferences() {
        // Retrieve text from EditText fields
        String text1 = editText1.getText().toString();
        String text2 = editText2.getText().toString();

        // Save data to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("text1", text1);
        editor.putString("text2", text2);
        editor.apply();

        Toast.makeText(this, "Data saved to SharedPreferences", Toast.LENGTH_SHORT).show();
    }
    private void hideFloatingWidget() {
        if (windowManager != null && floatingView != null) {
            windowManager.removeViewImmediate(floatingView);
        }
    }
}
