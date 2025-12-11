package pt.ismai.lastfmlogin.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle

@Composable
fun MapScreen() {
    MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState = rememberMapViewportState {
            setCameraOptions {
                zoom(16.4)
                center(Point.fromLngLat(-8.61652, 41.26942))
                pitch(0.0)
                bearing(0.0)
            }
        },
        style = { MapStyle(style = "mapbox://styles/mapbox/dark-v11") }
    )
}