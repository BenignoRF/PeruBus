package com.ruta.perubus.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.ruta.perubus.R
import com.ruta.perubus.databinding.ActivityMapsBinding
import com.ruta.perubus.models.Markers


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var markers: Markers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        markers = (intent.getSerializableExtra("markers") as? Markers)!!

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val codbus: TextView = findViewById(R.id.codBus)
        val fechaProg: TextView = findViewById(R.id.fechaProg)
        val tipoServicio: TextView = findViewById(R.id.tipoServicio)
        val duracion: TextView = findViewById(R.id.duracion)
        val distancia: TextView = findViewById(R.id.distancia)

        codbus.text = markers.codbus
        fechaProg.text = markers.fechaProg
        tipoServicio.text = markers.tipoServicio
        duracion.text = markers.duracion
        distancia.text = markers.distancia

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        val user = LatLng(
            markers.currentLatitude.toDouble(),
            markers.currentLongitude.toDouble()
        )

        val bus = LatLng(
            markers.busLatitude.toDouble(),
            markers.busLongitude.toDouble()
        )

        val builder = LatLngBounds.Builder()
        builder.include(user)
        builder.include(bus)



        mMap.addMarker(MarkerOptions().position(bus).title("Bus"))
        mMap.addMarker(MarkerOptions().position(user).title("Usuario"))

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))

    }

}