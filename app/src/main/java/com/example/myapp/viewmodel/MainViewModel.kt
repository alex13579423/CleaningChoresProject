package com.example.myapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.myapp.data.Person

class MainViewModel : ViewModel() {
    private val _selectedTab = mutableIntStateOf(0)
    val selectedTab: State<Int> = _selectedTab

    private val _isFabExpanded = mutableStateOf(false)
    val isFabExpanded: State<Boolean> = _isFabExpanded

    private val _showPersonForm = mutableStateOf(false)
    val showPersonForm: State<Boolean> = _showPersonForm

    private val _showChoreForm = mutableStateOf(false)
    val showChoreForm: State<Boolean> = _showChoreForm

    private val _editingPerson = mutableStateOf<Person?>(null)
    val editingPerson: State<Person?> = _editingPerson

    private val _showNfcSharing = mutableStateOf(false)
    val showNfcSharing: State<Boolean> = _showNfcSharing

    private val _showNfcScanner = mutableStateOf(false)
    val showNfcScanner: State<Boolean> = _showNfcScanner

    private val _showQrSharing = mutableStateOf(false)
    val showQrSharing: State<Boolean> = _showQrSharing

    private val _showQrScanner = mutableStateOf(false)
    val showQrScanner: State<Boolean> = _showQrScanner

    fun setSelectedTab(index: Int) {
        _selectedTab.intValue = index
        _isFabExpanded.value = false
    }

    fun setFabExpanded(expanded: Boolean) {
        _isFabExpanded.value = expanded
    }

    fun setShowPersonForm(show: Boolean, person: Person? = null) {
        _editingPerson.value = person
        _showPersonForm.value = show
        if (show) _isFabExpanded.value = false
    }

    fun setShowChoreForm(show: Boolean) {
        _showChoreForm.value = show
        if (show) _isFabExpanded.value = false
    }

    fun setShowNfcSharing(show: Boolean) {
        _showNfcSharing.value = show
    }

    fun setShowNfcScanner(show: Boolean) {
        _showNfcScanner.value = show
    }

    fun setShowQrSharing(show: Boolean) {
        _showQrSharing.value = show
    }

    fun setShowQrScanner(show: Boolean) {
        _showQrScanner.value = show
    }
}
