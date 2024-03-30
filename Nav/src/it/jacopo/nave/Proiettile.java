package it.jacopo.nave;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class Proiettile {
    double x, y; // Posizione del proiettile
    double velocita = 5; // Velocità del proiettile

    public Proiettile(double x, double y) {
        this.x = x;
        this.y = y;
    }

    void aggiorna() {
        x += velocita;
    }

    void disegna(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillOval((int)x, (int)y, 5, 5); // Disegna un piccolo cerchio come proiettile
    }
    
    // Metodo che restituisce la Shape del proiettile per il rilevamento delle collisioni
    public Shape getShape() {
        // Assumendo che il proiettile sia rappresentato come un cerchio di diametro 5
        return new Ellipse2D.Double(x, y, 5, 5);
    }
    
 // Metodo per ottenere il bounding box del proiettile
    public Rectangle getBounds() {
        // La dimensione del proiettile è 5x5, quindi il rettangolo avrà le stesse dimensioni
        return new Rectangle((int)x, (int)y, 5, 5);
    }
}
