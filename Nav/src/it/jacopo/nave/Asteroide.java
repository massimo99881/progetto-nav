package it.jacopo.nave;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Random;

public class Asteroide extends GameObject{
	int centroX = 0;
	int centroY = 0;
	double raggio = new Random().nextInt(20)+40;
	int primax = 0;
	int primay = 0;
	int npunti = 16;
	int rand1, rand2;
	int x1, y1;
	
	double angoloRotazione; // Angolo di rotazione per la rotazione casuale

	public Asteroide(String nome) {
		
//		this.nome = nome;
		x = 0;
		y = 0;
		this.speed = 5.0 + Math.random() * 3.0; // Velocità da 5.0 a 8.0
		
//		astPosx = new Random().nextInt(1200);
//		astPosy = new Random().nextInt(800);
//		
//		angolo2 = new Random().nextInt(100);
		shape = new Polygon();
		shape.addPoint((int)(centroX), (int)(centroY));
		
		for(int i=0; i<npunti; i++) {
			double angolo1 = 2 * Math.PI * i / npunti;
            x1 = (int)(centroX + raggio * Math.cos(angolo1));
            y1 = (int)(centroY + raggio * Math.sin(angolo1));
            rand1 = (new Random().nextInt((int)(raggio/6))-(int)(raggio/6)/2);
            rand2 = (new Random().nextInt((int)(raggio/6))-(int)(raggio/6)/2);
            if(i == 0) {
            	primax = x1 + rand1;
            	primay = y1 + rand2;
            }
            System.out.println("coords"+x1+ " "+y1);
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
}