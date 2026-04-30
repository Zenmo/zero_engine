# Energy Assets

Energy assets zijn de fysieke installaties die aan een GridConnection gekoppeld zijn. Ze bepalen het lokale energieverbruik, de productie, de opslag en de conversie van energie. Alle energy assets erven van de abstracte basisklasse `J_EA`.

## Basisklasse: J_EA

De abstracte klasse `J_EA` definieert de gemeenschappelijke interface voor alle energy assets:

- `getCapacityElectric_kW()` – Elektrisch vermogen van de installatie
- `getCapacityHeat_kW()` – Thermisch vermogen
- `getCapacityGas_kW()` – Gasvermogen
- Tijdreeksen voor vermogen en energie per tijdstap

Elke asset heeft een koppeling naar zijn `ConnectionOwner` (de eigenaar van de aansluiting) en optioneel naar een `EnergySupplier` of `EnergyCoop`.

---

## Vast verbruik: J_EAFixed

Een `J_EAFixed`-asset heeft een niet-stuurbaar verbruiksprofiel. Voorbeelden:

- Huishoudelijk elektriciteitsverbruik (zonder sturing)
- Industriële basisbelasting

De asset leest een tijdreeks in (`J_EAProfile`) die het vermogen per tijdstap aangeeft. Dit profiel is niet beïnvloedbaar door het EMS.

---

## Flexibel verbruik en productie: J_EAFlex

Een `J_EAFlex`-asset kan in de tijd worden verplaatst of gereduceerd. Subtypes:

### J_EAEV – Elektrisch voertuig

Modelleert een elektrisch voertuig dat aan een laadpunt staat. Kenmerken:

- Laadvermogen (maximaal en minimaal)
- Beschikbare laadtijden (op basis van rijgedrag uit `J_ActivityTrackerTrips`)
- Energiebehoefte (te laden kWh per dag/sessie)
- Sturing via `I_ChargingManagement`

Beschikbare sturingen (`I_ChargingManagement`):

| Implementatie | Beschrijving |
|---------------|-------------|
| Simple | Laden zodra aangesloten, maximaal vermogen |
| OffPeak | Laden buiten piekuren |
| Price | Laden op goedkoopste momenten |
| GridBalancing | Laden op basis van netbelasting |
| LocalBalancing | Laden op basis van lokale productie (zelfverbruik) |
| MaxAvailablePower | Laden met beschikbaar vermogen op de aansluiting |
| ExternalSetpoint | Laden op basis van extern setpoint |
| Scheduled | Gepland laden via `FlexAssetScheduler` |

### J_EAChargingSession – Laadsessie

Vertegenwoordigt één individuele laadsessie van een EV. Bevat:

- Aankomsttijd, vertrektijd
- Energiebehoefte
- Laadvermogenslimiet

### Flexibel industrieel verbruik

Industriële processen die in de tijd verschoven kunnen worden, zoals:

- Warmtepompen in warmtemodus
- Elektrolyseurs
- Productieprocessen met flexibele planning

Sturing via `FlexAssetScheduler` (greedy algoritme op basis van marginale prijs).

---

## Energieproductie: J_EAProduction

Productie-assets leveren elektriciteit, warmte of gas aan de GridConnection.

### J_EAProfile – Profielgestuurde productie

Productie op basis van een vaste tijdreeks. Toepassingen:

- Zonnepanelen (PV) – vermogensprofiel per tijdstap
- Windturbines – vermogensprofiel per tijdstap
- Vaste WKK-productie

### Gestuurde productie

Productie die (deels) sturing accepteert, bijv. afschakeling of curtailment:

- Sturing via `I_CurtailManagement`

| Implementatie | Beschrijving |
|---------------|-------------|
| Price | Curtailment op basis van prijs |
| ContractCapacity | Curtailment bij overschrijding van contractcapaciteit |
| NodalPricing | Curtailment op basis van knooppuntprijzen |
| ExternalSetpoint | Curtailment via extern setpoint |

---

## Energieopslag: J_EAStorage

Opslagassets kunnen energie bufferen en later terug leveren.

### J_EAStorageElectric – Elektrische batterij

Modellering via `BatteryAsset` (zie `zeroPackage/BatteryAsset.java`). Parameters:

| Parameter | Eenheid | Beschrijving |
|-----------|---------|-------------|
| `maxPower_kW` | kW | Maximaal laad-/ontlaadvermogen |
| `storageCapacity_kWh` | kWh | Maximale opslagcapaciteit |
| `initialSOC_fr` | fractie [0–1] | Beginvulling als fractie van capaciteit |
| `etaCharge_fr` | fractie | Laadrendement |
| `etaDischarge_fr` | fractie | Ontlaadrendement |

Sturing via `I_BatteryManagement`:

| Implementatie | Beschrijving |
|---------------|-------------|
| Off | Batterij niet actief |
| SelfConsumption | Lokaal zelfverbruik maximaliseren |
| Price | Arbitrage op marginale prijs |
| PeakShaving | Pieken op de aansluiting afvlakken |
| LocalBalancing | Balanceren van lokale vraag/aanbod |
| ExternalSetpoint | Sturing via extern setpoint |
| ProfileBased | Laden/ontladen op basis van een vast profiel |

Scheduling-algoritme: zie `zeroPackage/BatteryScheduler.java` – iteratief algoritme dat winstgevende laad/ontlaadcycli zoekt op basis van het prijsverschil tussen momenten.

### J_EAStorageHeat – Thermische buffer

Warmteopslag (bijv. boilervat, warmtebuffer). Parameters:

- Maximale capaciteit [kWh]
- Laad-/ontlaadvermogen [kW]
- Warmteverliezen [kW/K of fractie]

### J_EAStorageGas – Gasbuffer

Gasopslag voor waterstof of aardgas. Werkt analoog aan de thermische buffer.

---

## Energieconversie: J_EAConversion

Conversie-assets zetten de ene energievorm om in de andere. Subtypes:

| Klasse | Invoer | Uitvoer | Beschrijving |
|--------|--------|---------|-------------|
| `J_EAConversionHeatPump` | Elektriciteit | Warmte | Warmtepomp (lucht/water of bodem) |
| `J_EAConversionElectricHeater` | Elektriciteit | Warmte | Elektrische boiler |
| `J_EAConversionGasCHP` | Gas | Elektriciteit + Warmte | WKK op aardgas |
| `J_EAConversionFuelCell` | Waterstof | Elektriciteit + Warmte | Brandstofcel |
| `J_EAConversionElectrolyzer` | Elektriciteit | Waterstof | Elektrolyseur |
| `J_EAConversionGasBoiler` | Gas | Warmte | Gasketel |
| `J_EAConversionHeatExchanger` | Warmte | Warmte | Warmtewisselaar (bijv. stadsverwarming) |

Sturing van warmtevraag via `I_HeatingManagement`:

| Implementatie | Beschrijving |
|---------------|-------------|
| Simple | Warmte op aanvraag, maximaal vermogen |
| Profile | Warmtelevering op basis van profiel |
| PIControl | PID-regelaar op kamertemperatuur |
| HybridHeatPump | Hybride warmtepomp met gasketel als backup |
| DistrictHeating | Aansluiting op warmtenet |
| Neighborhood | Gedeelde warmtelevering op buurtniveau |

---

## Energiedragers

De engine ondersteunt de volgende energiedragers (gedefinieerd in `J_FlowsMap`):

| Drager | Beschrijving |
|--------|-------------|
| Electricity | Elektriciteit |
| Gas | Aardgas of biogas |
| Heat | Thermische energie |
| Hydrogen | Waterstof |

Per GridConnection wordt per tijdstap voor elke drager het vermogen (kW) bijgehouden. De energiebalans moet sluiten voor alle dragers afzonderlijk.

---

## Overzicht asset-stromen: J_AssetFlows

De klasse `J_AssetFlows` houdt per GridConnection de bijdrage van elke asset bij aan de totale energiestroom:

- Vast verbruik (`fixed_kW`)
- Warmtepompverbruik (`heatPump_kW`)
- EV-laadverbruik (`evCharging_kW`)
- Batterijvermogen (`battery_kW`)
- PV-productie (`pv_kW`)
- Windproductie (`wind_kW`)
- WKK-productie (`chp_kW`)
- Stadsverwarming (`districtHeat_kW`)
- Vehicle-to-Grid (`v2g_kW`)

Dit maakt het mogelijk om na de simulatie gedetailleerde analyse te doen per assettype.
