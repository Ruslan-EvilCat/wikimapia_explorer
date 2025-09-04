package com.neonpurple.wikimapiaexplorer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neonpurple.wikimapiaexplorer.data.WikimapiaRepository
import com.neonpurple.wikimapiaexplorer.data.remote.PlaceDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaceUi(
    val id: Long,
    val title: String,
    val lat: Double,
    val lon: Double,
)

data class MapUiState(
    val radius: Int = 500,
    val isLoading: Boolean = false,
    val places: List<PlaceUi> = emptyList(),
    val error: String? = null,
    val lastRefreshAt: Long = 0L,
)

class MapViewModel(
    private val repo: WikimapiaRepository = WikimapiaRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun setRadius(newRadius: Int) {
        _uiState.value = _uiState.value.copy(radius = newRadius)
    }

    fun refreshNearby(lat: Double, lon: Double) {
        val now = System.currentTimeMillis()
        val cooldownMs = 1500L
        val state = _uiState.value
        if (state.isLoading) return
        if (now - state.lastRefreshAt < cooldownMs) return

        _uiState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val res = repo.getNearby(lat, lon, state.radius)
            _uiState.value = if (res.isSuccess) {
                val items = res.getOrNull().orEmpty()
                    .mapNotNull { it.toUi() }
                _uiState.value.copy(
                    isLoading = false,
                    places = items,
                    error = null,
                    lastRefreshAt = now
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = res.exceptionOrNull()?.message ?: "Unknown error",
                    lastRefreshAt = now
                )
            }
        }
    }
}

private fun PlaceDto.toUi(): PlaceUi? {
    val la = this.lat
    val lo = this.lon
    val title = this.title ?: "(untitled)"
    return if (la != null && lo != null) PlaceUi(id = id, title = title, lat = la, lon = lo) else null
}
