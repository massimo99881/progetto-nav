package it.jacopo.nave;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class Proiettile  {
    double x, y; // Posizione del proiettile
    String mittente;
    
    public double getX() {
        return x;
    }

    // Getter per y
    public double getY() {
        return y;
    }
    double velocita = 7; // Velocità del proiettile
    double angolo;
    
    public Proiettile(double x, double y, double angolo) {
        this.x = x;
        this.y = y;
        this.angolo = angolo;
        this.velocita = 7; // Potresti voler utilizzare una velocità costante o basata sulla velocità della navicella
    }
    
    public Proiettile(double x, double y, double angolo, String mittente) {
        this.x = x;
        this.y = y;
        this.angolo = angolo;
        this.mittente = mittente;
        this.velocita = 7; // O una velocità basata sulla navicella, ecc.
    }

    
    public String getMittente() {
        return mittente;
    }
    
    void aggiorna() {
        x += velocita * Math.cos(angolo);
        y += velocita * Math.sin(angolo);
    }

    //ti assicuri che dopo la chiamata a disegna della classe Proiettile, il colore utilizzato per 
    //disegnare gli altri oggetti sul pannello (come la Nav) non sia influenzato dal colore impostato per i proiettili.
    void disegna(Graphics2D g) {
        Color originalColor = g.getColor(); // Memorizza il colore originale
        try {
            g.setColor(Color.WHITE);
            g.fillOval((int)x, (int)y, 5, 5); // Disegna un piccolo cerchio come proiettile
        } finally {
            g.setColor(originalColor); // Reimposta il colore originale
        }
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
    
    public void reset(double x, double y, double angolo) {
        this.x = x;
        this.y = y;
        this.angolo = angolo;
    }
}
