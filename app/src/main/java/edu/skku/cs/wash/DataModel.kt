package edu.skku.cs.wash

data class DataModel(
    var location: LocationState,
    var current: Weather
) {
    data class Weather(
        var temp_c: Double? = null,
        var wind_mph: Double? = null,
        var humidity: Double? = null
    )

    data class LocationState(
        var lat: Double? = null,
        var lon: Double? = null
    )
}
