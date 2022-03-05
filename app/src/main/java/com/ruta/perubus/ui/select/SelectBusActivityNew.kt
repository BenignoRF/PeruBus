package com.ruta.perubus.ui.select

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ruta.perubus.R
import com.ruta.perubus.api.Api
import com.ruta.perubus.api.RetrofitClient
import com.ruta.perubus.databinding.ActivitySelectbusBinding
import com.ruta.perubus.helper.Constants
import com.ruta.perubus.models.*
import com.ruta.perubus.ui.MapsActivity
import com.ruta.perubus.ui.agencias.AgenciasActivity
import com.ruta.perubus.ui.select.adapter.RecyclerAdapter
import com.ruta.perubus.ui.select.listener.IBusItemListener
import java.util.*

class SelectBusActivityNew : AppCompatActivity(), IBusItemListener {

    private lateinit var binding: ActivitySelectbusBinding
    lateinit var viewModel: SelectBusViewModel
    val PERMISSION_ID = 42
    private lateinit var userArrayList: ArrayList<Bus>
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var currentLatitude: String
    lateinit var currentLongitude: String
    lateinit var currentItem: Bus


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selectbus)
        binding = ActivitySelectbusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val api = RetrofitClient.buildService(Api::class.java)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel =
            ViewModelProvider(this, SelectBusViewModelFactory(SelectBusRepository(api))).get(
                SelectBusViewModel::class.java
            )

        viewModel.originList.observe(this, Observer {

            val positionAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, it).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.SpinnerOrigen.adapter = adapter

                    binding.SpinnerOrigen.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {


                            var origin = binding.SpinnerOrigen.selectedItem as Origen
                            binding.origenText.setText(origin.descripcionOrigen)

                            viewModel.getDestiny(origin.codigoOrigen)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            TODO("Not yet implemented")
                        }

                    }
                }

        })

        viewModel.destinyList.observe(this, Observer {

            val positionAdapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, it).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.SpinnerDestino.adapter = adapter

                    binding.SpinnerDestino.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {


                            var destino = binding.SpinnerDestino.selectedItem as Destino
                            binding.Destinotxt.setText(destino.descripcionOrigen)

                            getLastLocation()

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            TODO("Not yet implemented")
                        }

                    }
                }

        })

        viewModel.itinerarioList.observe(this, Observer {

            userArrayList = ArrayList()
            if (it != null) {
                for (itinerario in it) {
                    val bus =
                        Bus(
                            itinerario.fechaProg,
                            itinerario.codBus,
                            itinerario.tipoServicio,
                            itinerario.map3,
                            itinerario.distancia,
                            itinerario.duracion,
                            itinerario.latitude,
                            itinerario.longitude,
                            itinerario.nuSecu.toString(),
                            itinerario.codEmpresa
                        )
                    userArrayList.add(bus)
                }

                binding.listview.adapter = RecyclerAdapter(userArrayList, this)
                binding.listview.layoutManager = LinearLayoutManager(this)
                binding.listview.setHasFixedSize(true)

            }

        })

        viewModel.tarifaResult.observe(this, Observer {
            val intent = Intent(this, MapsActivity::class.java)

            intent.putExtra(
                "markers",
                Markers(
                    currentLatitude,
                    currentLongitude,
                    currentItem.latitude,
                    currentItem.longitude,
                    currentItem.codBus,
                    currentItem.fechaProg,
                    currentItem.duracion,
                    currentItem.tipoServicio,
                    currentItem.distancia,
                    if (it.size > 0) it[0].precio else 0
                )
            )
            startActivity(intent)
        })

        viewModel.getAllOrigins()

        binding.fabAgencias.setOnClickListener {
            val intent = Intent(this, AgenciasActivity::class.java)
            intent.putExtra(
                "markers",
                Markers(
                    currentLatitude,
                    currentLongitude,
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    0
                )
            )
            startActivity(intent)

        }


    }

    private fun checkPermissions(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result

                    currentLatitude = location?.latitude.toString()
                    currentLongitude = location?.longitude.toString()

                    viewModel.getItinerario(
                        location?.latitude.toString() + "," + location?.longitude.toString(),
                        ItinerarioInput(
                            viewModel.getDate(Date()),
                            (binding.SpinnerDestino.selectedItem as Destino).codRumbo,
                            (binding.SpinnerOrigen.selectedItem as Origen).codigoOrigen,
                            (binding.SpinnerDestino.selectedItem as Destino).codigoDestino
                        )
                    )

                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }


    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    override fun onBusItemClickListener(currentItem: Bus) {


        if (currentItem.latitude == Constants.NO_DATA
            || currentItem.longitude == Constants.NO_DATA
            || currentItem.longitude.isEmpty()
            || currentItem.latitude.isEmpty()
        ) {
            Toast.makeText(this, Constants.NO_POSITION_MESSAGE, Toast.LENGTH_LONG).show()
        } else {
            viewModel.getTarifa(
                currentItem,
                (binding.SpinnerDestino.selectedItem as Destino).codRumbo,
                (binding.SpinnerOrigen.selectedItem as Origen).codigoOrigen,
                (binding.SpinnerDestino.selectedItem as Destino).codigoDestino
            )

            this.currentItem = currentItem
        }


    }


}
