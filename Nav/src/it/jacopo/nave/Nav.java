package it.jacopo.nave;

import java.awt.Polygon;

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
	
}