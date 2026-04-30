# Results UI

De resultaten van een jaarsimulatie worden weergegeven via de **zero_results_UI**-module. Dit is een aparte AnyLogic-bibliotheek die in een project-main model wordt geïntegreerd.

De broncode van de results_ui is te vinden in de [zero_results_UI repository](https://github.com/Zenmo/zero_results_UI).

## Koppeling met het model

Er is **geen directe koppeling** tussen de `zero_engine` en de `results_ui`. Beide worden aangestuurd vanuit het project-main model:

```
Project-main model
├── zero_engine     (berekeningen)
└── zero_results_UI (visualisatie)
```

Na afloop van een jaarsimulatie haalt de results_ui de benodigde data op uit de agenten van de engine (GridConnections, GridNodes, EnergyModel) via door de engine gepubliceerde methoden en datasets.

## Dataformaat

De results_ui verwacht data in AnyLogic `Dataset`-objecten. De engine levert:

- **Jaardatasets** – 365 elementen (dagwaarden) voor energietotalen en financiële KPIs
- **Kwartierdata** – 35.040 elementen, alleen op aanvraag voor gedetailleerde analyses

Zie [Datasets](datasets.md) voor meer informatie over de dataopslag en conversie.

## Typen resultaten

De results_ui kan de volgende categorieën resultaten weergeven:

### Energiebalans

- Import en export per energiedrager [kWh/dag]
- Productie per productietype (PV, wind, WKK) [kWh/dag]
- Verbruik per verbruikstype (vast, EV, warmtepomp, etc.) [kWh/dag]
- Zelfverbruik en zelfvoorzieningsgraad [%]

### Vermogensprofielen

- Nettovermogen op de aansluiting per tijdstap [kW]
- Batterij laad-/ontlaadprofiel [kW]
- EV-laadprofiel [kW]
- PV-productie vs. verbruik [kW]

### Financiën

- Energiekosten en terugleverbaten [€/jaar]
- Nettarief en belastingen [€/jaar]
- Besparing door zelfverbruik [€/jaar]
- Vergelijking scenario's

### Netimpact

- Piekbelasting op transformatoren [kW]
- Congestie-uren per GridNode [h/jaar]
- Balans import/export per GridNode

### CO₂-emissies

- Scope 1 en Scope 2 emissies per energiedrager [kg CO₂/jaar]
- Emissiereductie ten opzichte van referentiescenario

## Sliders en interactie

Vanuit het project-main model kan de gebruiker via **sliders** het energiesysteem aanpassen:

- Instellen van opwekcapaciteit (bijv. kWp PV, kW wind)
- Aanpassen van opslagcapaciteit (kWh batterij)
- Wijzigen van EMS-strategie
- Aanpassen van tarievenstructuur

Na een aanpassing kan een nieuwe jaarsimulatie worden gestart en de resultaten worden direct vergeleken.

## Live-simulatie vs. jaarsimulatie

De results_ui ondersteunt twee simulatiemodi:

| Modus | Tijdresolutie | Dataset-grootte | Toepasssing |
|-------|--------------|-----------------|------------|
| Live-simulatie | 15 min (kwartier) | 672 elementen (1 week) | Realtime inzicht in dagelijks gedrag |
| Jaarsimulatie | Dag (aggregaat) | 365 elementen | Jaarlijkse KPI-analyse en scenariovergelijking |

Bij de live-simulatie worden grafieken real-time bijgewerkt via AnyLogic's ingebouwde tijdsfuncties. Bij de jaarsimulatie worden de grafieken eenmalig gevuld na afloop van de berekening.
