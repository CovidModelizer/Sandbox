# CovidModelizer - Sandbox

## Fonctionnalités
* Récupération des données : 
  * [Données relatives à l'épidémie de la COVID-19 en France](https://www.data.gouv.fr/fr/datasets/donnees-relatives-a-lepidemie-de-covid-19-en-france-vue-densemble/#_)
  * [Indicateurs de suivi de l'épidémie de la COVID-19](https://www.data.gouv.fr/fr/datasets/indicateurs-de-suivi-de-lepidemie-de-covid-19/)
* Modèles prédictifs d'infections : 
  * Un modèle de type linéaire
    * régression linéaire à une variable
  * Un modèle de type Machine Learning
    * régression linéaire à treize variables
  * Un modèle de type SIR
    * modélisation mathématique d'une épidémie
* Modèles prédictifs de vaccinations : 
  * Un modèle de type linéaire
    * régression linéaire à une variable
  * Un modèle de type Machine Learning
    * régression linéaire à onze variables
  * Un modèle de type SVIR
    * modélisation mathématique des vaccinations durant une épidémie

## Technologies
* Java 8
* [Weka 3.8](https://waikato.github.io/weka-wiki/documentation/)

## Launch
* Télécharger le certificat SSL de [opendata.gouv](https://www.data.gouv.fr/fr/)
* Importer le certificat dans votre JRE : 
```
keytool -import -file <certificat> -alias <donner un nom au certificat> -keystore <path vers le fichier cacerts>
```
* Pour récupérer les dernières données relatives à la covid et aux vaccinations : 
  * Lancer la méthode main de la classe DataMapper
* Pour générer les prédictions d'infections et de vaccinations : 
  * Lancer les méthodes main des classes de modèles prédictifs
* Pour récupérer les dernières données **et** générer les prédictions :
  * Lancer la méthode main de la classe App
