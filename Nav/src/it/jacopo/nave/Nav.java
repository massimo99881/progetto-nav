package it.jacopo.nave;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Nav extends GameObject{
	String nome;
	BufferedImage navImageSpenta; // Immagine della navicella
	BufferedImage navImageAccesa; // Immagine della navicella
	BufferedImage imageToDraw ;
	
	public Nav(String nome) {
		super(nome);
		this.nome = nome;
		this.x = 0;
		this.y = 0;

		shape = new Polygon();
		shape.addPoint(30, 0); // Punto di cima rimane centrato ma spostato leggermente a destra per aumentare la base
		shape.addPoint(-15, -25); // Punti base spostati verso l'esterno e meno in basso
		shape.addPoint(-15, 25); // Punti base spostati verso l'esterno e meno in basso

		
		try {
            BufferedImage tempImageSpenta = ImageIO.read(new File(Conf._RESOURCES_IMG_PATH + "astronavespenta.png")); // Sostituisci con il percorso corretto
            BufferedImage tempImageAccesa = ImageIO.read(new File(Conf._RESOURCES_IMG_PATH + "astronaveaccesa.png"));
            imageToDraw = tempImageSpenta;

            // Calcola le nuove dimensioni mantenendo le proporzioni e riducendo del 80%
            double misura = 0.082;
            int newWidthSpenta = (int) (tempImageSpenta.getWidth() * misura);
            int newHeightSpenta = (int) (tempImageSpenta.getHeight() * misura);
            int newWidthAccesa = (int) (tempImageAccesa.getWidth() * misura);
            int newHeightAccesa = (int) (tempImageAccesa.getHeight() * misura);

            // Ridimensiona le immagini
            navImageSpenta = toBufferedImage(tempImageSpenta.getScaledInstance(newWidthSpenta, newHeightSpenta, Image.SCALE_SMOOTH));
            navImageAccesa = toBufferedImage(tempImageAccesa.getScaledInstance(newWidthAccesa, newHeightAccesa, Image.SCALE_SMOOTH));
            
//            shape = Util.getPolygonFromImage(imageToDraw);
        } catch (IOException e) {
            e.printStackTrace();
            navImageSpenta = null; // Gestisci l'errore di caricamento
            navImageAccesa = null;
        }
	}
	
	@Override
	void draw(Graphics2D g) {
		//g.setColor(Color.WHITE);
	    // Imposta lo spessore del contorno
	    //g.setStroke(new BasicStroke(3));
	    
	    if(speed <= Double.MIN_VALUE) 
	    	imageToDraw = navImageSpenta; 
	    else 
	    	imageToDraw = navImageAccesa;
	    
	    int imageWidth = imageToDraw.getWidth();
	    int imageHeight = imageToDraw.getHeight();
	    
	    if(speed > 10) {speed = 10;}
		x += (int) (speed * Math.cos(angolo));
        y += (int) (speed * Math.sin(angolo));
        speed *= 0.96;

	    // Calcola la trasformazione per centrare l'immagine sulla posizione attuale della navicella
	    AffineTransform at = new AffineTransform();
	    at.translate(x - imageWidth / 2, y - imageHeight / 2); // Centra l'immagine sulla posizione (x, y)
	    at.rotate(angolo, imageWidth / 2, imageHeight / 2); // Ruota attorno al centro dell'immagine

	    // Disegna l'immagine con la trasformazione applicata
	    g.drawImage(imageToDraw, at, null);
	    
	    //Per debug: shape a forma di triangolo
//	    AffineTransform at2 = new AffineTransform();
//	    at2.translate(x, y);
//		at2.rotate(angolo);
//	    g.draw(at2.createTransformedShape(shape));
	}
	
}