# Datasets en tijdreeksen

De Zenmo Zero Engine maakt onderscheid tussen twee vormen van dataopslag: **arrays** tijdens de simulatie voor maximale snelheid, en **AnyLogic Datasets** na de simulatie voor visualisatie.

## Arrays tijdens de jaarsimulatie

Tijdens een jaarsimulatie worden alle relevante tijdreeksen opgeslagen als gewone Java `double[]`-arrays. Dit heeft twee voordelen:

1. **Snelheid** – Arrays zijn veel lichter dan AnyLogic Dataset-objecten. Dit is cruciaal omdat een jaarsimulatie tot 35.040 tijdstappen (kwartieren) telt.
2. **Schaalbaarheid** – Bij grote modellen met honderden agenten zou het gebruik van Datasets de geheugenbelasting sterk verhogen.

De arrays bevatten alleen de y-as (de vermogenswaarden per tijdstap). De x-as (tijdstip) wordt impliciet bepaald door de tijdstapparameter `p_timestep_h` en het starttijdstip van de simulatie.

## ZeroAccumulator

De klasse `J_ZeroAccumulator` is een hulpklasse voor het efficiënt bijhouden van tijdreeksdata. Hij accumuleert waarden per tijdstap in een interne array en biedt na de simulatie methoden om de data om te zetten naar een AnyLogic Dataset. Dit patroon vermijdt het aanmaken van zware objecten tijdens de simulatie zelf.

## Conversie naar AnyLogic Datasets

Na afloop van een jaarsimulatie worden de arrays omgezet naar AnyLogic `Dataset`-objecten. Dit gebeurt:

- **Op aanvraag** (on-demand) wanneer een specifieke grafiek wordt geselecteerd
- Of **in bulk** na afloop van de KPI-berekening

Door de conversie uit te stellen tot het moment van visualisatie, wordt de rekentijd van de jaarsimulatie zelf minimaal gehouden.

## Dataset-conventies

### Jaardatasets (grafieken op dagbasis)
Het model gebruikt datasets met **365 elementen**, waarbij elk element een dagtotaal is. Kwartierresolutie (35.040 elementen) wordt niet gebruikt voor grafieken, omdat dit onleesbaar wordt in kleine grafiekvensters.

```
Dataset-grootte: 365 elementen
X-as:            Dag van het jaar (1–365)
Y-as:            Dagtotaal [kWh] of gemiddeld dagvermogen [kW]
```

### Live-simulatiedatasets (grafieken per kwartier)
Voor de live-simulatie gebruikt het model datasets met **672 elementen**, wat overeenkomt met één week aan kwartierdata (7 × 24 × 4 = 672).

```
Dataset-grootte: 672 elementen
X-as:            Tijdstip [kwartier]
Y-as:            Vermogen [kW]
```

### Tijdstapgrootte
De standaard tijdstap is **15 minuten** (0,25 uur). Dit geeft:

| Periode | Aantal tijdstappen |
|---------|--------------------|
| 1 dag   | 96                 |
| 1 week  | 672                |
| 1 jaar  | 35.040             |

De tijdstap is instelbaar via `p_timestep_h`. Andere veelgebruikte waarden zijn 0,5 h (30 min) of 1 h.

## Tijdreeksprofielen: J_EAProfile en J_HourlyCurvesData

Verbruiks- en productieprofielen worden opgeslagen in:

- **`J_EAProfile`** – Een enkel tijdreeksprofiel voor een specifieke asset (bijv. PV-productie, huishoudelijk verbruik). Bevat zowel de vermogenswaarden als metadata over het profiel.
- **`J_HourlyCurvesData`** – Container voor 8.760-uurse jaarprofielen (of 35.040 kwartierprofielen). Wordt gebruikt voor groothandelsmarktprijzen en weersprofielen.
- **`J_ProfilePointer`** – Verwijzing naar een positie binnen een profiel, gebruikt om efficiënt het huidige tijdstap-waarde op te zoeken.

## Tijdparameters: J_TimeParameters en J_TimeVariables

- **`J_TimeParameters`** – Onveranderlijke tijdconfiguratie voor de simulatie: tijdstapgrootte, simulatiejaar, seizoensgrenzen, maandgrenzen. Geannoteerd met `@JsonCreator` voor serialisatie van `final`-velden.
- **`J_TimeVariables`** – Veranderlijke toestand: huidig tijdstip, huidige dag, huidig seizoen. Wordt per tijdstap bijgewerkt.

## Zomer/winter en dag/nacht splitsingen

Totalen voor zomer/winter, dag/nacht en week/weekend worden momenteel **tijdens** de simulatie bijgehouden (niet achteraf afgeleid uit de arrays). Dit is een uitzondering op de algemene conventie van het achteraf berekenen van totalen. In de toekomst kunnen ook deze gesplitste totalen achteraf worden berekend via array-operaties.

## Simulatieresultaten: J_SimulationResults

Na de KPI-berekening worden de jaarresultaten opgeslagen in een `J_SimulationResults`-object per agent. Dit bevat:

- Energietotalen [kWh]: import, export, productie, verbruik, zelfverbruik
- Financiële KPIs [€]: energiekosten, netkosten, belastingen, subsidies
- Emissies [kg CO₂]
- Netimpact: piekbelasting [kW], congestie-uren [h]
