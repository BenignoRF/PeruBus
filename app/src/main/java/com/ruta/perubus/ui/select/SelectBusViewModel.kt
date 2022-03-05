package com.ruta.perubus.ui.select

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ruta.perubus.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class SelectBusViewModel constructor(private val repository: SelectBusRepository) : ViewModel() {

    val originList = MutableLiveData<List<Origen>>()
    val destinyList = MutableLiveData<List<Destino>>()
    val itinerarioList = MutableLiveData<List<Itinerario>>()
    val tarifaResult = MutableLiveData<List<Price>>()

    val errorMessage = MutableLiveData<String>()

    fun getAllOrigins() {
        val response = repository.getAllOrigins()
        response.enqueue(object : Callback<List<Origen>> {
            override fun onResponse(call: Call<List<Origen>>, response: Response<List<Origen>>) {
                originList.postValue(response.body())
            }

            override fun onFailure(call: Call<List<Origen>>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun getDestiny(origin: String) {
        val response = repository.getDestiniesByOrigin(origin)
        response.enqueue(object : Callback<List<Destino>> {
            override fun onResponse(call: Call<List<Destino>>, response: Response<List<Destino>>) {
                destinyList.postValue(response.body())
            }

            override fun onFailure(call: Call<List<Destino>>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun getItinerario(position: String, itinerario: ItinerarioInput) {
        val response = repository.getItinerario(position, itinerario)
        println("Posicion: $position")
        //val response = repository.getItinerario("-14.018002,-75.734469", itinerario)
        response.enqueue(object : Callback<List<Itinerario>> {
            override fun onResponse(
                call: Call<List<Itinerario>>,
                response: Response<List<Itinerario>>
            ) {
                itinerarioList.postValue(response.body())
            }

            override fun onFailure(call: Call<List<Itinerario>>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun getTarifa(currentItem: Bus, codRumbo: String, codigoOrigen: String, codigoDestino: String) {


        val response = repository.getTarifaRuta(
            codRumbo,
            formatDate(currentItem.fechaProg),
            currentItem.nuSecu,
            currentItem.codEmpresa,
            codigoOrigen,
            codigoDestino
        )

        /*

        val response = repository.getTarifaRuta(
            "SUR",
            "2021-12-10",
            "27",
            "01",
            "0",
            "8"
        ) */

        response.enqueue(object : Callback<List<Price>> {
            override fun onResponse(
                call: Call<List<Price>>,
                response: Response<List<Price>>
            ) {
                tarifaResult.postValue(response.body())
            }

            override fun onFailure(call: Call<List<Price>>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    private fun formatDate(fechaProg: String): String {
        val year = fechaProg.substring(6, fechaProg.length)
        val month = fechaProg.substring(3, 5)
        val day = fechaProg.substring(0, 2)
        return "$year-$month-$day"
    }

    fun getDate(date: Date?): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(date)
        return currentDate
    }

}