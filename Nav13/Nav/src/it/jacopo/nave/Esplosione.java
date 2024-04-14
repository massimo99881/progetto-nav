package it.jacopo.nave;

import java.awt.Color;
import java.awt.Graphics2D;

public class Esplosione {
    double x, y; // Posizione dell'esplosione
    int raggio = 0; // Raggio dell'esplosione

    public Esplosione(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Metodo per aggiornare l'esplosione
    boolean aggiorna() {
        raggio += 2;
        if (raggio > 30) { // L'esplosione scompare dopo aver raggiunto un certo raggio
            return false; // Indica che l'esplosione è terminata
        }
        return true; // L'esplosione continua
    }

    // Metodo per disegnare l'esplosione
    void disegna(Graphics2D g) {
        g.setColor(Color.ORANGE);
        g.drawOval((int)(x - raggio / 2), (int)(y - raggio / 2), raggio, raggio);
    }
}
