package com.kala.co.co_monitor

import com.beust.klaxon.*
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.*
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*


class ValuesData(val newDataCallback: () -> Unit) {

    val API_URL = "http://co.slavek.webd.pro/api/get_curr_measures.php?api_key=apk"

    class SingleValue(
        val name: String,
        val description : String,
        val value: Double,
        val unit: String,
        val setValue: Int? = null
    )

    class EventData (
        val eventId: Int? = null,
        val eventDesctription: String? = null,
        val operationDescription: String? = null
    )

    private var timestamp: Date? = null
    private var measurementId: Int? = null

    private var valuesList: List<SingleValue>? = null
    private var eventData: EventData? = null



    fun fetchData() {
        //https://ryanharrison.co.uk/2018/06/15/make-http-requests-kotlin.html

        API_URL.httpGet().responseString { request, response, result ->
            when (result) {

                is Result.Failure -> {
                    val ex = result.getException()
                }
                is Result.Success -> {
                    val data = result.get()as String
                    processData(data)
                }
            }
        }
    }

    private fun processData(responseData: String) {
        // https://github.com/cbeust/klaxon
        // https://stackoverflow.com/questions/50218511/how-to-parse-just-part-of-json-with-klaxon

        val klaxon = Klaxon()
        val parsed = klaxon.parseJsonObject(StringReader(responseData))
        val dataArray = parsed.array<Any>("values")

        measurementId = parsed.int("measurementId")
        val dateStr = parsed.string("timestamp")
        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr)

        measurementId = parsed.int("co_measurement_id")
        valuesList = dataArray?.let { klaxon.parseFromJsonArray(it) }

        val eventObj = parsed.obj("event")
        eventData = eventObj?.let { klaxon.parseFromJsonObject(it) }

        newDataCallback?.invoke()
    }

    fun getDescriptionAt(id: Int) : String{
        return valuesList!![id].description
    }

    fun getNameAt(id: Int) : String {
        return valuesList!![id].name
    }

    fun getValueAt(id: Int) : String {
        return "%.2f".format(valuesList!![id].value) + valuesList!![id].setValue?.let { " / $it" }.orEmpty()
    }

    fun getUnitAt(id: Int) : String {
        return valuesList!![id].unit
    }

    fun hasAnEventOccrured() : Boolean {
        return eventData != null
    }

    fun getEventDescription() : String? {
        return eventData?.eventDesctription
    }

    fun getOperationDescription() : String? {
        return eventData?.operationDescription
    }

    fun getTime() : String {
        return SimpleDateFormat("HH:mm").format(timestamp)
    }

    fun getDate() : String {
        return SimpleDateFormat("yyyy-MM-dd").format(timestamp)
    }

    fun getFeederPercentage() : Int {
        val feederData = valuesList!!.filter { it.name == "distance" }.first()
        if(feederData == null) return 0;
        return ((1.0 - (feederData.value / feederData.setValue!!)) * 100 + 0.5)
            .toInt()
            .let { if(it > 100) 100 else it }
            .let { if(it < 0) 0 else it }
    }

    fun size() : Int {
        return valuesList!!.size
    }
}