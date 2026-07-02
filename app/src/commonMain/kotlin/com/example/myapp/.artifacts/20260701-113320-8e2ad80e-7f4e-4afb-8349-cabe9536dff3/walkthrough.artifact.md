# Walkthrough - Enhanced Chore Management & Statistics

I have implemented a comprehensive update that includes custom chores, uniform priority settings, and a dedicated workload distribution statistics screen.

## New Features

### 1. Statistics Screen & Workload Graph
- **[StatsScreen.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/ui/screens/StatsScreen.kt)**: A new tab in the navigation bar dedicated to visualizing work distribution.
- **[WorkloadGraph.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/ui/components/WorkloadGraph.kt)**: A dynamic bar chart that shows the percentage of workload for each person.
- **Workload Calculation**:
    - **Hard Task**: 3 points
    - **Medium Task**: 2 points
    - **Easy Task**: 1 point
    - The percentage is calculated as: `(Person Points / Total Points) * 100`.

### 2. Custom Chores
- **[ChoreFormBottomSheet.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/ui/screens/ChoreFormBottomSheet.kt)**: Added a new form to create custom chores with specific names and initial priorities.
- You can now add as many chores as you need from the Settings tab.

### 3. Uniform Priority Settings
- **[SettingsScreen.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/ui/screens/SettingsScreen.kt)**: Added "All Medium" and "All Hard" buttons to quickly standardize the difficulty of all active chores.

### 4. Navigation Update
- **[MainActivity.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/MainActivity.kt)**: The bottom navigation bar now has three tabs:
    - **Schedule**: View and edit the weekly assignments.
    - **Stats**: View the workload distribution graph.
    - **Settings**: Manage people and chore configurations.

## Verification
- **Persistence**: All custom chores and priority changes are saved to `SharedPreferences`.
- **Logic**: The workload calculation correctly weights tasks based on their priority level.
- **UI**: The navigation bar correctly switches between the three screens.
