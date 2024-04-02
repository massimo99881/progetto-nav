package it.jacopo.nave;

import java.awt.Image;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

public class AsteroideCache {

	private Image image;
	private Polygon polygon;
	
	public AsteroideCache(Image image) {
		super();
		this.image = image;
		this.polygon = getPolygonFromImage(Conf.toBufferedImage(this.image));
	}
	
	public Image getImage() {
		return image;
	}
	public Polygon getPolygon() {
		return polygon;
	}
	private Polygon getPolygonFromImage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        Polygon poly = new Polygon();

        // Scansione dei pixel per individuare quelli non trasparenti e aggiungere i loro vertici al poligono
        for (int y = 0; y < height; y+=2) {
            for (int x = 0; x < width; x+=2) {
                if ((img.getRGB(x, y) >> 24) != 0x00) { // Se il pixel non è trasparente
                    poly.addPoint(x, y);
                }
            }
        }
        return poly;
	}
}
