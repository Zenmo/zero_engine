double f_setNodeVisual()
{/*ALCODESTART::1658753874720*/
t_nodeID.setText(p_nodeID);
t_nodetype.setText(p_nodeType);
/*ALCODEEND*/}

double f_onClick()
{/*ALCODESTART::1670933638545*/
group.setY(450 - this.getY());
group.setX(1120 - this.getX());

for(UI_GridConnection g : kpiVisuals.uI_GridConnections){
	g.pl_powerFlows.setVisible(false);
	g.t_plotname.setVisible(false);
	g.t_assets.setVisible(false);
}
for(UI_GridNode n : kpiVisuals.uI_GridNodes){
	n.group.setVisible(false);
	group.setVisible(true);
}
/*ALCODEEND*/}

