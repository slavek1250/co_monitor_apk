package com.kala.co.co_monitor.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.kala.co.co_monitor.R
import com.kala.co.co_monitor.ValuesData
import kotlinx.android.synthetic.main.fragment_values.*
import kotlin.concurrent.timer


class ValuesFragment : Fragment() {

    val RELOAD_PARAM_PERIOD: Long = 30000

    private var valuesTxtList: List<Pair<String, TextView>>? = null
    private var valuesData: ValuesData = ValuesData {
        newDataCallback()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    lateinit var basicView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        basicView = inflater.inflate(R.layout.fragment_values, container, false)

        valuesData.fetchData()

        timer("dataUpdate", false, 1000, RELOAD_PARAM_PERIOD, {
            valuesData.fetchData()
        })

        basicView.post {
            valuesTxtList = listOf(
                Pair("ds2", valsMainTemp),
                Pair("exhaust", valsExhTemp),
                Pair("ds1", valsFdrTemp),
                Pair("ds4", valsOutdoorTemp),
                Pair("ds3", valsAddiTemp),
                Pair("distance", valsDist)
            )
        }

        return basicView
    }

    private fun newDataCallback() {
        basicView.post {

            valsTxtTime.text = valuesData.getTime()
            valsTxtDate.text = valuesData.getDate()

            if(valuesData.hasAnEventOccrured()) {
                valsTxtEvent.text = valuesData.getEventDescription()
                valsTxtOperation.text = valuesData.getOperationDescription()
                valsLayEvent.visibility = View.VISIBLE
            }
            else {
                valsLayEvent.visibility = View.GONE
            }

            updateValsView()
            valsTxtFeederProc.text = "%d".format(valuesData.getFeederPercentage()) + "%"
            valsFeederProgBar.progress = valuesData.getFeederPercentage()

            showValsLayout()
        }
    }

    private fun updateValsView() {
        for(i in 0 until valuesData.size()) {
            val name = valuesData.getNameAt(i)
            val newVal = valuesData.getValueAt(i)
            valuesTxtList!!
                .filter { it.first == name }
                .first()
                .second
                .text = newVal
        }
    }

    private fun showValsLayout() {
        valsProgBar.visibility = View.GONE
        valsLayDateTime.visibility = View.VISIBLE
        valsLayVals.visibility = View.VISIBLE
    }

    private fun showLoader() {
        valsLayDateTime.visibility = View.GONE
        valsLayVals.visibility = View.GONE
        valsProgBar.visibility = View.VISIBLE
    }

    companion object {
        @JvmStatic
        fun newInstance() = ValuesFragment()
    }
}
