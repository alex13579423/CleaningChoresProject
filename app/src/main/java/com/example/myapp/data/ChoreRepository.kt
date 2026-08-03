package com.example.myapp.data

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.myapp.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class ChoreRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("duty_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        @Volatile
        private var INSTANCE: ChoreRepository? = null

        fun getInstance(context: Context): ChoreRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChoreRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun savePeople(people: List<Person>) {
        prefs.edit().putString("people", gson.toJson(people)).apply()
    }

    fun getPeople(): List<Person> {
        val json = prefs.getString("people", null) ?: return emptyList()
        val type = object : TypeToken<List<Person>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveSchedule(schedule: Map<String, Map<String, List<String>>>) {
        prefs.edit { putString("schedule", gson.toJson(schedule)) }
    }

    fun getSchedule(): Map<String, Map<String, List<String>>>? {
        val json = prefs.getString("schedule", null) ?: return null
        val type = object : TypeToken<Map<String, Map<String, List<String>>>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveChores(chores: List<Chore>) {
        prefs.edit { putString("chores", gson.toJson(chores)) }
    }

    fun getChores(): List<Chore> {
        val json = prefs.getString("chores", null) ?: return DEFAULT_CHORES
        val type = object : TypeToken<List<Chore>>() {}.type
        return gson.fromJson(json, type)
    }

    fun savePriorityEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("priority_enabled", enabled) }
    }

    fun isPriorityEnabled(): Boolean {
        return prefs.getBoolean("priority_enabled", true)
    }

    fun saveUserRole(role: UserRole?) {
        prefs.edit { putString("user_role", role?.name) }
    }

    fun getUserRole(): UserRole? {
        val roleName = prefs.getString("user_role", null) ?: return null
        return try { UserRole.valueOf(roleName) } catch (e: Exception) { null }
    }

    fun saveDarkMode(enabled: Boolean) {
        prefs.edit { putBoolean("dark_mode", enabled) }
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean("dark_mode", true)
    }

    fun importSyncData(data: SyncData) {
        savePeople(data.people)
        saveChores(data.chores)
        saveSchedule(data.schedule ?: emptyMap())
        savePriorityEnabled(data.priorityEnabled)
    }

    fun exportSyncData(): SyncData {
        return SyncData(
            people = getPeople(),
            chores = getChores(),
            schedule = getSchedule(),
            priorityEnabled = isPriorityEnabled(),
        )
    }

    fun generateWeek(people: List<Person>, chores: List<Chore>, usePriorities: Boolean = true): Map<String, Map<String, List<String>>> {
        val activePeople = people.filter { it.active }
        val activeChores = chores.filter { it.isActive }
        if (activePeople.isEmpty() || activeChores.isEmpty()) return emptyMap()

        val newSchedule = mutableMapOf<String, MutableMap<String, MutableList<String>>>()
        val totalAssignments = activePeople.associate { it.name to 0 }.toMutableMap()
        val taskHistory = activePeople.associate { it.name to mutableMapOf<String, Int>() }.toMutableMap()

        activePeople.forEach { p ->
            activeChores.forEach { chore -> taskHistory[p.name]?.set(chore.id, 0) }
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
                taskHistory[selected.name]?.let { history ->
                    history[taskId] = (history[taskId] ?: 0) + 1
                }
                return selected
            }

            val malePool = available.filter { it.gender == Gender.MALE }
            if (activeChores.any { it.id == "toilet_m" }) {
                pickPerson(malePool, "toilet_m")?.let {
                    daySchedule["toilet_m"]?.add(it.name)
                    available.remove(it)
                }
            }

            val femalePool = available.filter { it.gender == Gender.FEMALE }
            if (activeChores.any { it.id == "toilet_f" }) {
                pickPerson(femalePool, "toilet_f")?.let {
                    daySchedule["toilet_f"]?.add(it.name)
                    available.remove(it)
                }
            }

            activeChores.filter { it.id != "toilet_m" && it.id != "toilet_f" }.forEach { chore ->
                if (usePriorities && chore.priority == Priority.LOW && day !in (activeDaysForLowPriority[chore.id] ?: emptyList())) {
                    return@forEach
                }
                
                pickPerson(available, chore.id)?.let {
                    daySchedule[chore.id]?.add(it.name)
                    available.remove(it)
                }
            }

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
                        daySchedule[chore.id]?.add(it.name)
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
        var text = context.getString(R.string.share_header)
        val activeChores = chores.filter { it.isActive }

        DAY_KEYS.forEach { dayKey ->
            text += context.getString(R.string.share_day_format, DAYS_HE[dayKey])
            val dayData = schedule[dayKey] ?: return@forEach
            var tasksFound = false

            activeChores.forEach { chore ->
                val assigned = dayData[chore.id]
                if (!assigned.isNullOrEmpty()) {
                    text += context.getString(R.string.share_task_format, chore.label, assigned.joinToString(", "))
                    tasksFound = true
                } else if (chore.priority != Priority.LOW) {
                    text += context.getString(R.string.share_task_format, chore.label, context.getString(R.string.share_missing))
                    tasksFound = true
                }
            }

            if (!tasksFound) text += context.getString(R.string.share_no_tasks)
            text += "\n"
        }

        text += context.getString(R.string.share_footer)

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.share_chooser_title))
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun getSyncJson(): String {
        return gson.toJson(exportSyncData())
    }

        fun applySyncJson(json: String): Boolean {
        return try {
            Log.d("ChoreRepository", "Applying Sync JSON: $json")
            val data = gson.fromJson(json, SyncData::class.java)
            if (data == null) {
                Log.e("ChoreRepository", "Parsed data is null")
                return false
            }
            importSyncData(data)
            Log.i("ChoreRepository", "Successfully imported sync data")
            true
        } catch (e: Exception) {
            Log.e("ChoreRepository", "Failed to parse Sync JSON", e)
            false
        }
    }
}
