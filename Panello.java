import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;

public class Panello extends JPanel implements KeyListener, MouseMotionListener{
	Map<String, GameObject> obj = new HashMap<>();
	Update update;
	int cx, cy;//thread update per drift astronave
	Boolean ThLavora = false;   //flag per vedere se il thread è gia attvio oppure no
	public Panello() {
		Nav nave = new Nav("ciao");  //nave principale	
		Nav nave2 = new Nav("ciao2"); //nave fantoccio per test collisioni
		Asteroide[] ast = new Asteroide[20];
		for(int i=0; i<20; i++) {
			ast[i] = new Asteroide("ast"+i);
			obj.put(ast[i].nome, ast[i]);
		}
		nave2.x = 300;
		nave2.y = 300;
		obj.put(nave.nome, nave);
		obj.put(nave2.nome, nave2);
		update = new Update(obj.get("ciao"), this);
		update.start();
	}
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		for(Entry<String, GameObject> e : obj.entrySet()) { //disegno tutto quello che c'è nella mappa
		   if(e.getValue().nome == "ast")
			   e.getValue().angolo += 0.1;
		   
		   g.drawLine(100, 100, 100, 100);
			
           e.getValue().draw(g2d);
        } 
		Area area1 = new Area(obj.get("ciao").getTransf()); //restituzione area nav1
		Area area2 = new Area(obj.get("ciao2").getTransf()); //restituzione area nav2
		area1.intersect(area2); //area1 diventa l'intersezione fra le 2
		g2d.fill(area1);
	}
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SPACE) { //quando premo spazio aumento la velocità
			if(cx-obj.get("ciao").x < 20 && cx-obj.get("ciao").x > -20 && cy-obj.get("ciao").y < 20 && cy-obj.get("ciao").y > -20) { //se il cursore è vicino ferma 
				obj.get("ciao").speed = Double.MIN_VALUE;
				
			}else {
				obj.get("ciao").speed += 10;
			}
			
			
		}
		if(!ThLavora) {  //se il thread è in funzione non lo eseguo senno esplode tutto
			update.start();
			ThLavora = true; //se lo faccio partire lo setto in lavoro
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
		obj.get("ciao").angolo = Math.atan2(((e.getY()) - (obj.get("ciao").y)) , ((e.getX()) - (obj.get("ciao").x))); 
		obj.get("ciao").angolo2 = Math.atan2(((e.getY()) - (obj.get("ciao").y)) , ((e.getX()) - (obj.get("ciao").x)));
		
		
	}
	
	
	
	
	
}
