# Rekenregels voor consistente energiehuishouding

De Zenmo Zero Engine past een aantal rekenregels toe die moeten zorgen voor een consistente energiehuishouding, zonder 'lekken' (positief of negatief) van energie. 
De basis van het energiemodel is de wet van behoud van energie: energie kan nooit ontstaan of verdwijnen. 

Energie kan wel het model in of uit stromen. 
Dat kan via de volgende paden:
* Import van de energiedragers elektriciteit, gas, waterstof en diesel/benzine (nu 1 energiedrager). 
* Export van de energiedragers elektriciteit, gas, en waterstof. (de aanname is dat in onze modellen nooit diesel of benzine geproduceerd worden)
* Primaire opwek (winning) van energie zoals energieproductie door windmolens en zonnepanelen, maar ook instraling en convectie van warmte van de buitenwereld naar gebouwen in het model. Hier _onstaat_ geen nieuwe energie, maar wordt als het ware energie _geoogst_ (gewonnen). Deze energiestromen worden in het model bijgehouden als energyProduction_kW.
* Finaal verbruik van energie. Dit is energie die het systeem verlaat, die 'finaal verloren' is. In de praktijk is finaal vebruik in natuurkundige zin eigenlijk altijd gedissipeerde warmte. Hieronder vallen zaken als consumptieprofielen (pure consumptie), maar ook verliezen (losses) van energieomzetting of bij het opslaan van energie in buffers. Deze energiestromen worden in het model bijgehouden als energyConsumption_kW.

Bij primaire opwek en finaal verbruik is het belangrijk om je bewust te zijn van wat de systeemgrens is. Wanneer verlaat energie het systeem en wanneer komt het binnen? 

## Consistency-check
Een jaarsimulatie van het model wordt altijd voorzien van een consistency check: is er geen energie-lek? 
Dit gebeurt door de twee kanten van de energiebalans vergelijking te vergelijken:
```math 
Opwek_{kWh} – export_{kWh} = Verbruik_{kWh} – import_{kWh} = zelfverbruik​_{kWh}
```

Wanneer deze twee berekeningen niet op hetzelfde zelfverbruik uitkomen, geeft dat aan dat er ergens in het model een rekenfout gemaakt wordt.


## van vermogen [kW] naar energie [kWh]: p_timestep_h
Numerieke integratie van vermogens per tijdstap: optellen van vermogen[kW]*tijdstap[h] = energie [kWh].
De 'solver' hierbij is het zero-order-hold princiepe: we benaderen vermogens als constant gedurende een tijdstap. 
