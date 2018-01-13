package io.github.hashbox;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MyUtils {
	public static BufferedImage scaleImage(BufferedImage img, int width, int height) {
		int imgWidth = img.getWidth();
		int imgHeight = img.getHeight();
		if (imgWidth * height < imgHeight * width) {
			width = imgWidth * height / imgHeight;
		} else {
			height = imgHeight * width / imgWidth;
		}
		BufferedImage newImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = newImage.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.clearRect(0, 0, width, height);
			g.drawImage(img, 0, 0, width, height, null);
		} finally {
			g.dispose();
		}
		return newImage;
	}
}
