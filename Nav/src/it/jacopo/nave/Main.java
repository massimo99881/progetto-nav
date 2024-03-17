package it.jacopo.nave;

import javax.swing.JFrame;

public class Main {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setBounds(200, 200, 1200, 800);
		
		Panello pan = new Panello();
		pan.setBounds(0, 0, 1200, 800);
		frame.add(pan);
		pan.addKeyListener(pan);
		pan.addMouseMotionListener(pan);
		pan.setFocusable(true);
		
		frame.setVisible(true);
	}

}
