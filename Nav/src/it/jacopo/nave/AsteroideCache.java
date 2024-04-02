package it.jacopo.nave;

import java.awt.Image;
import java.awt.Polygon;

public class AsteroideCache {

	private Image image;
	private Polygon polygon;
	
	public AsteroideCache(Image image) {
		super();
		this.image = image;
		this.polygon = Conf.getPolygonFromImage(Conf.toBufferedImage(this.image));
	}
	
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
		this.polygon = Conf.getPolygonFromImage(Conf.toBufferedImage(this.image));
	}
	public Polygon getPolygon() {
		return polygon;
	}
	 
}
