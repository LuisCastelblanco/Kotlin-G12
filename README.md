# ISIS3510 - Kotlin App

📌 **Project Description**  
This repository contains the source code for the app developed using **Kotlin** for Android. The project is part of the **ISIS3510** course and was built by the following team members.

---

👥 **Group Members**

| Full Name                          | Email             | Code       |
|-----------------------------------|-------------------|------------|
| Camilo A Daza R                   | ca.daza10         | 201416461  |
| Luis Castelblanco                 | la.castelblanco   | 201910966  |
| Juan Peña                         | j.penaj           | 202212756  |

---

🚀 **Getting Started**

### Prerequisites  
Make sure you have the following installed:

- Android Studio  
- Kotlin Plugin  
- Android SDK  

### Installation

Clone the repository:
```bash
git clone https://github.com/ISIS3510-G12-Kotlin/Kotlin-G12.git
```
Navigate to the project directory:
```bash
cd Kotlin-G12
```
Open the project in Android Studio, let it sync and install the required dependencies.
Then, run the app on an emulator or physical device:
```bash
Run > Run 'app'
```

```
Kotlin-G12
├─ .idea
│  ├─ .name
│  ├─ AndroidProjectSystem.xml
│  ├─ codeStyles
│  │  ├─ codeStyleConfig.xml
│  │  └─ Project.xml
│  ├─ compiler.xml
│  ├─ deploymentTargetSelector.xml
│  ├─ gradle.xml
│  ├─ inspectionProfiles
│  │  └─ Project_Default.xml
│  ├─ kotlinc.xml
│  ├─ migrations.xml
│  ├─ misc.xml
│  ├─ runConfigurations.xml
│  └─ vcs.xml
├─ .kotlin
│  └─ sessions
├─ app
│  ├─ proguard-rules.pro
│  └─ src
│     ├─ androidTest
│     │  └─ java
│     │     └─ com
│     │        └─ example
│     │           └─ explorandes
│     │              └─ ExampleInstrumentedTest.kt
│     ├─ main
│     │  ├─ AndroidManifest.xml
│     │  ├─ java
│     │  │  └─ com
│     │  │     └─ example
│     │  │        └─ explorandes
│     │  │           ├─ adapters
│     │  │           │  ├─ BuildingAdapter.kt
│     │  │           │  ├─ CategoriesAdapter.kt
│     │  │           │  ├─ PlacesAdapter.kt
│     │  │           │  └─ RecommendationAdapter.kt
│     │  │           ├─ HomeActivity.kt
│     │  │           ├─ MainActivity.kt
│     │  │           ├─ models
│     │  │           │  ├─ Building.kt
│     │  │           │  ├─ Category.kt
│     │  │           │  ├─ Place.kt
│     │  │           │  ├─ Recommendation.kt
│     │  │           │  └─ UserLocation.kt
│     │  │           ├─ repositories
│     │  │           │  └─ PlaceRepository.kt
│     │  │           ├─ services
│     │  │           │  └─ LocationService.kt
│     │  │           ├─ SplashActivity.kt
│     │  │           └─ ui
│     │  │              ├─ navigation
│     │  │              │  ├─ NavigationFragment.kt
│     │  │              │  └─ NavigationViewModel.kt
│     │  │              └─ theme
│     │  │                 ├─ Color.kt
│     │  │                 ├─ Theme.kt
│     │  │                 └─ Type.kt
│     │  └─ res
│     │     ├─ drawable
│     │     │  ├─ circular_background.xml
│     │     │  ├─ header_background.xml
│     │     │  ├─ header_curved_background.xml
│     │     │  ├─ ic_account.xml
│     │     │  ├─ ic_building.xml
│     │     │  ├─ ic_event.xml
│     │     │  ├─ ic_favorite.xml
│     │     │  ├─ ic_filter.xml
│     │     │  ├─ ic_food.xml
│     │     │  ├─ ic_home.xml
│     │     │  ├─ ic_launcher_background.xml
│     │     │  ├─ ic_launcher_foreground.xml
│     │     │  ├─ ic_navigate.xml
│     │     │  ├─ ic_notification.xml
│     │     │  ├─ ic_search.xml
│     │     │  ├─ ic_services.xml
│     │     │  ├─ ic_study.xml
│     │     │  └─ profile_placeholder.xml
│     │     ├─ layout
│     │     │  ├─ activity_home.xml
│     │     │  ├─ activity_splash.xml
│     │     │  ├─ fragment_navigation.xml
│     │     │  ├─ item_building.xml
│     │     │  ├─ item_category.xml
│     │     │  ├─ item_place.xml
│     │     │  └─ item_recommendation.xml
│     │     ├─ menu
│     │     │  └─ bottom_navigation_menu.xml
│     │     ├─ mipmap-anydpi
│     │     │  ├─ ic_launcher.xml
│     │     │  └─ ic_launcher_round.xml
│     │     ├─ mipmap-anydpi-v26
│     │     │  ├─ ic_launcher.xml
│     │     │  └─ ic_launcher_round.xml
│     │     ├─ mipmap-hdpi
│     │     │  ├─ ic_launcher.webp
│     │     │  └─ ic_launcher_round.webp
│     │     ├─ mipmap-mdpi
│     │     │  ├─ ic_launcher.webp
│     │     │  └─ ic_launcher_round.webp
│     │     ├─ mipmap-xhdpi
│     │     │  ├─ ic_launcher.webp
│     │     │  └─ ic_launcher_round.webp
│     │     ├─ mipmap-xxhdpi
│     │     │  ├─ ic_launcher.webp
│     │     │  └─ ic_launcher_round.webp
│     │     ├─ mipmap-xxxhdpi
│     │     │  ├─ ic_launcher.webp
│     │     │  └─ ic_launcher_round.webp
│     │     ├─ values
│     │     │  ├─ colors.xml
│     │     │  ├─ dimens.xml
│     │     │  ├─ strings.xml
│     │     │  └─ themes.xml
│     │     └─ xml
│     │        ├─ activity_splash.xml
│     │        ├─ backup_rules.xml
│     │        └─ data_extraction_rules.xml
│     └─ test
│        └─ java
│           └─ com
│              └─ example
│                 └─ explorandes
│                    └─ ExampleUnitTest.kt
├─ gradle
│  ├─ libs.versions.toml
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradle.properties
├─ gradlew
├─ gradlew.bat
└─ README.md

```

```
Kotlin-G12
├─ .idea
│  ├─ .name
│  ├─ AndroidProjectSystem.xml
│  ├─ codeStyles
│  │  ├─ codeStyleConfig.xml
│  │  └─ Project.xml
│  ├─ compiler.xml
│  ├─ deploymentTargetSelector.xml
│  ├─ gradle.xml
│  ├─ inspectionProfiles
│  │  └─ Project_Default.xml
│  ├─ kotlinc.xml
│  ├─ migrations.xml
│  ├─ misc.xml
│  ├─ runConfigurations.xml
│  └─ vcs.xml
├─ .kotlin
│  └─ sessions
├─ app
│  ├─ proguard-rules.pro
│  └─ src
│     ├─ androidTest
│     │  └─ java
│     │     └─ com
│     │        └─ example
│     │           └─ explorandes
│     │              └─ ExampleInstrumentedTest.kt
│     ├─ main
│     │  ├─ AndroidManifest.xml
│     │  ├─ java
│     │  │  └─ com
│     │  │     └─ example
│     │  │        └─ explorandes
│     │  │           ├─ adapters
│     │  │           │  ├─ BuildingAdapter.kt
│     │  │           │  ├─ CategoriesAdapter.kt
│     │  │           │  ├─ PlacesAdapter.kt
│     │  │           │  └─ RecommendationAdapter.kt
│     │  │           ├─ api
│     │  │           │  ├─ ApiClient.kt
│     │  │           │  └─ ApiService.kt
│     │  │           ├─ HomeActivity.kt
│     │  │           ├─ MainActivity.kt
│     │  │           ├─ models
│     │  │           │  ├─ AuthRequest.kt
│     │  │           │  ├─ AuthResponse.kt
│     │  │           │  ├─ Building.kt
│     │  │           │  ├─ Category.kt
│     │  │           │  ├─ Place.kt
│     │  │           │  ├─ Recommendation.kt
│     │  │           │  └─ UserLocation.kt
│     │  │           ├─ repositories
│     │  │           │  └─ PlaceRepository.kt
│     │  │           ├─ services
│     │  │           │  ├─ BrightnessController.kt
│     │  │           │  ├─ LightSensorManager.kt
│     │  │           │  └─ LocationService.kt
│     │  │           ├─ SplashActivity.kt
│     │  │           ├─ ui
│     │  │           │  ├─ navigation
│     │  │           │  │  ├─ NavigationFragment.kt
│     │  │           │  │  └─ NavigationViewModel.kt
│     │  │           │  └─ theme
│     │  │           │     ├─ Color.kt
│     │  │           │     ├─ Theme.kt
│     │  │           │     └─ Type.kt
│     │  │           └─ utils
│     │  │              └─ SessionManager.kt
│     │  └─ res
│     │     ├─ drawable
│     │     │  ├─ circular_background.xml
│     │     │  ├─ header_background.xml
│     │     │  ├─ header_curved_background.xml
│     │     │  ├─ ic_account.xml
│     │     │  ├─ ic_building.xml
│     │     │  ├─ ic_event.xml
│     │     │  ├─ ic_favorite.xml
│     │     │  ├─ ic_filter.xml
│     │     │  ├─ ic_food.xml
│     │     │  ├─ ic_home.xml
│     │     │  ├─ ic_launcher_background.xml
│     │     │  ├─ ic_launcher_foreground.xml
│     │     │  ├─ ic_navigate.xml
│     │     │  ├─ ic_notification.xml
│     │     │  ├─ ic_search.xml
│     │     │  ├─ ic_services.xml
│     │     │  ├─ ic_study.xml
│     │     │  └─ profile_placeholder.xml
│     │     ├─ layout
│     │     │  ├─ activity_home.xml
│     │     │  ├─ activity_splash.xml
│     │     │  ├─ fragment_navigation.xml
│     │     │  ├─ item_building.xml
│     │     │  ├─ item_category.xml
│     │     │  ├─ item_place.xml
│     │     │  └─ item_recommendation.xml
│     │     ├─ menu
│     │     │  └─ bottom_navigation_menu.xml
│     │     ├─ mipmap-anydpi
│     │     │  ├─ ic_launcher.xml
│     │     │  └─ ic_launcher_round.xml
│     │     ├─ mipmap-anydpi-v26
│     │     │  ├─ ic_launcher.xml
│     │     │  └─ ic_launcher_round.xml
│     │     ├─ mipmap-hdpi
│     │     │  ├─ ic_launcher.webp
│     │     │  └─ ic_launcher_round.webp
│     │     ├─ mipmap-mdpi
│     │     │  ├─ ic_launcher.webp
│     │     │  └─ ic_launcher_round.webp
│     │     ├─ mipmap-xhdpi
│     │     │  ├─ ic_launcher.webp
│     │     │  └─ ic_launcher_round.webp
│     │     ├─ mipmap-xxhdpi
│     │     │  ├─ ic_launcher.webp
│     │     │  └─ ic_launcher_round.webp
│     │     ├─ mipmap-xxxhdpi
│     │     │  ├─ ic_launcher.webp
│     │     │  └─ ic_launcher_round.webp
│     │     ├─ values
│     │     │  ├─ colors.xml
│     │     │  ├─ dimens.xml
│     │     │  ├─ strings.xml
│     │     │  └─ themes.xml
│     │     └─ xml
│     │        ├─ activity_splash.xml
│     │        ├─ backup_rules.xml
│     │        └─ data_extraction_rules.xml
│     └─ test
│        └─ java
│           └─ com
│              └─ example
│                 └─ explorandes
│                    └─ ExampleUnitTest.kt
├─ gradle
│  ├─ libs.versions.toml
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradle.properties
├─ gradlew
├─ gradlew.bat
└─ README.md

```