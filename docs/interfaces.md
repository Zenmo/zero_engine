## Interfaces naar andere modules

### Project-main modellen
Elk project dat met zero_engine werkt heeft een eigen 'main' model. Daarin wordt brondata ingelezen en gebruikt om de agent-populaties aan te maken. Ook bevat dit main model:
-Sliders waarmee het energiesysteem aangepast kan worden door de gebruiker tijdens de live-simulatie en voor de volgende jaarsimulatie
-De koppeling naar de results_ui module, waarmee resultaten in diverse grafieken weergegeven kunnen worden
-De aansturing van de tijdsstappen van de 'live-simulatie', met gebruik van de in Anylogic ingebouwde tijdsfuncties. 
-Het triggeren van een jaarsimulatie in de engine, waarbij de engine zélf een heel jaar doorrekent, zonder gebruik te maken van de in Anylogic ingebouwde tijdsfuncties.

### Results-ui
Er is geen _directe_ link tussen de zero_engine en de results_ui. Beide worden aangeroepen vanuit het project-main model, maar hoeven in principe niet _direct_ met elkaar te communiceren.
