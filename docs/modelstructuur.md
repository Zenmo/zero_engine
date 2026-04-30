# Modelstructuur

De Zenmo Zero Engine is opgebouwd als een multi-agent simulatiemodel in AnyLogic. Onderstaande beschrijving geeft een overzicht van de agenttypen, hun verantwoordelijkheden en de manier waarop ze samenwerken.

## Agënthiërarchie

```
EnergyModel  (hoofdorchestrator)
├── GridOperator
│   └── GridNode (netwerkknooppunten: elektriciteit, warmte)
│       └── GridConnection (aansluitingen op het net)
│           └── Energy Assets (installaties per aansluiting)
├── NationalEnergyMarket
├── EnergyCoop
├── EnergySupplier
└── ConnectionOwner
```

## EnergyModel

De `EnergyModel`-agent is de centrale orchestrator van de simulatie. Hij:

- Beheert alle agentpopulaties (GridConnections, GridNodes, etc.)
- Start en coördineert de jaarsimulatie (`f_runYearSimulation()`)
- Roept na afloop de KPI-berekeningen aan (`f_calculateKPI()`)
- Slaat globale parameters op zoals tijdstap (`p_timestep_h`) en simulatiejaar

## GridOperator

De `GridOperator`-agent vertegenwoordigt de netbeheerder. Hij:

- Beheert nettarieven via de `I_GridOperatorTariffs`-interface
- Bepaalt congestieprijzen op basis van belasting van transformatorstations
- Coördineert met `NationalEnergyMarket` voor marktprijzen

## GridNode

Een `GridNode` representeert een netwerkknooppunt, zoals een transformatorstation. Er zijn verschillende typen:

| Type | Beschrijving |
|------|-------------|
| GridNode Electricity | Elektrisch knooppunt; bewaakt congestie op transformatoren |
| GridNode Heat | Warmtenetknooppunt; sluit de warmtebalans |

Kenmerken:
- Heeft een beperkte doorvoercapaciteit (de 'flessenhals')
- Congestie wordt bijgehouden per tijdstap
- Warmtenetten sluiten de warmtebalans binnen het model (geen expliciete infrastructuur modellering voor gas/H2)

## GridConnection

Een `GridConnection` vertegenwoordigt een fysieke aansluiting op het energienet. Er zijn meerdere subtypes:

| Agent | Beschrijving |
|-------|-------------|
| `GCHouse` | Woonhuis |
| `GCIndustry` | Industriële aansluiting |
| `GCNeighborhood` | Aggregatie van meerdere huishoudens |
| `GCDistrictHeating` | Warmtenet-aansluiting |
| `GCEnergyProduction` | Productie-installatie (bijv. wind, zon) |
| `GCEnergyConversion` | Energieconversie (bijv. elektrolyseur, warmtepomp op netschaal) |
| `GCGridBattery` | Grootschalige netbatterij |
| `GCPublicCharger` | Publiek laadstation voor elektrische voertuigen |
| `GCUtility` | Nutsaansluiting |

Elke GridConnection:
- Heeft een lijst van energy assets die hij beheert
- Voert een Energy Management System (EMS) uit via de `I_EnergyManagement`-interface
- Berekent eigen KPIs na de jaarsimulatie

## Energy Assets

Energy assets zijn de fysieke installaties binnen een GridConnection. Ze erven van de abstracte basisklasse `J_EA`. Zie [Energy Assets](energy_assets.md) voor een volledig overzicht.

## EnergyCoop

De `EnergyCoop`-agent modelleert een energiecoöperatie. Hij:

- Beheert de leden (aangesloten GridConnections)
- Berekent collectief zelfverbruik
- Verwerkt economische transacties tussen leden

## EnergySupplier

De `EnergySupplier`-agent is een commerciële energieleverancier die:

- Energiecontracten beheert voor aangesloten GridConnections
- Energie levert op basis van importvraag van de aansluitingen

## NationalEnergyMarket

De `NationalEnergyMarket`-agent simuleert de groothandelsmarkt voor elektriciteit. Hij levert de basisprijscurve (`dailyPriceCurve_eurpMWh`) aan het markprijsmechanisme.

## GIS-agenten

Voor geografische visualisatie zijn drie agenttypen beschikbaar:

- `GIS_Object` – Generiek GIS-object
- `GIS_Building` – Gebouwcontouren
- `GIS_Parcel` – Perceelgrenzen

## GovernmentLayer

De `GovernmentLayer`-agent modelleert overheidsbeleid en regelgeving, zoals subsidies, belastingen of netwerknormen.

## UI-agenten

- `UI_EnergyAsset` – Visuele weergave van een energy asset op het canvas
- `UI_GridNode` – Visuele weergave van een netwerkknooppunt
- `EnergyDataViewer` – Datavisualisatie-agent voor resultaten

## Simulatieverloop

### Live-simulatie
Bij een live-simulatie gebruikt AnyLogic zijn ingebouwde tijdsfuncties. Per tijdstap worden de energiebalansen berekend, de EMS-algoritmen uitgevoerd en de resultaten bijgewerkt in grafieken.

### Jaarsimulatie
Bij een jaarsimulatie doorloopt de engine zelf een volledig jaar (8760 uur of 35040 kwartieren), zonder gebruik te maken van AnyLogic's tijdsfuncties. Dit is aanzienlijk sneller:

1. Initialiseer alle agenten en hun assets voor het jaar
2. Doorloop elk tijdstap (standaard kwartierresolutie):
   - Lees vaste verbruiksprofielen
   - Bereken energiebalans per GridConnection
   - Voer EMS uit voor elke aansluiting
   - Plan batterijen in (`BatteryScheduler`)
   - Plan flexibel verbruik in (`FlexAssetScheduler`)
   - Sla resultaten op in arrays
3. Na afloop: `f_calculateKPI()` aggregeert de resultaten hiërarchisch
4. Valideer energieconsistentie (zie [Rekenregels](rekenregels.md))
