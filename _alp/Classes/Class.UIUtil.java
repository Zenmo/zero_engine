/**
 * UIUtil
 */	

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class UIUtil {

    public static void decreaseTextFontSizeToFitRectangle(ShapeText textShape, ShapeRectangle rectShape, double margin_px, int defaultFontSize_pt) {
    	
		if (textShape == null || textShape.getText() == null || textShape.getText().isEmpty() || rectShape == null) {
		    return;
		}
		
		// Resetting of default fontsize to prevent overflow
		Font font = textShape.getFont();
		if (font != null) {
			Font defaultFont = font.deriveFont((float) defaultFontSize_pt);
			textShape.setFont(defaultFont);
		}
		
		double rectWidth = rectShape.getWidth();
		double rectHeight = rectShape.getHeight();
		double rectX = rectShape.getX();
		double rectY = rectShape.getY();
		double targetWidth = rectWidth - 2 * margin_px;
		
		
		String text = textShape.getText();
		Font currentFont = textShape.getFont();
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		try {
			FontMetrics fm = g2d.getFontMetrics(currentFont);
			
			// Find longest line of text (if ShapeText contains multiple sentences)
			String[] lines = text.split("\n");
			double maxLineWidth = 0;
			for (String line : lines) {
				double lineWidth = fm.stringWidth(line);
				if (lineWidth > maxLineWidth) {
					maxLineWidth = lineWidth;
				}
			}
			
			// Scale down the font if it exceeds the maximum target width
			if (maxLineWidth > targetWidth) {
				double scaleFactor = targetWidth / maxLineWidth;
				double newSizeDouble = defaultFontSize_pt * scaleFactor;
				float newSize = (float) Math.floor(newSizeDouble);
				
				Font scaledFont = currentFont.deriveFont(newSize);
				textShape.setFont(scaledFont);
				fm = g2d.getFontMetrics(scaledFont);
			}
			
			double textHeight = lines.length * fm.getHeight();
			double newY = rectY + 0.5*(rectHeight - textHeight);
			textShape.setY(newY);
			
		} finally {
			g2d.dispose();
		}
		
    }

}