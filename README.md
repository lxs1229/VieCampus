# VieCampus

Application Android permettant aux étudiants de gérer leur vie sur le campus : emploi du temps, tâches (devoirs, examens, to‑do), calcul du GPA et rappels via notifications locales.

## Fonctionnalités

- **Emploi du temps** : enregistrement des cours (jour, horaires, salle, enseignant, notes) avec affichage trié et possibilité d’ajouter/modifier/supprimer.
- **Tâches et rappels** : gestion des tâches avec statut (à faire, en cours, terminé), types (tâche, devoir, examen), échéances et rappels automatiques via WorkManager + notifications.
- **Calcul du GPA** : ajout de notes et crédits pour obtenir automatiquement le GPA courant.
- **Persistance locale** : toutes les données (cours, tâches) sont stockées via Room.
- **Interface moderne** : navigation par onglets inférieurs, dialogs Material Design, prise en charge des appareils récents (SDK 36).

## Prérequis

- JDK 21 (ou 17) – ne pas utiliser JDK 25 qui n’est pas supporté par AGP/Kotlin actuels.
- Android Studio Jellyfish ou plus récent.
- Gradle Wrapper fourni dans le projet (`./gradlew`).

## Démarrage rapide

```bash
git clone https://github.com/lxs1229/VieCampus.git
cd VieCampus
./gradlew clean assembleDebug
```

> Ajustez `JAVA_HOME` si nécessaire :  
> `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home`

Installez ensuite l’APK générée (`app/build/outputs/apk/debug/app-debug.apk`) sur un appareil ou un émulateur.

## Structure principale

- `app/src/main/java/com/example/viecampus/`
  - `MainActivity` : shell de navigation et gestion des permissions de notifications.
  - `data/` : entités, DAO, base Room et repository.
  - `ui/schedule`, `ui/tasks`, `ui/gpa` : fragments, viewmodels et adaptateurs pour chaque module.
  - `reminders/` : worker WorkManager et planificateur de notifications.
- `app/src/main/res/` : ressources XML (layouts, menus, navigation, valeurs).

## Tests & qualité

Pour une exécution complète des tests unitaires :

```bash
./gradlew test
```

Vous pouvez ajouter vos propres tests instrumentés dans `app/src/androidTest`.

## Personnalisation

- Adapter les chaînes dans `app/src/main/res/values/strings.xml`.
- Modifier les schémas Room (entités & DAOs) si de nouvelles données doivent être stockées.
- Ajouter d’autres types de rappels en créant de nouveaux Workers ou canaux de notification.

## Licence

Projet interne / académique – adaptez la licence selon vos besoins.
