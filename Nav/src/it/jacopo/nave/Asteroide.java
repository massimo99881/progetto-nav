package it.jacopo.nave;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Random;

public class Asteroide extends GameObject {
    Image image;
    double angoloRotazione; // Angolo di rotazione per la rotazione casuale
    final double ANGLE_BASE ;
    private int colpiSubiti = 0;
    float opacita = 1.0f; // Opacità iniziale al 100%
    
 // Metodo per gestire l'essere colpiti da un proiettile
    public void colpito() {
    	colpiSubiti++;
        // Gestisci il colpo riducendo l'opacità
    	if(opacita>=0) {
    		opacita -= 0.1; // Riduci del 20% per ogni colpo
    	}
    	
        
    	if (colpiSubiti >= 5) {
            // L'asteroide è completamente "dissolto", gestisci la rimozione
            System.out.println(name + " è stato distrutto");
            
        }
         
    }
    
    public int getColpiSubiti() {
        return colpiSubiti;
    }

    public Asteroide(String nome, String imagePath) {
    	this.name=nome;
    	
    	BufferedImage originalImage = null;
    	int newWidth = 0;
    	int newHeight = 0;
    	 // Caricamento dell'immagine
    	Random rand = new Random();
    	double scaleFactor = 0.2;
    	if(!imagePath.contains("asteroide1.png")) {
    		scaleFactor = 0.2 + (0.45 - 0.2) * rand.nextDouble(); // Genera un numero casuale tra 0.2 e 0.35
    	}
    	try {
            originalImage = ImageIO.read(new File(imagePath));
            // Calcola le nuove dimensioni dell'immagine riducendola 
            newWidth = (int) (originalImage.getWidth(null) * scaleFactor); // 80% dell'originale
            newHeight = (int) (originalImage.getHeight(null) * scaleFactor); // 80% dell'originale
            
            // Ridimensiona l'immagine
            this.image = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
        } catch (IOException e) {
            e.printStackTrace();
            this.image = null; // Gestisci l'errore impostando l'immagine a null
        }
    	ANGLE_BASE = (Math.random() - 0.5) * 2; // angolo traiettoria
         
        this.speed = 2.0 + Math.random() * 4.0; // Velocità casuale da 2.0 a 5.0

        shape = Conf.getPolygonFromImage(Conf.toBufferedImage(this.image));

        // Imposta una velocità di rotazione casuale
        angoloRotazione = (Math.random() * 2 - 1) * Math.PI / 180;
        if (Math.random() > 0.5) {
            angoloRotazione *= -1; // Cambia il verso della rotazione
        }
        
    }

    @Override
    void draw(Graphics2D g) {
        if (this.image != null /* && !gameStopped */) {
        	float safeOpacity = Math.max(0, Math.min(opacita, 1.0f));
        	g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, safeOpacity));
            AffineTransform at = new AffineTransform();
            int imageWidth = this.image.getWidth(null);
            int imageHeight = this.image.getHeight(null);
            int imageCenterX = imageWidth / 2;
            int imageCenterY = imageHeight / 2;

            at.translate(x + imageCenterX, y + imageCenterY);
            at.rotate(angoloRotazione, 0, 0);
            at.translate(-imageCenterX, -imageCenterY);

            g.drawImage(this.image, at, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            
        } else {
            super.draw(g);
        }
    }


    @Override
    void updateMovement() {
        

        // Aggiorna la velocità con una variazione casuale
        speed += (Math.random() - 0.5) * Conf.ACCELERATION_CHANGE;
        speed = Math.max(Conf.MIN_SPEED, Math.min(Conf.MAX_SPEED, speed));

        // Applica una variazione angolare casuale entro un range più ampio per una curvatura maggiore della traiettoria
        double angleVariation = (Math.random() - 0.5) * 2 * Conf.MAX_ANGLE_VARIATION;
        angolo = ANGLE_BASE + angleVariation;

        // Aggiorna la posizione dell'asteroide basandosi sulla sua velocità e angolo aggiornati
        x -= (int) (speed * Math.cos(angolo)); // Questo sposterà l'asteroide verso sinistra
        y += (int) (speed * Math.sin(angolo));

        // Opcionalmente, aggiorna la rotazione dell'asteroide per effetti visivi
        angoloRotazione += Math.PI / 180; // Aggiusta a piacimento
    }

    
    @Override
    Shape getTransf() {
        AffineTransform at = new AffineTransform();

        // Calcola il centro dell'immagine per la rotazione
        int imageWidth = this.image.getWidth(null);
        int imageHeight = this.image.getHeight(null);
        int imageCenterX = imageWidth / 2;
        int imageCenterY = imageHeight / 2;

        // Prima trasla al centro dello schermo (o alla posizione desiderata)
        at.translate(x + imageCenterX, y + imageCenterY);

        // Poi ruota attorno al centro dell'immagine
        at.rotate(angoloRotazione, 0, 0);

        // Trasla indietro in modo che l'angolo in alto a sinistra sia nella posizione corretta
        at.translate(-imageCenterX, -imageCenterY);

        // Applica la trasformazione alla forma dell'asteroide
        return at.createTransformedShape(shape);
    }

}

