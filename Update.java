

public class Update extends Thread{
	GameObject nav;
	Panello pan;
	public Update(GameObject nav, Panello pan) {
		this.nav = nav;
		this.pan = pan;
	}
	@Override
	public void run() {
		while(true) {
			if(pan.cx-pan.obj.get("ciao").x < 20 && pan.cx-pan.obj.get("ciao").x > -20 && pan.cy-pan.obj.get("ciao").y < 20 && pan.cy-pan.obj.get("ciao").y > -20) { //se il cursore è vicino ferma 
				pan.obj.get("ciao").speed = Double.MIN_VALUE;
			}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pan.repaint();
			
		}
		//pan.ThLavora = false;
	}
}
