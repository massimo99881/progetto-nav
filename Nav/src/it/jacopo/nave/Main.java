package it.jacopo.nave;


import javax.swing.JFrame;

public class Main {

	

	public static void main(String[] args) {
		// Precarica le immagini degli asteroidi
	    Asteroide.precaricaImmagini();
		
		JFrame frame = new JFrame();
		
		int FRAME_WIDTH = 1200;
		int FRAME_HEIGHT = 800;
		
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setBounds(200, 200, FRAME_WIDTH, FRAME_HEIGHT);
		
		Pannello pan = new Pannello();
		pan.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
		frame.add(pan);
		pan.addKeyListener(pan);
		pan.addMouseMotionListener(pan);
		pan.setFocusable(true);
		
		
		frame.setVisible(true);
		
		 // Ora che il frame e il pannello sono visibili, richiedi il focus.
	    pan.requestFocusInWindow();
	}

}
