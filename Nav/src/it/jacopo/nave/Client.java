package it.jacopo.nave;


import java.io.IOException;

import javax.swing.JFrame;

public class Client {
	
	public static void main(String[] args) throws IOException {
	    
	    int h = Conf.FRAME_HEIGHT;
	    int w = Conf.FRAME_WIDTH;
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setBounds(100, 20, w, h);
        Pannello pan = new Pannello(frame);
		pan.setBounds(0, 0, w, h);
		frame.add(pan);
		pan.addKeyListener(pan);
		pan.addMouseMotionListener(pan);
		pan.setFocusable(true);
		frame.setVisible(true);
	    pan.requestFocusInWindow();
	}
	
}
