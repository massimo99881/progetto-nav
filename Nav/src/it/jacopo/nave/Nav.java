package it.jacopo.nave;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;

public class Nav extends GameObject{
	String nome;
	public Nav(String nome) {
		this.nome = nome;
		this.x = 0;
		this.y = 0;
		shape = new Polygon();
		shape.addPoint(30, 0);
		shape.addPoint(-30, -20);
		shape.addPoint(-30, 20);
	}
	
	@Override
	void draw(Graphics2D g) {
	    // Imposta il colore del contorno in bianco
	    g.setColor(Color.WHITE);
	    
	    // Imposta lo spessore del contorno
	    g.setStroke(new BasicStroke(3)); // Aumenta questo numero per un contorno più spesso

	    // Disegna la navicella
	    // Assumendo che `shape` sia una variabile membro di tipo Polygon o simile
	    AffineTransform at = new AffineTransform();
		if(speed > 10) {speed = 10;}
		x += (int) (speed * Math.cos(angolo));
        y += (int) (speed * Math.sin(angolo));
        speed *= 0.96;
		at.translate(x, y);
		at.rotate(angolo);
		g.draw(at.createTransformedShape(shape));
	}
}