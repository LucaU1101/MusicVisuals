package ie.tudublin;

import ddf.minim.*;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import processing.core.*;

public class gambinovisual2 extends Visual {

    Minim minim;
    AudioPlayer mySound;
    FFT fft;
    BeatDetect beat;
    float[] bands;
    float[] smoothedBands;
    int numBands = 256;
    float rotationSpeed = 0.01f;
    float amplitudeThreshold = 30;

    public void settings() {
        size(800, 600, P3D);
    }

    public void setup() {
        minim = new Minim(this);
        mySound = minim.loadFile("heroplanet.mp3", 1024);
        mySound.play();
        fft = new FFT(mySound.bufferSize(), mySound.sampleRate());
        beat = new BeatDetect(); // Initialize BeatDetect
        bands = new float[numBands];
        smoothedBands = new float[numBands];
        colorMode(HSB, 360, 100, 100);
    }

    public void draw() {
        background(0);
        fft.forward(mySound.mix);
        beat.detect(mySound.mix); // Detect the beat
    
        float totalAmplitude = 0;
        int countedBands = 0;
    
        for (int i = 0; i < numBands; i++) {
            bands[i] = fft.getBand(i);
            smoothedBands[i] = lerp(smoothedBands[i], bands[i], 0.1f);
    
            if (bands[i] > 0) {
                totalAmplitude += bands[i];
                countedBands++;
            }
        }
    
        float gap = width / (float) numBands;
        float halfHeight = height / 2.0f;
    
        strokeWeight(2);
        for (int i = 0; i < numBands; i++) {
            float hue = map(i, 0, numBands, 0, 360);
            stroke(hue, 100, 100);
            float amplitude = smoothedBands[i] * 2;
            if (amplitude > amplitudeThreshold) {
                drawCircle(i * gap, halfHeight, amplitude, hue);
            }
            line(i * gap, halfHeight, i * gap, halfHeight - amplitude);
            line(i * gap, halfHeight, i * gap, halfHeight + amplitude);
        }
    
        float averageAmplitude = totalAmplitude / countedBands;
    
        // Rotate the flower based on the beat
        if (beat.isOnset()) {
            rotationSpeed += 0.01f;
        } else {
            rotationSpeed = lerp(rotationSpeed, 0.005f, 0.1f); // Reduce rotation speed
        }
    
        pushMatrix();
        translate(width / 2, height / 2);
        rotateY(frameCount * rotationSpeed * 0.05f);
        rotateZ(frameCount * rotationSpeed * 0.05f);
        noFill();
        stroke(255);
        drawFlower(200 + averageAmplitude * 5); // Adjust the flower size based on the average amplitude
        popMatrix();
    }

    void drawCircle(float x, float y, float amplitude, float hue) {
        float size = map(amplitude, amplitudeThreshold, 200, 20, 150);
        stroke(hue, 100, 100, map(amplitude, amplitudeThreshold, 200, 50, 100));
        noFill();
        ellipse(x, y, size, size);
    }

    void drawFlower(float size) {
        int numPetals = 12; // Number of petals
        float petalLength = size / 3;

        // Rotate and draw petals around the center
        for (int i = 0; i < numPetals; i++) {
            float hue = map(i, 0,            numPetals, 0, 360);
            float angle = TWO_PI / numPetals * i;
            float x = cos(angle) * petalLength;
            float y = sin(angle) * petalLength;

            pushMatrix();
            translate(x, y);
            rotate(angle);
            drawPetal(petalLength, hue);
            popMatrix();
        }
    }

    void drawPetal(float length, float hue) {
        stroke(hue, 100, 100);
        fill(hue, 100, 50);
        beginShape();
        vertex(0, 0);
        bezierVertex(length / 2, -length / 2, length / 2, -length * 1.5f, 0, -length * 2);
        bezierVertex(-length / 2, -length * 1.5f, -length / 2, -length / 2, 0, 0);
        endShape(CLOSE);
    }

    public void stop() {
        mySound.close();
        minim.stop();
        super.stop();
    }
}

