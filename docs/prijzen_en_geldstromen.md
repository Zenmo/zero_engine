# Energieprijzen en geldstromen

De Zenmo Zero Engine modelleert energieprijzen en financiële stromen via een combinatie van marktprijsmechanismen en contracten.

## Marginale prijscurves: Market.java

De kern van het prijsmechanisme zit in de klasse `zeroPackage/Market.java`. Deze berekent per tijdstap een marginale prijs [€/MWh] op basis van vier componenten:

```
Marginale prijs = Basisprijs + Zelfverbruiksbonus + Congestieterm + Marktfeedbackterm
```

### 1. Basisprijs (`dailyPriceCurve_eurpMWh`)

De dagelijkse groothandelsprijscurve, geleverd door de `NationalEnergyMarket`-agent. Dit is een array van prijzen per tijdstap [€/MWh].

### 2. Zelfverbruiksbonus (`selfConsumptionSaving_eurpMWh`)

Een vaste opslag op de prijs die actief is wanneer de aansluiting netto verbruikt (importerend is). Dit stimuleert lokaal zelfverbruik boven export naar het net.

- Actief wanneer: `loadProfile_kW >= 0` (netto verbruik)
- Waarde: constante parameter [€/MWh]

### 3. Congestieterm (`congestionDeadzone_kW`, `congestionFactor_eurpMWhpkW`)

De congestieterm verhoogt of verlaagt de prijs op basis van de netbelasting:

- Buiten de deadzone: prijsopslag proportioneel aan de netbelasting
- Formule: `loadProfileSign × (|load| − deadzone + |load|) × congestionFactor`
- Stimuleert flexibel verbruik om congestie op transformatoren te vermijden

De `deadzone` voorkomt dat kleine onbalansen al congestieprijzen activeren.

### 4. Marktfeedbackterm (`marketFeedback_eurpMWhpkW`)

Een feedbackterm die de marginale prijs aanpast op basis van de hoeveelheid flex die al ingepland is:

- Formule: `2 × flexProfile_kW × marketFeedback`
- Zorgt ervoor dat het systeem niet alle flex tegelijk op hetzelfde moment inpland (vermijdt 'valley filling' en nieuwe pieken)

### Opwaartse vs. neerwaartse prijscurve

De `Market`-klasse berekent twee prijscurves:

- **`getMarginalPriceCurveUpwards()`** – Kostprijs voor het vergroten van verbruik (opladen batterij, starten flex-last). Relevant voor consumptie-sturing.
- **`getMarginalPriceCurveDownwards()`** – Opbrengst voor het verminderen van verbruik of leveren van energie. Relevant voor ontladen batterij of teruglevering.

Het verschil tussen beide curven bepaalt de winstmarge voor energiearbitrage.

---

## Contracten

Financiële stromen worden vastgelegd in contracten tussen agenten. De basisklasse is `J_Contract`, met de volgende subtypes:

### J_DeliveryContract – Energieleveringscontract

Contract tussen een GridConnection en een EnergySupplier voor de levering van elektriciteit, gas of warmte.

Bevat typisch:
- Energieprijs [€/kWh] of [€/MWh] (vast of variabel)
- Salderingsregels
- Eventuele terugleververgoeding

### J_TransportContract – Transportcontract

Contract met de GridOperator voor het gebruik van het elektriciteitsnet. Bevat:

- Transportkosten [€/kWh]
- Netbeheerkosten
- Vastrecht [€/jaar]

### J_TaxContract – Belasting- en subsidiecontract

Modellering van energiebelasting, ODE (Opslag Duurzame Energie) en eventuele subsidies (bijv. SDE++). Bevat:

- Belastingtarief [€/kWh] per energiedrager
- Eventuele vrijstellingsdrempel (bijv. salderingsgrens)
- Subsidietarief [€/kWh] voor teruggeleverde energie

### J_CapacitySharingContract – Capaciteitsdeling

Contract waarbij meerdere aansluitingen gezamenlijk gebruikmaken van één netaansluiting of capaciteit. Relevant voor:

- Collectieve aansluitingen (bijv. VvE of bedrijventerrein)
- Gedeelde batterijopslag

### J_ConnectionContract – Aansluitingscontract

Beschrijft de technische en financiële voorwaarden van de fysieke aansluiting op het net:

- Aansluitcapaciteit [kW] (contractcapaciteit)
- Aansluitingskosten [€/jaar]

---

## Geldstromen per tijdstap

Tijdens de simulatie worden per tijdstap financiële stromen bijgehouden voor elke GridConnection:

| Stroom | Richting | Beschrijving |
|--------|----------|-------------|
| Energiekosten import | Uit | Betaling voor geïmporteerde energie × energieprijs |
| Terugleververgoeding | In | Vergoeding voor geëxporteerde energie × terugleverprijs |
| Nettarief | Uit | Transportkosten op basis van verbruik of piek |
| Belasting | Uit | Energiebelasting op verbruik |
| Subsidie | In | Productievergoeding (bijv. SDE++) |
| Coöperatievoordeel | In/Uit | Voordeel door collectief zelfverbruik in een EnergyCoop |

---

## KPI-financiën

Na de jaarsimulatie worden de financiële KPIs berekend in `f_calculateKPI()` per GridConnection. Typische outputs:

- **Totale energiekosten** [€/jaar] – Bruto kosten voor energie-import
- **Terugleverbaten** [€/jaar] – Inkomsten uit export
- **Netto energiekosten** [€/jaar] – Saldo import/export
- **Transportkosten** [€/jaar] – Netkosten
- **Belastingen** [€/jaar]
- **Subsidies** [€/jaar]
- **Totale woonlasten** [€/jaar] – Som van alle kosten en baten

Deze waarden worden geaggregeerd op GridNode- en EnergyModel-niveau voor een systeembreed overzicht.

---

## Prijsprikkel voor flexibel gedrag

De combinatie van marginale prijscurves en schedulingalgoritmen (zie [Modelstructuur](modelstructuur.md)) zorgt voor een marktgebaseerde sturing van flexibel verbruik:

1. `Market.java` berekent de marginale prijs per tijdstap
2. `BatteryScheduler` gebruikt deze prijzen om winstgevende laad/ontlaadcycli te vinden
3. `FlexAssetScheduler` plant flexibel verbruik in op de goedkoopste momenten
4. De marktfeedbackterm voorkomt dat alle assets simultaan reageren op dezelfde prikkel
