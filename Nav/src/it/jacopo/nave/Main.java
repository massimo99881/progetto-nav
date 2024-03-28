package it.jacopo.nave;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Toolkit;

public class Main {

    // Definisci le dimensioni della finestra come costanti statiche
    public static final int LARGHEZZA_FINESTRA = 1200;
    public static final int ALTEZZA_FINESTRA = 700;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Ottieni le dimensioni dello schermo
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int larghezzaSchermo = screenSize.width;
        int altezzaSchermo = screenSize.height;

        // Calcola la posizione x e y per centrare la finestra
        int x = (larghezzaSchermo - LARGHEZZA_FINESTRA) / 2;
        int y = (altezzaSchermo - ALTEZZA_FINESTRA) / 2;

        // Imposta le dimensioni e la posizione della finestra
        frame.setBounds(x, y, LARGHEZZA_FINESTRA, ALTEZZA_FINESTRA);

        Panello pan = new Panello();
        pan.setPreferredSize(new Dimension(LARGHEZZA_FINESTRA, ALTEZZA_FINESTRA));
        frame.add(pan);
        pan.addKeyListener(pan);
        pan.addMouseMotionListener(pan);
        pan.setFocusable(true);

        frame.pack(); // Adegua la dimensione del frame al contenuto
        frame.setVisible(true);
    }
}
