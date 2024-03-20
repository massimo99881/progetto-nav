package it.jacopo.nave;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;

public class Panello extends JPanel implements KeyListener, MouseMotionListener{
	Map<String, GameObject> obj = new HashMap<>();
	Update update;
	Area area1;
	int cx = 100, cy = 100;//thread update per drift astronave
	public Panello() {
		Nav nave = new Nav("navicella1");  //nave principale	
		Nav nave2 = new Nav("navicella2"); //nave fantoccio per test collisioni
		nave2.x = 300;
		nave2.y = 300;
		obj.put(nave.nome, nave);
		obj.put(nave2.nome, nave2);
		update = new Update(obj.get("navicella1"), this);
	}
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		for(Entry<String, GameObject> e : obj.entrySet()) { //disegno tutto quello che c'è nella mappa
           e.getValue().draw(g2d);
        } 
		area1 = new Area(obj.get("navicella1").getTransf()); //restituzione area nav1
		//Area area2 = new Area(obj.get("navicella2").getTransf()); //restituzione area nav2
		
		Shape circle = createCircle(cx, cy, 20);
		Area area2 = new Area(circle); // Area del cerchio
		area1.intersect(area2); //area1 diventa l'intersezione fra le 2
		
		
     // Se l'area di intersezione non è vuota, ferma la navicella
        if (!area1.isEmpty()) {
            obj.get("navicella1").speed = Double.MIN_VALUE; // Ferma la navicella
            area1.reset();
        }

        // Disegna l'area di intersezione per debugging
        g2d.fill(area1);
	}
	
	// Metodo per creare un cerchio
    private Shape createCircle(int x, int y, int r) {
        return new java.awt.geom.Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
    }
    
	@Override
	public void keyTyped(KeyEvent e) {}
	
	@Override
	public void keyPressed(KeyEvent e) {
	    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
	        
	    	if (!area1.isEmpty()) {
                obj.get("navicella1").speed = Double.MIN_VALUE;
            } else {
                obj.get("navicella1").speed += 50;
            }
        	repaint();

	        if (!update.isAlive() ) { // Controlla se il thread è già in esecuzione
	            update.start();
	            //ThLavora = true;
	        }
	    }
	}


	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseMoved(MouseEvent e) {	
		cx = e.getX();
		cy = e.getY();
		//calcolo in radianti dell'angolo della retta fra cursore e nav
		obj.get("navicella1").angolo = Math.atan2(((e.getY()) - (obj.get("navicella1").y)) , ((e.getX()) - (obj.get("navicella1").x))); 
		
		if((int)(obj.get("navicella1").speed) == 0) { //se la navicella è in movimento e muovo il cursore non faccio
			repaint();							//repaint perchè ci pensa già keyPressed
		}
		
	}
	
	
	
	
	
}
