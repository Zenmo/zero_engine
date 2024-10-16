# Berekening KPIs jaarsimulatie

Na een jaarsimulatie wordt op verschillende niveaus een KPI-berekening gedaan. 
Op het 'hoogste' niveau geldt dat voor het energyModel zelf in de functie f_calculateKPI(). Verder heeft ook elke GridConnection, GridNode en EnergyCoop z'n eigen KPI-berekening die na de simulatie van een jaar wordt gemaakt.

De hierarchy van deze verschillende KPI functies is dat de f_calculateKPI() in het energyModel wordt aangeroepen. Hierin wordt vervolgens voor alle GridConnections, GridNodes en EnergyCoops ook hun KPI-berekening, in die volgorde, aangeroepen. Deze volgorde is belangrijk en kan niet zomaar veranderd worden.

## Arrays en jaartotalen
Tijdens de jaarsimulatie wordt relevante data per tijdstap opgeslagen in tijdsreeksen (arrays). De conventie is om jaartotalen achteraf op te tellen wanneer mogelijk, omdat hiervoor (snellere) array operaties gebruikt kunnen worden. 
Uitzonderingen hierop zijn (nu nog) de totalen voor zomer/winter, dag/nacht en week/weekend. In princiepe zouden deze ook achteraf uit de arrays gehaald kunnen worden, maar daarvoor is nog geen implementatie gemaakt.

## KPIs on-demand
Het uitvoeren van de functie f_calculateKPI voor alle agents kost tijd, minder dan de jaarsimulatie maar wel enkele tientallen procenten daarvan. Om de wachttijd voor een jaarsimulatie te minimaliseren overwegen we om de aanroepen van deze f_calculateKPI functies _on-demand_ te doen.
Dit is momenteel nog niet geïmplementeerd, maar de optie is aanwezig indien de rekentijd voor een jaarsimulatie langer wordt dan gewenst.

## Datasets
Anylogic werkt met objecten van het type Dataset om tijdsreeksen weer te geven in grafieken. Dit object-type bevat naast de waardes van een tijdsreeks (y-as), ook de tijdstippen (x-as), en biedt bovendien nog wat andere methodes die een java array niet heeft. Het is daarintegen ook een veel 'zwaarder' object. Om die reden werkt de jaarsimulatie zo veel mogelijk met arrays (alleen de y-as) voor het opslaan van data. Wanneer data in een grafiek weergegeven moet worden, zal deze echter in een anylogic dataset gezet moeten worden. Dit gebeurt zoveel mogelijk ná de jaarsimulatie, zodat dit eventueel ook 'on-demand' gedaan kan worden wanneer een bepaalde grafiek wordt geselecteerd.

Het model hanteert datasets voor jaartotalen met meestal 365 elementen; elk element is een dag-totaal. De reden dat dit niet 35040 kwartier-totalen zijn, is dat zo'n kwartier-tijdsreeks vaak onleesbaar wordt in een klein grafiekje.

Voor de live-simulatie gebruikt het model datasets met 672 kwartieren, ofwel een week aan data. 



