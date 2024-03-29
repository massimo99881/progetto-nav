package it.jacopo.nave;

import java.awt.Color;
import java.awt.Graphics2D;

public class Proiettile {
    double x, y; // Posizione del proiettile
    double velocita = 5; // Velocità del proiettile

    public Proiettile(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Metodo per aggiornare la posizione del proiettile
    void aggiorna() {
        x += velocita;
    }

    // Metodo per disegnare il proiettile
    void disegna(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillOval((int)x, (int)y, 5, 5); // Disegna un piccolo cerchio come proiettile
    }
}
