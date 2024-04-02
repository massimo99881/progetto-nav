package it.jacopo.nave;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

public class GameObject {
	String name;
	Polygon shape;
	double speed;
	double angolo;
	int x, y;
	
	void draw(Graphics2D g) {
		AffineTransform at = new AffineTransform();
		if(speed > 10) {speed = 10;}
		x += (int) (speed * Math.cos(angolo));
        y += (int) (speed * Math.sin(angolo));
        speed *= 0.96;
		at.translate(x, y);
		at.rotate(angolo);
		g.draw(at.createTransformedShape(shape));
	}
	
	Shape getTransf() {
		AffineTransform at = new AffineTransform();
		at.translate(x, y);
		at.rotate(angolo);
		return at.createTransformedShape(shape);
	}

	void updateMovement() {
	}
	
	Rectangle getBounds() {
	    return getTransf().getBounds();
	}
}
