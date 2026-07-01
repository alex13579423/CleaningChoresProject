package com.example.myapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.myapp.data.ChoreRepository
import com.example.myapp.data.Gender
import com.example.myapp.data.Person

class ChoreViewModel(private val repository: ChoreRepository) : ViewModel() {
    private val _people = mutableStateOf(repository.getPeople())
    val people: State<List<Person>> = _people

    private val _schedule = mutableStateOf(repository.getSchedule())
    val schedule: State<Map<String, Map<String, List<String>>>?> = _schedule

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

    fun generateSchedule() {
        val newSchedule = repository.generateWeek(_people.value)
        _schedule.value = newSchedule
        repository.saveSchedule(newSchedule)
    }

    fun updateSchedule(updated: Map<String, Map<String, List<String>>>) {
        _schedule.value = updated
        repository.saveSchedule(updated)
    }

    fun shareSchedule() {
        _schedule.value?.let { repository.shareSchedule(it) }
    }
}
