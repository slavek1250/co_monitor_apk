package com.kala.co.co_monitor

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.io.StringReader

object CoNotificationData {

    private lateinit var apiRespCallback: () -> Unit
    private val API_URL = "http://co.slavek.webd.pro/api/get_curr_event.php?api_key=apk"

    private var handled = false
    private var eventId = 0
    private var eventDescription = ""
    private var operationDescription = ""

    fun fetchEvents() {
        API_URL.httpGet().responseString { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
                }
                is Result.Success -> {
                    val data = result.get()
                    processData(data)
                }
            }
        }
    }

    fun setApiResponseCallback(apiResponseCallback: () -> Unit) {
        apiRespCallback = apiResponseCallback
    }

    fun setAsHandled() {
        handled = true
    }

    fun hasAnEventOccurred() : Boolean {
        return eventId != 0
    }

    fun isEventHandled() : Boolean {
        return handled
    }

    fun getEventId() : Int {
        return eventId
    }

    fun getEventDescription() : String {
        return eventDescription
    }

    fun getOperationDescription() : String {
        return operationDescription
    }

    private fun processData(responseData: String) {
        val klaxon = Klaxon()
        val parsed = klaxon.parseJsonObject(StringReader(responseData))

        val newEventId = parsed.int("eventId")!!
        if(newEventId != eventId) {
            handled = false
            eventId = newEventId
        }
        if(eventId > 0) {
            eventDescription = parsed.string("eventDesctription")!!
            operationDescription = parsed.string("operationDescription")!!
        }

        apiRespCallback?.invoke()
    }
}