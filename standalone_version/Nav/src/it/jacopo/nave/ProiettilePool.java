package it.jacopo.nave;

import java.util.LinkedList;
import java.util.List;

public class ProiettilePool {
    private final List<Proiettile> available = new LinkedList<>();

    public Proiettile getProiettile(double x, double y, double angolo) {
        if (available.isEmpty()) {
            return new Proiettile(x, y, angolo);
        } else {
            Proiettile proiettile = available.remove(0); // Prende il primo proiettile disponibile
            proiettile.reset(x, y, angolo); // Reimposta le proprietà del proiettile
            return proiettile;
        }
    }

    public void releaseProiettile(Proiettile proiettile) {
        available.add(proiettile); // Restituisce il proiettile al pool
    }
}

