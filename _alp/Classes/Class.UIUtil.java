/**
 * UIUtil
 *
 * Static helpers for fitting a ShapeText inside a ShapeRectangle.
 *
 * Notes/assumptions:
 * - Shapes are assumed to be unrotated and in the same coordinate space
 *   (i.e. same parent group / level).
 * - The text shape's Y is treated as the top of the text block, matching
 *   AnyLogic's text shape anchor.
 */

import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

public class UIUtil {
	
	/**
     * Standalone render context for text measurement. Arguments:
     *   1. transform (null) - no scaling/rotation of the target surface; measure in plain user-space pixels (identity)
     *   2. isAntiAliased (true) - assume glyph edges are smoothed, matching how AnyLogic renders presentation text
     *   3. usesFractionalMetrics (true) - keep exact fractional glyph widths instead of rounding to whole pixels, so measured size scales linearly with font size
     */
    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    
    public static void fitTextInRectangleVertical(ShapeText textShape, ShapeRectangle rectShape, double topMargin_px, double bottomMargin_px) {
        fitTextInRectangle(textShape, rectShape, topMargin_px, bottomMargin_px, 0.0, 0.0);
    }

    public static void fitTextInRectangleHorizontal(ShapeText textShape, ShapeRectangle rectShape, double leftMargin_px, double rightMargin_px) {
        fitTextInRectangle(textShape, rectShape, 0.0, 0.0, leftMargin_px, rightMargin_px);
    }

    public static void fitTextInRectangle(ShapeText textShape, ShapeRectangle rectShape, double margin_px) {
        fitTextInRectangle(textShape, rectShape, margin_px, margin_px, margin_px, margin_px);
    }
    
    public static void fitTextInRectangle(ShapeText textShape, ShapeRectangle rectShape, double topMargin_px, double bottomMargin_px, double leftMargin_px, double rightMargin_px) {
    	/**
         * Scales the font of textShape (up or down) so the text block fits exactly
         * within rectShape minus the given margins, and centers the block inside
         * that margin box. The binding axis (width or height) touches its margins
         * exactly; the other axis gets the remaining space distributed equally.
         */
        if (textShape == null || rectShape == null || textShape.getText() == null || textShape.getText().isEmpty() || textShape.getFont() == null) {
            return;
        }

        double availableWidth  = rectShape.getWidth() - leftMargin_px - rightMargin_px;
        double availableHeight = rectShape.getHeight() - topMargin_px  - bottomMargin_px;
        if (availableWidth <= 0 || availableHeight <= 0) {
            return; // margins consume the whole rectangle
        }

        Font font = textShape.getFont();
        String[] lines = textShape.getText().split("\n", -1); // Limit -1 keeps trailing empty lines, so they still count for height

        double maxLineWidth = maxLineWidth(font, lines);
        double textHeight   = lines.length * lineHeight(font);
        if (maxLineWidth <= 0 || textHeight <= 0) {
            return; // text contains no measurable glyphs
        }

        // One scale factor for both axes; the smaller ratio is the binding axis
        double scale = Math.min(availableWidth / maxLineWidth, availableHeight / textHeight);
        float newSize = (float) (font.getSize2D() * scale);
        Font fittedFont = font.deriveFont(newSize);

        // Font metrics are not perfectly linear in size (hinting, rounding): step down until the fit is guaranteed
        while (maxLineWidth(fittedFont, lines) > availableWidth || lines.length * lineHeight(fittedFont) > availableHeight) {
            newSize = newSize - 0.5f;
            fittedFont = fittedFont.deriveFont(newSize);
        }
        textShape.setFont(fittedFont);

        // Center the fitted block inside the margin box
        double fittedWidth  = maxLineWidth(fittedFont, lines);
        double fittedHeight = lines.length * lineHeight(fittedFont);
        double blockLeft = rectShape.getX() + leftMargin_px + 0.5 * (availableWidth  - fittedWidth);
        double blockTop  = rectShape.getY() + topMargin_px  + 0.5 * (availableHeight - fittedHeight);

        textShape.setY(blockTop);
        textShape.setX(blockLeft + alignmentAnchorOffset(textShape, fittedWidth));
    }

    public static double maxLineWidth(Font font, String[] lines) {
    	/** Width of the widest line of text. */
        double max = 0.0;
        for (String line : lines) {
            if (line.isEmpty()) continue;
            Rectangle2D bounds = font.getStringBounds(line, FRC);
            if (bounds.getWidth() > max) {
                max = bounds.getWidth();
            }
        }
        return max;
    }

    public static double lineHeight(Font font) {
    	/** Height of a single line (ascent + descent + leading) for this font. */
        return font.getLineMetrics("Ag", FRC).getHeight();
    }

    public static double alignmentAnchorOffset(ShapeText textShape, double textWidth) {
		/**
	     * AnyLogic anchors a text shape's X at its alignment point: left edge for
	     * left-aligned, center for center-aligned, right edge for right-aligned.
	     */
        TextAlignment alignment = textShape.getAlignment();
        if (alignment == TextAlignment.ALIGNMENT_CENTER) return 0.5 * textWidth;
        if (alignment == TextAlignment.ALIGNMENT_RIGHT)  return textWidth;
        return 0.0; // ALIGNMENT_LEFT or null
    }

}