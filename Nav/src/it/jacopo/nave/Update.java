package it.jacopo.nave;


public class Update extends Thread{
	GameObject nav;
	Panello pan;
	public Update(GameObject nav, Panello pan) {
		this.nav = nav;
		this.pan = pan;
	}
	@Override
	public void run() {
		while(nav.speed != 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pan.repaint();
		}
		pan.ThLavora = false;
	}
}
