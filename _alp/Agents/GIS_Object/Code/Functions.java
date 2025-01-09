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

