package it.jacopo.nave;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Asteroide extends GameObject {
     
    Image image;
    
    // Impostazioni per la variazione dell'angolo di movimento
    final double ANGLE_CHANGE = Math.PI / 220; 

    // Impostazioni per la velocità
    final double MIN_SPEED = 1.5; // Velocità minima
    final double MAX_SPEED = 2.5; // Velocità massima per mantenere il movimento gestibile
    
    // Impostazione per la variazione angolare iniziale dell'asteroide
    final double ANGLE_VARIATION = Math.PI / 2; // Massima deviazione di 8 gradi dall'orizzontale

    double angoloRotazione; // Angolo di rotazione attuale dell'asteroide
    double velocitaRotazione; // Velocità di rotazione (include la direzione)


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

        angolo = (Math.random() - 0.5) * ANGLE_VARIATION; 
        
     // Imposta una velocità di rotazione iniziale casuale
        velocitaRotazione = (Math.random() * 0.04) - 0.02; // Ad esempio, tra -0.02 e +0.02 radianti per frame
        
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
        if (this.image != null) {
            int imageWidth = this.image.getWidth(null);
            int imageHeight = this.image.getHeight(null);

            // Crea un nuovo AffineTransform
            AffineTransform at = new AffineTransform();

            // Step 1: Traslazione per centrare l'immagine nella sua posizione finale
            at.translate(x + imageWidth / 2, y + imageHeight / 2);

            // Step 2: Rotazione intorno al centro dell'immagine
            at.rotate(angoloRotazione, 0, 0);

            // Step 3: Traslazione indietro per allineare il centro dell'immagine con il punto di rotazione
            at.translate(-imageWidth / 2, -imageHeight / 2);

            // Disegna l'immagine dell'asteroide con la trasformazione applicata
            g.drawImage(this.image, at, null);
            
         // DEBUG ONLY:
            // Set the stroke and color for the polygon outline
            //g.setStroke(new BasicStroke(3));
            //g.setColor(Color.RED);
            // Now transform the polygon in the same way as the image
            Shape transformedShape = at.createTransformedShape(shape);
            // Draw the outline of the polygon
            g.draw(transformedShape);
        } else {
            // Se l'immagine non è stata caricata, disegna un placeholder
            super.draw(g);
        }
    }



    @Override
    void updateMovement() {
        
        // Ensure that the speed stays within bounds
        speed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed));
         
        // Update the position based on the new speed and direction
        x += (int) (speed * Math.cos(angolo));
        y += (int) (speed * Math.sin(angolo));
        // Aggiorna l'angolo di rotazione per la rotazione dell'asteroide
        angoloRotazione += velocitaRotazione;

        // Assicurati che l'angolo di rotazione resti in un intervallo gestibile
        if(angoloRotazione > 2 * Math.PI) {
            angoloRotazione -= 2 * Math.PI;
        } else if(angoloRotazione < 0) {
            angoloRotazione += 2 * Math.PI;
        }
        
        // Update the Polygon shape here
        if (image != null) {
            BufferedImage bufferedImage = toBufferedImage(image);
            shape = getPolygonFromImage(bufferedImage);
        }
    }

    public void resetPosizione() {
    }


}

