/**
 * J_AssetTypeForecast
 */	
public record J_AssetTypeForecast(
	    Class<? extends I_AssetManagement> assetType,
	    Map<OL_EnergyCarriers, Double[]> load_kW,
	    OL_ForecastStatus status,
	    String reason // Optional field to give an explanation for the status
) {}