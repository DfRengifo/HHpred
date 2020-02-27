# HHpred
Proyecto de Grado

Daniel Rengifo 201533922

- Implementacion PSI-BLAST 
    - WEB API NBCI BLAST

- Implemntacion Prediccion estructura secudnaria
    - WEB API PSIPRED
    * errores por parte del servidor, corregidos

- Intergacion de procesos blast y psipred
    - Threads
    * Inconisstencias en tiempos de ejecucion de Blast
        * incluir ".be-md" en la URL, resuelve el problema, sin embargo no es recomendable, en aras de la comunidad, usar el recurso repetidamente.