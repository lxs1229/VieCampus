# VieCampus

Assistant mobile pour la vie étudiante : gestion du planning, tâches + rappels locaux, calcul du GPA et écran de connexion (sans backend). Données stockées en local avec Room, rappels gérés par WorkManager.

## Points clés

- **Emploi du temps** : cours par jour/créneau, ajout/édition/suppression, notes et enseignant optionnels.
- **Tâches & rappels** : trois types (todo, devoir, examen) avec statut (à faire / en cours / terminé), échéance facultative et notification locale programmée.
- **GPA** : saisie des notes (échelle 0‑20) et crédits pour calculer le GPA courant.
- **Connexion** : validation basique email/mot de passe puis accès aux onglets principaux (prêt à être branché à une vraie auth).
- **Notifications** : demande de permission sur Android 13+, WorkManager + NotificationCompat pour déclencher les rappels et rediriger vers la liste des tâches.

## Stack technique

- Kotlin + ViewBinding, composants Material, Navigation (NavHost + BottomNavigation).
- Room (entités/DAO/Repository) pour la persistance ; LiveData/Flow pour alimenter l’UI.
- WorkManager + NotificationCompat pour les notifications locales.
- Coroutines (Dispatchers.IO) pour le travail hors UI.

## Structure

- `app/src/main/java/com/example/viecampus/`
  - `ui/` : `schedule` (planning), `tasks` (tâches/rappels), `gpa`, `auth` (connexion) avec fragments, ViewModels et adaptateurs.
  - `data/` : `entity`, `dao`, `CampusRepository`, `CampusDatabase`.
  - `reminders/` & `notifications/` : planification et canaux de notification.
  - `MainActivity` : héberge le NavHost, gère la permission de notification, affiche/masque app bar + bottom bar selon la destination.
- `app/src/main/res/` : layouts, navigation, menus, chaînes, valeurs.

## Prérequis

- Android Studio Jellyfish ou plus récent.
- JDK 17+ (AGP 8.13) ; cible bytecode Java 11.
- SDK Android 36 (`compileSdk` / `targetSdk` / `minSdk` = 36 ; utiliser un émulateur ou appareil Android 15).
- Utiliser le Gradle Wrapper fourni (`./gradlew`).

## Démarrage rapide

```bash
git clone <url-du-repo>
cd VieCampus
# Vérifier que local.properties pointe vers un SDK Android valide
./gradlew assembleDebug
```

- APK générée : `app/build/outputs/apk/debug/app-debug.apk`.
- Première ouverture : l’écran de connexion accepte toute adresse email valide + mot de passe ≥ 6 caractères.
- Android 13+ : accepter la permission de notifications pour que les rappels s’affichent.

## Tests

```bash
./gradlew test
```

Ajoutez vos tests instrumentés dans `app/src/androidTest` si nécessaire.

## Personnalisation

- Textes : modifier `app/src/main/res/values/strings.xml`.
- Modèle de données : ajuster `data/entity` + DAO/Repository puis mettre à jour l’UI.
- Rappels : adapter la logique/format dans `reminders/ReminderScheduler` et `TaskReminderWorker`.

