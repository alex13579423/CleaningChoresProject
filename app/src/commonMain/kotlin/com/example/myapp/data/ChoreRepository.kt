package com.example.myapp.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class ChoreRepository(
    private val storage: Storage,
    private val sharer: Sharer
) {
    private val json = Json { ignoreUnknownKeys = true }

    // Hardcoded strings for common logic to avoid R.string dependency in multiplatform logic
    private val HE_STRINGS = mapOf(
        "share_header" to "📋 סידור עבודה לשבוע הקרוב:\n\n",
        "share_day_format" to "*יום %s*:\n",
        "share_task_format" to "  - %s: %s\n",
        "share_missing" to "⚠️ חסר תורן!",
        "share_no_tasks" to "  (אין משימות היום)\n",
        "share_footer" to "\nבהצלחה לכולם! 💪"
    )

    fun savePeople(people: List<Person>) {
        storage.saveString("people", json.encodeToString(people))
    }

    fun getPeople(): List<Person> {
        val string = storage.getString("people") ?: return emptyList()
        return try {
            json.decodeFromString(string)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSchedule(schedule: Map<String, Map<String, List<String>>>) {
        storage.saveString("schedule", json.encodeToString(schedule))
    }

    fun getSchedule(): Map<String, Map<String, List<String>>>? {
        val string = storage.getString("schedule") ?: return null
        return try {
            json.decodeFromString(string)
        } catch (e: Exception) {
            null
        }
    }

    fun saveChores(chores: List<Chore>) {
        storage.saveString("chores", json.encodeToString(chores))
    }

    fun getChores(): List<Chore> {
        val string = storage.getString("chores") ?: return DEFAULT_CHORES
        return try {
            json.decodeFromString(string)
        } catch (e: Exception) {
            DEFAULT_CHORES
        }
    }

    fun savePriorityEnabled(enabled: Boolean) {
        storage.saveBoolean("priority_enabled", enabled)
    }

    fun isPriorityEnabled(): Boolean {
        return storage.getBoolean("priority_enabled", true)
    }

    fun generateWeek(people: List<Person>, chores: List<Chore>, usePriorities: Boolean = true): Map<String, Map<String, List<String>>> {
        val activePeople = people.filter { it.active }
        val activeChores = chores.filter { it.isActive }
        if (activePeople.isEmpty() || activeChores.isEmpty()) return emptyMap()

        val newSchedule = mutableMapOf<String, MutableMap<String, MutableList<String>>>()
        val totalAssignments = activePeople.associate { it.name to 0 }.toMutableMap()
        val taskHistory = activePeople.associate { it.name to mutableMapOf<String, Int>() }.toMutableMap()

        activePeople.forEach { p ->
            activeChores.forEach { chore -> taskHistory[p.name]!![chore.id] = 0 }
        }

        val activeDaysForLowPriority = activeChores.filter { 
            if (usePriorities) it.priority == Priority.LOW else false 
        }.associate { chore ->
            val numDays = (1..2).random()
            chore.id to DAY_KEYS.shuffled().take(numDays)
        }

        DAY_KEYS.forEach { day ->
            val daySchedule = mutableMapOf<String, MutableList<String>>()
            activeChores.forEach { daySchedule[it.id] = mutableListOf() }
            
            val available = activePeople.filter { day !in it.unavailableDays }.toMutableList()
            available.shuffle()

            fun pickPerson(pool: List<Person>, taskId: String): Person? {
                if (pool.isEmpty()) return null
                val selected = pool.sortedWith(compareBy(
                    { totalAssignments[it.name] ?: 0 },
                    { taskHistory[it.name]?.get(taskId) ?: 0 }
                )).first()
                
                totalAssignments[selected.name] = (totalAssignments[selected.name] ?: 0) + 1
                taskHistory[selected.name]!![taskId] = (taskHistory[selected.name]!![taskId] ?: 0) + 1
                return selected
            }

            // 1. Mandatory Gendered Tasks
            val malePool = available.filter { it.gender == Gender.MALE }
            if (activeChores.any { it.id == "toilet_m" }) {
                pickPerson(malePool, "toilet_m")?.let {
                    daySchedule["toilet_m"]!!.add(it.name)
                    available.remove(it)
                }
            }

            val femalePool = available.filter { it.gender == Gender.FEMALE }
            if (activeChores.any { it.id == "toilet_f" }) {
                pickPerson(femalePool, "toilet_f")?.let {
                    daySchedule["toilet_f"]!!.add(it.name)
                    available.remove(it)
                }
            }

            // 2. Primary Assignments
            activeChores.filter { it.id != "toilet_m" && it.id != "toilet_f" }.forEach { chore ->
                if (usePriorities && chore.priority == Priority.LOW && day !in (activeDaysForLowPriority[chore.id] ?: emptyList())) {
                    return@forEach
                }
                
                pickPerson(available, chore.id)?.let {
                    daySchedule[chore.id]!!.add(it.name)
                    available.remove(it)
                }
            }

            // 3. Redistribute Idle People
            while (available.isNotEmpty()) {
                var assignedInThisLoop = false
                val sortedChores = if (usePriorities) {
                    activeChores.sortedByDescending { it.priority }
                } else {
                    activeChores.shuffled()
                }

                for (chore in sortedChores) {
                    if (available.isEmpty()) break
                    
                    if (usePriorities && chore.priority == Priority.LOW && day !in (activeDaysForLowPriority[chore.id] ?: emptyList())) {
                        continue
                    }

                    val pool = when (chore.genderConstraint) {
                        Gender.MALE -> available.filter { it.gender == Gender.MALE }
                        Gender.FEMALE -> available.filter { it.gender == Gender.FEMALE }
                        null -> available
                    }

                    pickPerson(pool, chore.id)?.let {
                        daySchedule[chore.id]!!.add(it.name)
                        available.remove(it)
                        assignedInThisLoop = true
                    }
                }
                if (!assignedInThisLoop) break
            }
            newSchedule[day] = daySchedule
        }

        return newSchedule
    }

    fun shareSchedule(schedule: Map<String, Map<String, List<String>>>, chores: List<Chore>) {
        var text = HE_STRINGS["share_header"] ?: ""
        val activeChores = chores.filter { it.isActive }

        DAY_KEYS.forEach { dayKey ->
            text += (HE_STRINGS["share_day_format"] ?: "").format(DAYS_HE[dayKey] ?: "")
            val dayData = schedule[dayKey] ?: return@forEach
            var tasksFound = false

            activeChores.forEach { chore ->
                val assigned = dayData[chore.id]
                if (!assigned.isNullOrEmpty()) {
                    text += (HE_STRINGS["share_task_format"] ?: "").format(chore.label, assigned.joinToString(", "))
                    tasksFound = true
                } else if (chore.priority != Priority.LOW) {
                    text += (HE_STRINGS["share_task_format"] ?: "").format(chore.label, HE_STRINGS["share_missing"] ?: "")
                    tasksFound = true
                }
            }

            if (!tasksFound) text += HE_STRINGS["share_no_tasks"] ?: ""
            text += "\n"
        }

        text += HE_STRINGS["share_footer"] ?: ""
        sharer.shareSchedule(text)
    }
}
