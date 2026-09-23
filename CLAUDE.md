# Project: MyCardholder (`my.passman`)

## Architecture

- Single Activity design pattern
- MVVM with unidirectional data flow (UI State + Events)
- Fragments, XML layouts and View Binding for the UI
- Hilt for dependency injection
- Navigation Architecture Component + SafeArgs for navigation

## Conventions

- ViewModels expose StateFlow<UiState>, never mutable state directly
- All fragment ViewModels inherit from BaseViewModel
- All fragments inherit from BaseFragment
- All views are stateless where possible

## Testing

- ViewModels: JUnit5 + Turbine for Flow testing + MockK
- Minimum 80% coverage on ViewModel and Repository layers
