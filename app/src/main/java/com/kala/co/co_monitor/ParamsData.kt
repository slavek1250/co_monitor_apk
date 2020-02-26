package com.kala.co.co_monitor

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ParamsData(val apiRespCallback: () -> Unit) {

    val API_URL = "http://co.slavek.webd.pro/api/get_curr_params.php?api_key=apk"
    val API_URL_SET = "http://co.slavek.webd.pro/api/set_new_params.php?api_key=apk"

    class SingleParam(
        val defId: Int,
        val name: String,
        val value: Int
    ) {
        override fun toString() : String {
            return "{\"defId\":$defId,\"name\":\"$name\",\"value\":$value}"
        }
    }

    enum class ResponseType {
        PARAM, NACK, ACK, NOT_NEWEST
    }

    private var paramId: Int? = null
    private var responseType: ResponseType =
        ResponseType.PARAM
    private var timestamp: Date? = null
    private var paramsList: List<SingleParam>? = listOf()
    private var paramsListNotSaved: ArrayList<SingleParam> = arrayListOf()

    fun fetchParams() {

        API_URL.httpGet().responseString { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
                }
                is Result.Success -> {
                    val data = result.get()
                    processApiResponse(data)
                }
            }
        }
    }

    fun commitNewParams() {
        val jsonString =
                "{\"oldParamId\":$paramId,\"newParams\":[" +
                paramsListNotSaved.joinToString { it.toString() } +
                "]}"

        Fuel.post(API_URL_SET)
            //.header(Headers.CONTENT_TYPE, "application/json")
            .body(jsonString)
            .response { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
                }
                is Result.Success -> {
                    val data = result.get().toString(Charsets.UTF_8)
                    processApiResponse(data)
                }
            }
        }
    }

    fun getApiResponseType() : ResponseType {
        return responseType
    }

    fun getTime() : String {
        return SimpleDateFormat("HH:mm").format(timestamp)
    }

    fun getDate() : String {
        return SimpleDateFormat("yyyy-MM-dd").format(timestamp)
    }

    fun getParamValue(name: String) : String {
        val currParam = paramsList!!.filter { it.name == name }.first()
        return "${currParam.value}"
    }

    fun getParamDefId(name: String) : Int {
        val currParam = paramsList!!.filter { it.name == name }.first()
        return currParam.defId
    }

    fun setParam(name: String, value: Int) {
        paramsListNotSaved.add(
            SingleParam(
                getParamDefId(name),
                name,
                value
            )
        )
    }

    private fun processApiResponse(responseData: String) {

        val klaxon = Klaxon()
        val parsed = klaxon.parseJsonObject(StringReader(responseData))

        when(parsed.string("type")) {
            "PARAM" -> {
                responseType = ResponseType.PARAM
            }
            "ACK" -> {
                responseType = ResponseType.ACK
                paramsList = paramsListNotSaved;
                paramsListNotSaved = arrayListOf()
            }
            "NACK" -> {
                responseType = ResponseType.NACK
                return
            }
            "NOT_NEWEST" -> {
                responseType = ResponseType.NOT_NEWEST
            }
        }

        paramId = parsed.int("paramId")!!

        val dateStr = parsed.string("timestamp")
        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr)
        if(responseType != ResponseType.ACK) {

            val dataArray = parsed.array<Any>("params")
            paramsList = dataArray?.let { klaxon.parseFromJsonArray(it) }
        }

        apiRespCallback?.invoke()
    }
}