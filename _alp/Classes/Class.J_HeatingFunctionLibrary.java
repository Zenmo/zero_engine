/**
 * J_HeatingFunctionLibrary
 */	
public abstract class J_HeatingFunctionLibrary {

	public static double  managePTAndHotWaterHeatBuffer(J_EAStorageHeat hotWaterBuffer, List<J_EAProduction> ptAssets, double hotWaterDemand_kW){
    	//Calculate the pt production
    	double ptProduction_kW = 0;
    	for (J_EA j_ea : ptAssets) {
    		ptProduction_kW -= j_ea.getLastFlows().get(OL_EnergyCarriers.HEAT);
    	}
    	
    	//Calculate the remaining hot water energy need after pt production, also calculate the remaining unused pt production
    	double remainingHotWater_kW = max(0, hotWaterDemand_kW - ptProduction_kW); // Need to do this, because pt has already compensated the hot water demand in the gc flows, so just need to update this value
    	double remainingPTProduction_kW = max(0, ptProduction_kW - hotWaterDemand_kW);
    	
    	if(hotWaterBuffer != null){
    		double chargeSetpoint_kW = 0;
    		if(remainingHotWater_kW > 0) {
    			chargeSetpoint_kW = -remainingHotWater_kW;
    		}
    		else if(remainingPTProduction_kW > 0) {
    			chargeSetpoint_kW = remainingPTProduction_kW;
    		}
    		hotWaterBuffer.v_powerFraction_fr = chargeSetpoint_kW / hotWaterBuffer.getCapacityHeat_kW();
    		hotWaterBuffer.f_updateAllFlows(hotWaterBuffer.v_powerFraction_fr);
    		
			double heatBufferCharge_kW = hotWaterBuffer.getLastFlows().get(OL_EnergyCarriers.HEAT);
			
    		if(remainingHotWater_kW > 0){//Only if the current pt production, wasnt enough, adjust the hotwater demand with the buffer, cause then the buffer will have tried to discharge
    			remainingHotWater_kW = max(0, remainingHotWater_kW + heatBufferCharge_kW);
    		}
    		else {//Curtail the remaining pt that is not used for hot water
    			remainingPTProduction_kW = max(0, remainingPTProduction_kW - heatBufferCharge_kW);
    		}
    	}
    	
    	if (remainingPTProduction_kW > 0) {//Heat (for now always curtail over produced heat!)
    		for (J_EAProduction j_ea : ptAssets) {
    			remainingPTProduction_kW -= j_ea.curtailEnergyCarrierProduction( OL_EnergyCarriers.HEAT, remainingPTProduction_kW);
    			
    			if (remainingPTProduction_kW <= 0) {
    				break;
    			}
    		}
    	}
    	return remainingHotWater_kW;
    }
	
	public static double  manageHotWaterHeatBuffer(J_EAStorageHeat hotWaterBuffer, double hotWaterDemand_kW, double availableHeatingPower_kWth, double timeStep_h){
		if(hotWaterDemand_kW > availableHeatingPower_kWth + hotWaterBuffer.getCurrentStateOfCharge_kWh() / timeStep_h) {
			throw new RuntimeException("Hot water demand is higher than available power.");
		}
		
		//Heating asset should always try to fill the heat buffer as fast as possible.
		double hotWaterDemandFromHeatingAsset_kW = min(availableHeatingPower_kWth, hotWaterDemand_kW + (hotWaterBuffer.getStorageCapacity_kWh() - hotWaterBuffer.getCurrentStateOfCharge_kWh()));
		double heatIntoBuffer_kW =  hotWaterDemandFromHeatingAsset_kW - hotWaterDemand_kW;
				

		hotWaterBuffer.v_powerFraction_fr = heatIntoBuffer_kW / hotWaterBuffer.getCapacityHeat_kW();
		hotWaterBuffer.f_updateAllFlows(hotWaterBuffer.v_powerFraction_fr);

		
    	return hotWaterDemandFromHeatingAsset_kW;
    }
}
 
 
 
 
 
 
 
 
 
 