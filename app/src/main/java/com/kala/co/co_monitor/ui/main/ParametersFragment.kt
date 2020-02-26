package com.kala.co.co_monitor.ui.main

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import com.kala.co.co_monitor.ParamsData

import com.kala.co.co_monitor.R
import kotlinx.android.synthetic.main.fragment_parameters.*


class ParametersFragment : Fragment() {

    private var paramsEditList: List<Pair<String, EditText>>? = null
    private val paramsData: ParamsData = ParamsData {
        handleApiResponse()
    }
    lateinit var basicView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        basicView = inflater.inflate(R.layout.fragment_parameters, container, false)

        paramsData.fetchParams()

        basicView.post {
            paramsEditList = listOf(
                Pair("set_temp", paramEdtMainTemp),
                Pair("feeder_break", paramEdtFdrBrk),
                Pair("feeder_revo_num", paramEdtRPC),
                Pair("hist", paramEdtHist),
                Pair("fmt", paramEdtMaxTempFdr),
                Pair("fmd", paramEdtMaxDist)

            )
            paramBtnCancel.setOnClickListener { reloadParams() }
            paramBtnSave.setOnClickListener { saveBtnHandler() }
        }
        return basicView
    }

    private fun handleApiResponse() {
        when(paramsData.getApiResponseType()) {
            ParamsData.ResponseType.PARAM -> loadParams()
            ParamsData.ResponseType.ACK -> paramsSavedConfirmation()
            ParamsData.ResponseType.NACK -> notSaved()
            ParamsData.ResponseType.NOT_NEWEST -> paramsNotNewest()
        }
    }

    private fun notSaved() {
        Snackbar.make(
            paramBtnSave,
            "Niestety, zapis się nie powiódł. Kliknij ANULUJ by załadować poprzednie parametry, lub zapisz by spróbować ponownie.",
            3000)
            .show()
    }

    private fun loadParams() {
        basicView.post {
            paramTxtTime.text = paramsData.getTime()
            paramTxtDate.text = paramsData.getDate()
            paramsEditList?.forEach {
                it.second.setText(paramsData.getParamValue(it.first))
            }
            showParams()
        }
    }

    private fun paramsSavedConfirmation() {
        basicView.post {
            paramTxtTime.text = paramsData.getTime()
            paramTxtDate.text = paramsData.getDate()
        }
        Snackbar.make(paramBtnSave, "Pomyślnie zapisano nowe parametry.", 3000).show()
    }

    private fun paramsNotNewest() {
        basicView.post {
            val dialogBuilder = AlertDialog.Builder(this.context)

            dialogBuilder.setMessage("Edytowane parametry nie są najnowszymi, wycofywanie zmian.")
                .setCancelable(false)
                .setPositiveButton("Ok", DialogInterface.OnClickListener { _, _ ->
                    reloadParams()
                })

            val alert = dialogBuilder.create()
            alert.setTitle("Błąd")
            alert.show()
        }
    }

    private fun reloadParams() {
        loadParams()
        Snackbar.make(paramBtnCancel, "Przeładowano parametry.", 3000).show()
    }

    private fun showParams() {
        paramProgBar.visibility = View.GONE
        paramLayTime.visibility = View.VISIBLE
        paramLayParams.visibility = View.VISIBLE
    }

    private fun saveBtnHandler() {

        var valuesHasChanged  = false
        paramsEditList?.forEach {
            valuesHasChanged =
                valuesHasChanged or (
                        it.second.text.toString() != paramsData.getParamValue(it.first))
        }
        if(!valuesHasChanged) return

        val dialogBuilder = AlertDialog.Builder(this.context)

        dialogBuilder.setMessage("Czy na pewno chcesz zapisać nowe parametry?")
            .setCancelable(false)
            .setPositiveButton("Tak", DialogInterface.OnClickListener {
                    _, _ -> saveNewParams()
            })
            .setNegativeButton("Nie", DialogInterface.OnClickListener {
                    dialog, _ -> dialog.cancel()
            })

        val alert = dialogBuilder.create()
        alert.setTitle("Potwierdź")
        alert.show()
    }

    private fun saveNewParams() {
        paramsEditList?.forEach {
            paramsData.setParam(it.first, it.second.text.toString().toInt())
        }
        paramsData.commitNewParams()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ParametersFragment()
    }
}
