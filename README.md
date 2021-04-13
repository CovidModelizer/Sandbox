# Covid_Modelizer_Sandbox

## Fonctionnalités
* Récupération des données : 
    * [Données relatives à l'épidémie du COVID-19 en France](https://www.data.gouv.fr/fr/datasets/donnees-relatives-a-lepidemie-de-covid-19-en-france-vue-densemble/#_)
    * [Indicateurs de suivi de l'épidémie de COVID-19](https://www.data.gouv.fr/fr/datasets/indicateurs-de-suivi-de-lepidemie-de-covid-19/)
* Modèles prédictifs d'infections : 
    * Un modèle linéaire (régression linéaire à une variable)
    * Un modèle SIR (modélisation mathématique d'une épidémie)
    * Un modèle Machine Learning (régression linéaire à treize variables)
* Modèles prédictifs de vaccinations : 
    * Un modèle linéaire (régression linéaire à une variable)
    * Un modèle SVIR (modélisation mathématique des vaccinations durant une épidémie)
    * Un modèle Machine Learning (régression linéaire à onze variables)

## Technologies
* Java 8
* [Weka 3.8](https://waikato.github.io/weka-wiki/documentation/)

## Launch
* Télécharger le certificat SSL de [opendata.gouv](https://www.data.gouv.fr/fr/)
* Importer le certificat dans votre JRE : 
```
keytool -import -file <certificat> -alias <donner un nom au certificat> -keystore <path vers le fichier cacerts>
```
* Récupérer les dernières données pour l'entraînement des modèles : 
    * Lancer la classe DataMapper
* Générer les prédictions : 
    * Lancer les classes des différents modèles prédictifs
