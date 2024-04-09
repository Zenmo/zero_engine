# Modeling conventions

## Objecten op het canvas
Alle opjecten op het canvas dienen als eerste letters van hun naam een type-aanduiding te hebben gevolgd door een underscore en een beschrijving van hun functie/inhoud.
De type aanduidingen zijn:
* p_ voor parameter (eventueel met uitzondering b_ voor boolean parameters)
* v_ voor variabele (eventueel met uitzondering b_ voor boolean variabelen)
* f_ voor functie
* gr_ voor group
* pl_ voor plot
* ch_ voor chart
* e_ voor event
* c_ voor collectie

## Eenheden
Alle variabelen die een fysieke grootheid representeren, denk aan energie [kWh], vermogen [kW], tijd [h], etc., dienen aan het einde van hun naam _[unit] te hebben.
Dit geldt ook voor functies die een fysieke grootheid als return geven, bijvoorbeeld de functie getCapacityElectric_kW() van een energy asset.
Het toevoegen van de eenheid in de naam verkomt verwarring over de betekenis van variabelen, en ook over de grootte (kW vs MW vs GW, etc.), en voorkomt daarmee rekenfouten.

## van vermogen [kW] naar energie [kWh]: p_timestep_h
Numerieke integratie van vermogens per tijdstap: optellen van vermogen[kW]*tijdstap[h] = energie [kWh]
De 'solver' hierbij is het zero-order-hold princiepe: we benaderen vermogens als constant gedurende een tijdstap. 
