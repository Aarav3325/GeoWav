---

name: compose-screen
description: Creates Jetpack Compose screens. Use when building a new screen, creating UI layouts, implementing Material 3 designs, or generating Compose code from requirements.
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

When creating a Compose screen:

1. Identify the screen purpose and key user actions.
2. Create a clear layout hierarchy using Material 3 components.
3. Keep composables small and reusable.
4. Hoist state when appropriate.
5. Use LazyColumn or LazyRow for dynamic content.
6. Include loading, empty, and error states when relevant.
7. Follow MVVM architecture and assume data comes from a ViewModel.
8. Generate production-ready Kotlin code.

Screen structure should generally include:

* Route/Screen composable
* Content composable
* Reusable UI components
* Preview composable

Prefer:

* Material 3
* StateFlow-based state management
* Accessibility-friendly components
* Responsive spacing and typography

Avoid:

* Business logic inside composables
* Direct repository access from UI
* Large monolithic composable functions
