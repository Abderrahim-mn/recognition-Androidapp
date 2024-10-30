package com.example.myrecognitionapp;

public class PersonFace {
    private String name;
    private float[] faceEmbedding; // Embedding ou caractéristique faciale simulée

    public PersonFace(String name, float[] faceEmbedding) {
        this.name = name;
        this.faceEmbedding = faceEmbedding;
    }

    public String getName() {
        return name;
    }

    public float[] getFaceEmbedding() {
        return faceEmbedding;
    }
}
