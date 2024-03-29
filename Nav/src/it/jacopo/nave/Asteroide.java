package it.jacopo.nave;

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

    public Asteroide(String nome, String imagePath) {
    	this.name=nome;
    	
    	BufferedImage originalImage = null;
    	int newWidth = 0;
    	int newHeight = 0;
    	 // Caricamento dell'immagine
    	try {
            originalImage = ImageIO.read(new File(imagePath));
            // Calcola le nuove dimensioni dell'immagine riducendola 
            newWidth = (int) (originalImage.getWidth(null) * 0.2); // 80% dell'originale
            newHeight = (int) (originalImage.getHeight(null) * 0.2); // 80% dell'originale
            
            // Ridimensiona l'immagine
            this.image = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
        } catch (IOException e) {
            e.printStackTrace();
            this.image = null; // Gestisci l'errore impostando l'immagine a null
        }
    	
        x = 0;
        y = 0;
        this.speed = 2.0 + Math.random() * 3.0; // Velocità casuale da 2.0 a 5.0

        shape = getPolygonFromImage(toBufferedImage(this.image));

        // Imposta una velocità di rotazione casuale
        angoloRotazione = (Math.random() * 2 - 1) * Math.PI / 180;
        if (Math.random() > 0.5) {
            angoloRotazione *= -1; // Cambia il verso della rotazione
        }
        
    }
    
    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Crea un buffered image con trasparenza
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Disegna l'immagine su buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        
        bGr.dispose();

        // Restituisce il buffered image
        return bimage;
    }
    
    private Polygon getPolygonFromImage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        Polygon poly = new Polygon();

        // Scansione dei pixel per individuare quelli non trasparenti e aggiungere i loro vertici al poligono
        for (int y = 0; y < height; y+=2) {
            for (int x = 0; x < width; x+=2) {
                if ((img.getRGB(x, y) >> 24) != 0x00) { // Se il pixel non è trasparente
                    poly.addPoint(x, y);
                }
            }
        }
        return poly;
	}

    @Override
    void draw(Graphics2D g) {
        if (this.image != null /* && !gameStopped */) {
            AffineTransform at = new AffineTransform();
            int imageWidth = this.image.getWidth(null);
            int imageHeight = this.image.getHeight(null);
            int imageCenterX = imageWidth / 2;
            int imageCenterY = imageHeight / 2;

            at.translate(x + imageCenterX, y + imageCenterY);
            at.rotate(angoloRotazione, 0, 0);
            at.translate(-imageCenterX, -imageCenterY);

            g.drawImage(this.image, at, null);
            
         // Now transform the polygon in the same way as the image
//            Shape transformedShape = at.createTransformedShape(shape);
//            g.setStroke(new BasicStroke(3));
//            g.setColor(Color.RED);
//            g.draw(transformedShape);

            // Per il contorno, se necessario, applicare lo stesso principio di trasformazione
        } else {
            super.draw(g);
        }
    }


    @Override
    void updateMovement() {
        final double ACCELERATION_CHANGE = 0.05; // Quanto la velocità dell'asteroide può cambiare ad ogni frame
        final double ANGLE_CHANGE = Math.PI / 90; // Cambiamento massimo dell'angolo in radianti ad ogni frame
        final double MIN_SPEED = 0.5; // Velocità minima dell'asteroide
        final double MAX_SPEED = 2.0; // Velocità massima dell'asteroide

        // Aggiungi una piccola variazione casuale alla velocità
        speed += (Math.random() - 0.5) * ACCELERATION_CHANGE; 
        // Assicurati che la velocità rimanga nei limiti
        speed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed));

        // Aggiungi una piccola variazione casuale all'angolo per creare una traiettoria non lineare
        angolo += (Math.random() - 0.5) * ANGLE_CHANGE; 

        // Calcola il nuovo movimento basato sull'angolo aggiornato
        // Si assume qui che angolo sia l'angolo di direzione dell'asteroide rispetto all'orizzontale,
        // che determina la sua traiettoria da sinistra verso destra.
        x += (int) (speed * Math.cos(angolo));
        y += (int) (speed * Math.sin(angolo));

        // Aggiornamento della rotazione dell'asteroide, se desiderato
        // Per esempio, puoi far ruotare l'asteroide attorno al suo asse più lentamente o più velocemente
        angoloRotazione += Math.PI / 180; // Qui come esempio, l'asteroide ruota di 1 grado per frame
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

