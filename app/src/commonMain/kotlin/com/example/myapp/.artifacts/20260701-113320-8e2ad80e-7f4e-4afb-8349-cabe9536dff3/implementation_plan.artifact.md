# Implementation Plan - Enhanced Chore Management & Statistics

This plan incorporates chore priority toggling, custom chore creation, uniform priority settings, and a dedicated workload distribution statistics screen.

## Proposed Changes

### Data Model & Persistence
- **[Models.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/data/Models.kt)**: Ensure `Chore` supports custom IDs and `isActive` flag.
- **[ChoreRepository.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/data/ChoreRepository.kt)**: Methods for saving/loading custom chores and calculating workload statistics based on the current schedule.

### Logic & State
- **[ChoreViewModel.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/viewmodel/ChoreViewModel.kt)**:
    - `addCustomChore(label: String, priority: Priority)`
    - `deleteChore(chore: Chore)`
    - `setAllPriorities(priority: Priority)`
    - `workloadStats`: A derived state that calculates the percentage of work done by each person, weighted by chore priority.

### UI Enhancements
- **[ChoreFormBottomSheet.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/ui/screens/ChoreFormBottomSheet.kt)** (NEW): A bottom sheet to add/edit custom chores.
- **[StatsScreen.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/ui/screens/StatsScreen.kt)** (NEW): A dedicated screen for workload statistics, accessible via a new navigation tab.
- **[WorkloadGraph.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/ui/components/WorkloadGraph.kt)** (NEW): A visual bar graph showing workload distribution among users.
- **[SettingsScreen.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/ui/screens/SettingsScreen.kt)**:
    - Add "Add Chore" button.
    - Add "Set All to Medium" button.
- **[MainActivity.kt](file:///C:/Users/itay1/Downloads/CleaningChores_AndroidProject/CleaningChoresProject/app/src/main/java/com/example/myapp/MainActivity.kt)**:
    - Add a third navigation tab for "Statistics".
    - Handle switching to the `StatsScreen`.

## Workload Calculation Formula
A person's "Workload Score" is the sum of the priority levels of all chores assigned to them in the weekly schedule:
- **High**: 3 points
- **Medium**: 2 points
- **Low**: 1 point

**Percentage** = (Person Score / Total Score of all assignments) * 100.

## Verification Plan

### Manual Verification
1.  **Navigation**: Verify there are now three tabs: Schedule, Stats, and Settings.
2.  **Custom Chores**: Add a chore named "Water Plants" with High priority. Verify it appears in the list and schedule.
3.  **Uniform Priority**: Click "Set All to Medium". Verify all active chores switch to Medium priority.
4.  **Workload Graph (Stats Tab)**:
    - Assign multiple High tasks to one person and Low tasks to another.
    - Verify the graph shows the first person with a significantly higher percentage.
    - Verify the percentages add up to approximately 100%.
5.  **Persistence**: Ensure custom chores and graph data persist across app restarts.
