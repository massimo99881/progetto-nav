package it.jacopo.nave;


public class Update extends Thread {
    GameObject nav;
    Panello pan;

    public Update(GameObject nav, Panello pan) {
        this.nav = nav;
        this.pan = pan;
    }

    @Override
    public void run() {
        while (nav.speed != 0 && !pan.gameStopped) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pan.repaint();
        }
    }
}

