package it.jacopo.nave;


import javax.swing.JFrame;

public class Main {

	

	public static void main(String[] args) {
		// Precarica le immagini degli asteroidi
	    Asteroide.precaricaImmagini();
	    int h = Conf.FRAME_HEIGHT;
	    int w = Conf.FRAME_WIDTH;
		
		JFrame frame = new JFrame();
		
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setBounds(200, 200, w, h);
		
		ProiettilePool proiettilePool = ProiettilePool.getInstance();
        Pannello pan = new Pannello(proiettilePool);
		pan.setBounds(0, 0, w, h);
		frame.add(pan);
		pan.addKeyListener(pan);
		pan.addMouseMotionListener(pan);
		pan.setFocusable(true);
		
		
		frame.setVisible(true);
		
		 // Ora che il frame e il pannello sono visibili, richiedi il focus.
	    pan.requestFocusInWindow();
	    
	    
	}

}
