package com.example.myrecognitionapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_VOICE_RECOGNITION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Bouton pour la reconnaissance faciale
        Button faceRecognitionButton = findViewById(R.id.btnFaceRecognition);
        faceRecognitionButton.setOnClickListener(v -> {
            // Rediriger vers l'activité de reconnaissance faciale
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Bouton pour la reconnaissance vocale
        Button voiceRecognitionButton = findViewById(R.id.btnVoiceRecognition);
        voiceRecognitionButton.setOnClickListener(v -> {
            // Rediriger vers l'activité de reconnaissance vocale
            Intent intent = new Intent(HomeActivity.this, VoiceRecognitionActivity.class);
            startActivity(intent);
        });

        // Bouton pour lancer la commande vocale
        Button voiceCommandButton = findViewById(R.id.btnVoiceCommand);
        voiceCommandButton.setOnClickListener(v -> startVoiceRecognition());

        // Bouton "About Me"
        Button aboutMeButton = findViewById(R.id.btnAboutMe);
        aboutMeButton.setOnClickListener(v -> showAboutDialog());  // Affiche la boîte de dialogue
    }

    // Méthode pour afficher la boîte de dialogue "About Me"
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("À propos de l'application");
        builder.setMessage("Cette application utilise la reconnaissance vocale et faciale pour offrir une interaction intuitive avec les utilisateurs.\n" +
                "                + Elle permet également d'envoyer des SMS basés sur les \t \n" +
                "                  commandes vocales détectées.\n"+
                "                + Copyright © 2024 . Tous droits réservés.\n" +
                "                + Développée par :  \n"+
                "                  --> MOUNOUAR ABBDERRAHIM.\n"+
                "                  --> EL GHAZOUANI AMINA.\n");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();  // Fermer la boîte de dialogue
            }
        });

        // Afficher la boîte de dialogue
        builder.show();
    }

    // Méthode pour lancer la reconnaissance vocale
    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites 'Reconnaissance faciale' ou 'Reconnaissance vocale'");

        try {
            startActivityForResult(intent, REQUEST_VOICE_RECOGNITION);
        } catch (Exception e) {
            Toast.makeText(this, "La reconnaissance vocale n'est pas supportée", Toast.LENGTH_SHORT).show();
        }
    }

    // Récupérer le résultat de la reconnaissance vocale
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VOICE_RECOGNITION && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String recognizedCommand = results.get(0).toLowerCase(Locale.ROOT);  // Obtenir la commande reconnue
            handleVoiceCommand(recognizedCommand);  // Gérer la commande vocale
        }
    }

    // Gérer la commande vocale
    private void handleVoiceCommand(String command) {
        if (command.contains("face")) {
            // Rediriger vers l'activité de reconnaissance faciale
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (command.contains("vocale")) {
            // Rediriger vers l'activité de reconnaissance vocale
            Intent intent = new Intent(HomeActivity.this, VoiceRecognitionActivity.class);
            startActivity(intent);
        } else {
            // Si la commande n'est pas reconnue
            Toast.makeText(this, "Commande non reconnue", Toast.LENGTH_SHORT).show();
        }
    }
}
