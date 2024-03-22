package it.jacopo.nave;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Random;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Random;

public class Asteroide extends GameObject {
    int centroX = 0;
    int centroY = 0;
    double raggio = new Random().nextInt(20) + 40;
    int primax = 0;
    int primay = 0;
    int npunti = 16;
    int rand1, rand2;
    int x1, y1;

    double angoloRotazione; // Angolo di rotazione per la rotazione casuale

    public Asteroide(String nome) {
    	this.name=nome;
        x = 0;
        y = 0;
        this.speed = 2.0 + Math.random() * 3.0; // Velocità casuale da 2.0 a 5.0

        shape = new Polygon();
        shape.addPoint((int) (centroX), (int) (centroY));

        for (int i = 0; i < npunti; i++) {
            double angolo1 = 2 * Math.PI * i / npunti;
            x1 = (int) (centroX + raggio * Math.cos(angolo1));
            y1 = (int) (centroY + raggio * Math.sin(angolo1));
            rand1 = (new Random().nextInt((int) (raggio / 6)) - (int) (raggio / 6) / 2);
            rand2 = (new Random().nextInt((int) (raggio / 6)) - (int) (raggio / 6) / 2);
            if (i == 0) {
                primax = x1 + rand1;
                primay = y1 + rand2;
            }
            System.out.println("coords" + x1 + " " + y1);
            shape.addPoint(x1 + rand1, y1 + rand2);
        }
        shape.addPoint(primax, primay);

        // Imposta una velocità di rotazione casuale
        angoloRotazione = (Math.random() * 2 - 1) * Math.PI / 180; // Rotazione casuale da -1 a 1 grado per frame
    }

    @Override
    void draw(Graphics2D g) {
        // Rotazione dell'asteroide
        angolo += angoloRotazione;

        // Chiama il metodo draw di GameObject per gestire il movimento e disegnare l'asteroide
        super.draw(g);
    }

    @Override
    void updateMovement() {
        // Aggiorna l'angolo di rotazione per far ruotare l'asteroide attorno a se stesso
        angolo += angoloRotazione;

        // Aggiunge una variazione casuale alla velocità lineare
        double deltaSpeed = (new Random().nextDouble() - 0.5) * 0.5; // Variazione casuale da -0.5 a +0.5
        speed += deltaSpeed;

        // Limita la velocità minima e massima
        double minSpeed = 1.0; // Velocità minima
        double maxSpeed = 5.0; // Velocità massima
        speed = Math.max(minSpeed, Math.min(maxSpeed, speed));

        // Aggiorna la posizione dell'asteroide in base alla sua velocità di movimento lineare
        x += (int) (speed * Math.cos(angolo));
        y += (int) (speed * Math.sin(angolo));
    }

}

