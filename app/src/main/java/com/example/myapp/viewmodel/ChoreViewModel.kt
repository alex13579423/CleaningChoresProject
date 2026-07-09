package com.example.myapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.myapp.data.*

class ChoreViewModel(private val repository: ChoreRepository) : ViewModel() {
    private val _people = mutableStateOf(repository.getPeople())
    val people: State<List<Person>> = _people

    private val _chores = mutableStateOf(repository.getChores())
    val chores: State<List<Chore>> = _chores

    private val _priorityEnabled = mutableStateOf(repository.isPriorityEnabled())
    val priorityEnabled: State<Boolean> = _priorityEnabled

    private val _schedule = mutableStateOf(repository.getSchedule())
    val schedule: State<Map<String, Map<String, List<String>>>?> = _schedule

    private val _userRole = mutableStateOf(repository.getUserRole())
    val userRole: State<UserRole?> = _userRole

    private val _isDarkMode = mutableStateOf(repository.isDarkMode())
    val isDarkMode: State<Boolean> = _isDarkMode

    fun setUserRole(role: UserRole?) {
        _userRole.value = role
        repository.saveUserRole(role)
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        repository.saveDarkMode(enabled)
    }

    fun getSyncJson(): String = repository.getSyncJson()

    fun applySyncJson(json: String) {
        if (repository.applySyncJson(json)) {
            _people.value = repository.getPeople()
            _chores.value = repository.getChores()
            _schedule.value = repository.getSchedule()
            _priorityEnabled.value = repository.isPriorityEnabled()
        }
    }

    fun addPerson(name: String, gender: Gender, unavailableDays: List<String> = emptyList()) {
        val newPerson = Person(
            id = System.currentTimeMillis(), 
            name = name, 
            gender = gender,
            unavailableDays = unavailableDays
        )
        val newList = _people.value + newPerson
        updatePeople(newList)
    }

    fun deletePerson(person: Person) {
        val newList = _people.value.filter { it.id != person.id }
        updatePeople(newList)
    }

    fun updatePerson(updated: Person) {
        val newList = _people.value.map { if (it.id == updated.id) updated else it }
        updatePeople(newList)
    }

    private fun updatePeople(newList: List<Person>) {
        _people.value = newList
        repository.savePeople(newList)
    }

    fun updateChore(updated: Chore) {
        val newList = _chores.value.map { if (it.id == updated.id) updated else it }
        _chores.value = newList
        repository.saveChores(newList)
    }

    fun togglePriorityEnabled(enabled: Boolean) {
        _priorityEnabled.value = enabled
        repository.savePriorityEnabled(enabled)
    }

    fun generateSchedule() {
        val newSchedule = repository.generateWeek(_people.value, _chores.value, _priorityEnabled.value)
        _schedule.value = newSchedule
        repository.saveSchedule(newSchedule)
    }

    fun updateSchedule(updated: Map<String, Map<String, List<String>>>) {
        _schedule.value = updated
        repository.saveSchedule(updated)
    }

    fun shareSchedule() {
        _schedule.value?.let { repository.shareSchedule(it, _chores.value) }
    }

    fun getWorkloadStats(): Map<String, Float> {
        val currentSchedule = _schedule.value ?: return emptyMap()
        val currentChores = _chores.value
        val usePriorities = _priorityEnabled.value
        val stats = mutableMapOf<String, Int>()
        var totalPoints = 0

        currentSchedule.values.forEach { dayTasks ->
            dayTasks.forEach { (choreId, assignedNames) ->
                val chore = currentChores.find { it.id == choreId } ?: return@forEach
                val points = if (usePriorities) {
                    when (chore.priority) {
                        Priority.HIGH -> 3
                        Priority.MEDIUM -> 2
                        Priority.LOW -> 1
                    }
                } else {
                    1
                }
                assignedNames.forEach { name ->
                    stats[name] = (stats[name] ?: 0) + points
                    totalPoints += points
                }
            }
        }

        if (totalPoints == 0) return emptyMap()
        return stats.mapValues { it.value.toFloat() / totalPoints }
    }

    fun setAllPriorities(priority: Priority) {
        val newList = _chores.value.map { it.copy(priority = priority) }
        _chores.value = newList
        repository.saveChores(newList)
    }

    fun addCustomChore(label: String, priority: Priority) {
        val newChore = Chore(
            id = "custom_${System.currentTimeMillis()}",
            label = label,
            priority = priority,
            isActive = true
        )
        val newList = _chores.value + newChore
        _chores.value = newList
        repository.saveChores(newList)
    }

    fun deleteChore(chore: Chore) {
        val newList = _chores.value.filter { it.id != chore.id }
        _chores.value = newList
        repository.saveChores(newList)
    }
}
