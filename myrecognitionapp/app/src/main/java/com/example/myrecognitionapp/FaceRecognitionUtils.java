package com.example.myrecognitionapp;

import android.util.Log;

import java.util.List;

public class FaceRecognitionUtils {

    // Méthode pour calculer la distance euclidienne entre deux embeddings
    public static float calculateEuclideanDistance(float[] embedding1, float[] embedding2) {
        float distance = 0.0f;
        for (int i = 0; i < embedding1.length; i++) {
            float diff = embedding1[i] - embedding2[i];
            distance += diff * diff;
        }
        return (float) Math.sqrt(distance);
    }

    // Méthode pour comparer les visages et trouver une correspondance
    public static String recognizeFace(float[] faceEmbedding, List<PersonFace> knownFaces, float threshold) {
        String recognizedPerson = "Inconnu"; // Par défaut, personne n'est reconnu
        float minDistance = Float.MAX_VALUE;

        for (PersonFace personFace : knownFaces) {
            float distance = calculateEuclideanDistance(faceEmbedding, personFace.getFaceEmbedding());
            Log.d("Recognition", "Distance calculée avec " + personFace.getName() + ": " + distance);

            // Ajuster le seuil si nécessaire pour tolérer des petites variations
            if (distance < minDistance && distance < threshold) {
                minDistance = distance;
                recognizedPerson = personFace.getName();
            }
        }

        Log.d("Recognition", "Personne reconnue: " + recognizedPerson);
        return recognizedPerson;
    }

}
