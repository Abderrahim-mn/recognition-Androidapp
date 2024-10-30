package com.example.myrecognitionapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_VOICE_RECOGNITION = 300;
    private static final String TAG = "MainActivity";

    private PreviewView cameraView;
    private TextView resultTextView;
    private Button voiceButton; // Bouton pour déclencher la reconnaissance vocale
    private FaceOverlay faceOverlay;

    private FaceDetector detector;
    private ExecutorService cameraExecutor;

    private List<PersonFace> knownFaces; // Liste des visages connus

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultTextView = findViewById(R.id.resultTextView);
        cameraView = findViewById(R.id.cameraView);
        voiceButton = findViewById(R.id.voiceButton); // Initialiser le bouton vocal
        faceOverlay = findViewById(R.id.faceOverlay); // Overlay pour dessiner les rectangles

        // Initialiser le détecteur de visages
        detector = FaceDetection.getClient();

        // Charger les visages connus
        loadKnownFaces();

        // Vérifier la permission de la caméra et du microphone
        checkPermissions();

        // Initialiser l'exécuteur pour l'analyse des images
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Configuration du bouton pour déclencher la reconnaissance vocale
        voiceButton.setOnClickListener(v -> startVoiceRecognition());
    }

    // Méthode pour vérifier les permissions de la caméra et du microphone
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }
    }

    // Méthode pour démarrer la caméra
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Log.d(TAG, "Camera provider obtenu");

                // Configurer le preview pour afficher le flux vidéo
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraView.getSurfaceProvider());

                Log.d(TAG, "Preview lié au SurfaceProvider");

                // Configurer l'analyse d'image pour la reconnaissance faciale
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new android.util.Size(1280, 720)) // Ajuster la résolution si nécessaire
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Ne garder que la dernière image
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                // Utiliser la caméra frontale
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                // Lier la caméra au cycle de vie de l'activité
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

                Log.d(TAG, "Caméra liée au cycle de vie");

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Erreur lors de l'initialisation de la caméra", e);
            }
        }, ContextCompat.getMainExecutor(this));
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

    // Méthode pour récupérer les résultats de la reconnaissance vocale
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VOICE_RECOGNITION && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String recognizedText = result.get(0); // Récupérer la première commande reconnue

            // Afficher ou utiliser le texte reconnu
            resultTextView.setText("Commande vocale : " + recognizedText);

            // Effectuer des actions en fonction de la commande vocale
            handleVoiceCommand(recognizedText);
        }
    }

    // Méthode pour gérer les commandes vocales
    private void handleVoiceCommand(String command) {
        if (command.equalsIgnoreCase("back") || command.equalsIgnoreCase("retourner à la page principale")) {
            // Retourner à la page d'accueil
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Retour à la page principale", Toast.LENGTH_SHORT).show();
        } else {
            // Commande non reconnue
            Toast.makeText(this, "Commande non reconnue : " + command, Toast.LENGTH_SHORT).show();
        }
    }

    // Charger les visages connus et analyser les images de référence
    private void loadKnownFaces() {
        knownFaces = new ArrayList<>();
        analyzeReferenceImage(R.drawable.abdo, "abdo");
        analyzeReferenceImage(R.drawable.amina, "amina");
        analyzeReferenceImage(R.drawable.mark, "mark");
    }

    // Analyser une image de référence pour en extraire les caractéristiques faciales
    private void analyzeReferenceImage(int drawableId, String personName) {
        Bitmap bitmap = loadImageFromDrawable(drawableId);
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        Face face = faces.get(0); // Prendre le premier visage détecté
                        float[] faceEmbedding = generateFaceEmbedding(face);
                        knownFaces.add(new PersonFace(personName, faceEmbedding)); // Ajouter à la base de données
                        Log.d("FaceDetection", "Visage enregistré: " + personName);
                    } else {
                        Log.d("FaceDetection", "Aucun visage détecté dans l'image: " + personName);
                    }
                })
                .addOnFailureListener(e -> Log.e("FaceDetection", "Erreur lors de l'analyse de l'image: " + personName, e));
    }

    // Analyser chaque image du flux pour la reconnaissance faciale
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        @androidx.camera.core.ExperimentalGetImage
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            // Utiliser ML Kit pour détecter les visages
            detector.process(image)
                    .addOnSuccessListener(faces -> processFaceDetectionResult(faces, imageProxy))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur de détection de visage", e);
                        imageProxy.close(); // Fermer l'image après traitement
                    });
        } else {
            imageProxy.close(); // Fermer l'image si elle est nulle
        }
    }

    // Comparer les visages détectés avec ceux enregistrés
    private void processFaceDetectionResult(List<Face> faces, ImageProxy imageProxy) {
        List<RectF> faceBoundingBoxes = new ArrayList<>();

        if (!faces.isEmpty()) {
            StringBuilder faceInfo = new StringBuilder();

            int previewWidth = cameraView.getWidth();
            int previewHeight = cameraView.getHeight();
            float imageWidth = imageProxy.getWidth();
            float imageHeight = imageProxy.getHeight();

            for (Face face : faces) {
                // Simuler un embedding pour le visage détecté
                float[] faceEmbedding = generateFaceEmbedding(face);
                Log.d("FaceEmbedding", "Embedding généré pour le visage détecté : " + Arrays.toString(faceEmbedding));

                // Comparer l'embedding du visage détecté avec ceux des visages connus
                String recognizedPerson = FaceRecognitionUtils.recognizeFace(faceEmbedding, knownFaces, 5000f); // Ajustement du seuil ici

                faceInfo.append("Visage détecté: ").append(face.getBoundingBox().toString())
                        .append("\nPersonne reconnue: ").append(recognizedPerson)
                        .append("\n");

                // Convertir les coordonnées du visage pour les adapter à la taille du PreviewView
                RectF faceRect = new RectF(
                        translateX(face.getBoundingBox().left, imageWidth, previewWidth),
                        translateY(face.getBoundingBox().top, imageHeight, previewHeight),
                        translateX(face.getBoundingBox().right, imageWidth, previewWidth),
                        translateY(face.getBoundingBox().bottom, imageHeight, previewHeight)
                );

                faceBoundingBoxes.add(faceRect);
            }

            resultTextView.setText(faceInfo.toString());

            // Mettre à jour l'overlay pour dessiner les rectangles
            faceOverlay.setFaceBoundingBoxes(faceBoundingBoxes);
        } else {
            resultTextView.setText("Aucun visage détecté.");
            faceOverlay.setFaceBoundingBoxes(new ArrayList<>()); // Effacer les rectangles s'il n'y a pas de visage
        }

        imageProxy.close(); // Fermer l'image après traitement
    }

    // Simuler un embedding à partir des caractéristiques du visage
    private float[] generateFaceEmbedding(Face face) {
        float[] embedding = new float[128];

        // Utiliser des caractéristiques du visage pour simuler un embedding
        float width = face.getBoundingBox().width();
        float height = face.getBoundingBox().height();
        float left = face.getBoundingBox().left;
        float top = face.getBoundingBox().top;
        float smileProbability = face.getSmilingProbability() != null ? face.getSmilingProbability() : 0.5f;
        float leftEyeOpenProbability = face.getLeftEyeOpenProbability() != null ? face.getLeftEyeOpenProbability() : 0.5f;
        float rightEyeOpenProbability = face.getRightEyeOpenProbability() != null ? face.getRightEyeOpenProbability() : 0.5f;
        float headEulerAngleY = face.getHeadEulerAngleY(); // Rotation Y
        float headEulerAngleZ = face.getHeadEulerAngleZ(); // Rotation Z

        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = (width * (i + 1) / 100.0f) + (height * (i + 1) / 100.0f) +
                    (left * (i % 10 + 1) / 50.0f) + (top * (i % 5 + 1) / 50.0f) +
                    (smileProbability * (i % 3 + 1) / 25.0f) +
                    (leftEyeOpenProbability * (i % 2 + 1) / 25.0f) +
                    (rightEyeOpenProbability * (i % 2 + 1) / 25.0f) +
                    (headEulerAngleY * (i % 3 + 1) / 25.0f) +
                    (headEulerAngleZ * (i % 3 + 1) / 25.0f);
        }

        return embedding;
    }

    // Méthode pour charger une image depuis drawable
    private Bitmap loadImageFromDrawable(int drawableId) {
        Bitmap image = BitmapFactory.decodeResource(getResources(), drawableId);
        Log.d("ImageLoading", "Image chargée avec dimensions: " + image.getWidth() + "x" + image.getHeight());
        return image;
    }

    // Méthode pour traduire les coordonnées X
    private float translateX(float x, float imageWidth, float previewWidth) {
        return (x / imageWidth) * previewWidth;
    }

    // Méthode pour traduire les coordonnées Y
    private float translateY(float y, float imageHeight, float previewHeight) {
        return (y / imageHeight) * previewHeight;
    }

    // Gestion des permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Fermer l'exécuteur à la destruction de l'activité
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        detector.close(); // Fermer le détecteur de visages
    }
}
