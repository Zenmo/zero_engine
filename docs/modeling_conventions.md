# Modeling conventions

## Objecten op het canvas

Alle objecten op het canvas dienen als eerste letters van hun naam een type-aanduiding te hebben, gevolgd door een underscore en een beschrijving van hun functie/inhoud.

| Prefix | Type | Voorbeeld |
|--------|------|-----------|
| `p_` | Parameter | `p_capacity_kWh` |
| `b_` | Boolean parameter of variabele | `b_hasExcess` |
| `v_` | Variabele | `v_power_kW` |
| `f_` | Functie | `f_calculateKPI()` |
| `gr_` | Group | `gr_controls` |
| `pl_` | Plot | `pl_energyBalance` |
| `ch_` | Chart | `ch_dailyLoad` |
| `e_` | Event | `e_hourlyUpdate` |
| `c_` | Collectie (lijst of set van agenten) | `c_assets` |

## Eenheden

Alle variabelen en functies die een fysieke grootheid representeren, moeten de eenheid opnemen aan het einde van de naam, tussen vierkante haken:

```
v_power_kW          → vermogen in kilowatt
v_energy_kWh        → energie in kilowattuur
v_time_h            → tijd in uren
p_capacity_kWh      → capaciteit in kilowattuur
getCapacityElectric_kW()  → functie die kW teruggeeft
```

**Veelgebruikte eenheden:**

| Suffix | Eenheid | Grootheid |
|--------|---------|-----------|
| `_kW` | kilowatt | Vermogen |
| `_kWh` | kilowattuur | Energie |
| `_h` | uur | Tijd |
| `_fr` | fractie [0–1] | Rendement, State of Charge |
| `_eurpMWh` | €/MWh | Energieprijs |
| `_eurpMWhpkW` | €/MWh per kW | Prijsgevoeligheid |
| `_degC` | graden Celsius | Temperatuur |
| `_nr` | aantal (dimensieloos) | Teller |

Het opnemen van eenheden in de naam voorkomt:
- Verwarring over schaalniveaus (kW vs MW vs GW)
- Rekenfouten door verkeerde eenheidconversies
- Onduidelijkheid over de betekenis van een getal

## Van vermogen [kW] naar energie [kWh]: zero-order-hold

Numerieke integratie van vermogens per tijdstap:

```
energie [kWh] = vermogen [kW] × tijdstap [h]
```

De 'solver' is het **zero-order-hold** principe: het vermogen wordt als constant beschouwd gedurende de gehele tijdstap. Dit is een eerste-orde benadering die nauwkeuriger wordt naarmate de tijdstap kleiner is.

De tijdstapgrootte is beschikbaar als parameter `p_timestep_h` (standaard 0,25 h = 15 minuten).

## Tekenconventie voor energiestromen

Het model hanteert de volgende tekenconventie voor vermogens:

| Teken | Betekenis |
|-------|-----------|
| Positief (+) | Verbruik (belasting op het net) |
| Negatief (−) | Productie (teruglevering aan het net) |

Dit geldt voor vermogensprofielen op GridConnection-niveau. Zo is:
- PV-productie: negatief (levert terug)
- EV-laden: positief (verbruikt van het net)
- Batterij ontladen: negatief
- Batterij opladen: positief

## Klassenamen in de Java-code

In de AnyLogic-klassen wordt de volgende naamgevingsconventie gehanteerd:

| Prefix | Type | Voorbeeld |
|--------|------|-----------|
| `J_` | Data/model klasse | `J_EA`, `J_TimeParameters` |
| `I_` | Interface | `I_EnergyManagement`, `I_BatteryManagement` |
| `GC` | GridConnection-agent | `GCHouse`, `GCIndustry` |
| `J_EA` | Energy asset klasse | `J_EAStorage`, `J_EAConversion` |
