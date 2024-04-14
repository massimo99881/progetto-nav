package it.jacopo.nave;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProiettilePool {
    private final List<Proiettile> available = new LinkedList<>();
    private final List<Proiettile> active = new LinkedList<>();
    private Map<String, Cache> obj = new HashMap<>();
    private static ProiettilePool instance = new ProiettilePool();
    private List<String> nomiAsteroidi = new ArrayList<>();
    private ProiettilePool() {}

    public static ProiettilePool getInstance() {
        return instance;
    }

    public Proiettile getProiettile(double x, double y, double angolo, String mittente) {
        Proiettile proiettile;
        if (available.isEmpty()) {
            proiettile = new Proiettile(x, y, angolo, mittente);
        } else {
            proiettile = available.remove(0);
            proiettile.reset(x, y, angolo, mittente);
        }
        active.add(proiettile);
        return proiettile;
    }

    public void releaseProiettile(Proiettile proiettile) {
        active.remove(proiettile);
        available.add(proiettile);
    }

    public List<Proiettile> getActiveProiettili() {
        return new LinkedList<>(active); // Return a copy to avoid modification outside
    }
    
    public Map<String, Cache> getObj(){
    	return this.obj;
    }

	public List<String> getNomiAsteroidi() {
		return nomiAsteroidi;
	}
}
