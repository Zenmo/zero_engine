double f_style(Color fillColor,Color lineColor,Double width,LineStyle lineStyle)
{/*ALCODESTART::1721720983063*/
if (fillColor == null) {
	fillColor = p_defaultFillColor;
}
gisRegion.setFillColor( fillColor );
if (lineColor == null) {
	lineColor = p_defaultLineColor;
}
gisRegion.setLineColor( lineColor );
if (width == null) {
	width = p_defaultLineWidth;
}
gisRegion.setLineWidth( width );
if (lineStyle == null) {
	lineStyle = p_defaultLineStyle;
}
gisRegion.setLineStyle( lineStyle );
/*ALCODEEND*/}

double f_writeStyleStrings()
{/*ALCODESTART::1753363771985*/
if (p_defaultFillColor!=null){
	p_defaultFillColorString = p_defaultFillColor.getRed() + "," + p_defaultFillColor.getGreen() + "," + p_defaultFillColor.getBlue();
}
if (p_defaultLineColor!=null){
	p_defaultLineColorString = p_defaultLineColor.getRed() + "," + p_defaultLineColor.getGreen() + "," + p_defaultLineColor.getBlue();
}
if (p_defaultLineStyle!=null){
	p_defaultLineStyleString = p_defaultLineStyle.toString();
}
/*ALCODEEND*/}

double f_resetStyle()
{/*ALCODESTART::1753364192780*/
String[] rgb;
if (p_defaultFillColorString!=null){
	rgb = p_defaultFillColorString.split(",");
	
	if (rgb.length == 3) {
	    p_defaultFillColor = new Color(
	        Integer.parseInt(rgb[0]),
	        Integer.parseInt(rgb[1]),
	        Integer.parseInt(rgb[2])
	    );
	} 
}

if (p_defaultLineColorString!=null){
	rgb = p_defaultLineColorString.split(",");
	if (rgb.length == 3) {
	    p_defaultLineColor = new Color(
	        Integer.parseInt(rgb[0]),
	        Integer.parseInt(rgb[1]),
	        Integer.parseInt(rgb[2])
	    );
	} 
} 
if (p_defaultLineStyleString!=null){
	p_defaultLineStyle = LineStyle.valueOf(LineStyle.class, p_defaultLineStyleString);
}
/*ALCODEEND*/}

