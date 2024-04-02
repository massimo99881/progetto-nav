package it.jacopo.nave;


import javax.swing.JFrame;

public class Main {

	

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setBounds(200, 200, Conf.FRAME_WIDTH, Conf.FRAME_HEIGHT);
		
		Pannello pan = new Pannello();
		pan.setBounds(0, 0, Conf.FRAME_WIDTH, Conf.FRAME_HEIGHT);
		frame.add(pan);
		pan.addKeyListener(pan);
		pan.addMouseMotionListener(pan);
		pan.setFocusable(true);
		
		frame.setVisible(true);
		
		 // Ora che il frame e il pannello sono visibili, richiedi il focus.
	    pan.requestFocusInWindow();
	}

}
