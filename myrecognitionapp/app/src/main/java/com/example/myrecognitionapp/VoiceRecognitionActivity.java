package com.example.myrecognitionapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceRecognitionActivity extends AppCompatActivity {

    private static final int REQUEST_VOICE_RECOGNITION = 300;
    private static final int REQUEST_SMS_PERMISSION = 400;
    private TextView voiceResultTextView;
    private Button sendSmsButton;
    private Button btnStartVoiceRecognition; // Bouton pour démarrer la reconnaissance vocale
    private String recognizedText = ""; // Texte reconnu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recognition);

        voiceResultTextView = findViewById(R.id.voiceResultTextView);
        sendSmsButton = findViewById(R.id.sendSmsButton);
        btnStartVoiceRecognition = findViewById(R.id.btnStartVoiceRecognition);

        sendSmsButton.setEnabled(false); // Désactiver jusqu'à ce qu'un texte soit reconnu

        // Bouton pour démarrer la reconnaissance vocale
        btnStartVoiceRecognition.setOnClickListener(v -> startVoiceRecognition());

        // Bouton pour envoyer un SMS après la reconnaissance vocale
        sendSmsButton.setOnClickListener(v -> {
            if (recognizedText.isEmpty()) {
                Toast.makeText(this, "Aucun texte reconnu", Toast.LENGTH_SHORT).show();
                return;
            }
            requestSmsPermission(); // Demander la permission d'envoyer un SMS
        });
    }

    // Méthode pour déclencher la reconnaissance vocale
    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites quelque chose...");

        try {
            startActivityForResult(intent, REQUEST_VOICE_RECOGNITION);
        } catch (Exception e) {
            Toast.makeText(this, "Reconnaissance vocale non supportée", Toast.LENGTH_SHORT).show();
        }
    }

    // Récupérer le résultat de la reconnaissance vocale
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VOICE_RECOGNITION && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            recognizedText = result.get(0); // Récupérer le premier résultat

            // Afficher le texte reconnu
            voiceResultTextView.setText("Message  : " + recognizedText);

            // Activer le bouton pour envoyer un SMS
            sendSmsButton.setEnabled(true);
        }
    }

    // Demander la permission d'envoyer des SMS
    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
        } else {
            showSmsDialog(); // Si la permission est déjà accordée, montrer la boîte de dialogue pour le numéro
        }
    }

    // Boîte de dialogue pour entrer le numéro de téléphone
    private void showSmsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Envoyer SMS");

        final EditText input = new EditText(this);
        input.setHint("Insérez le numéro de téléphone");
        builder.setView(input);

        builder.setPositiveButton("Envoyer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String phoneNumber = input.getText().toString();
                if (!phoneNumber.isEmpty()) {
                    sendSms(phoneNumber, recognizedText);
                } else {
                    Toast.makeText(VoiceRecognitionActivity.this, "Numéro invalide", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Envoyer le SMS
    private void sendSms(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(this, "SMS envoyé", Toast.LENGTH_SHORT).show();
    }

    // Gestion des permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSmsDialog(); // Si la permission est accordée, montrer la boîte de dialogue
            } else {
                Toast.makeText(this, "Permission d'envoyer SMS refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
