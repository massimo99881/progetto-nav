package it.jacopo.nave;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.Random;

public class GameObject {
	String nome = "";
	Polygon shape;
	double speed;
	double angolo;
	double angolo2;
	int x, y;
	int astPosx, astPosy;
	
	void draw(Graphics2D g) {
		AffineTransform at = new AffineTransform();
		if(speed > 10) {speed = 10;}
		x += (int) (speed * Math.cos(angolo2));
        y += (int) (speed * Math.sin(angolo2));
    
        if(!nome.equals("ast")) {
        	System.out.println(nome);
        	speed *= 0.96;
        	at.translate(x, y);
        }else {
        	at.translate(x+astPosx, y+astPosy);
        }
    	//at.translate(x+100, y+100);
        at.rotate(angolo);
		
		System.out.println(nome + ":" + x + " " +y);
		g.draw(at.createTransformedShape(shape));
	}
	
	Shape getTransf() {
		AffineTransform at = new AffineTransform();
		at.translate(x, y);
		at.rotate(angolo);
		return at.createTransformedShape(shape);
	}
}

