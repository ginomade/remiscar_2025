package com.nomade.movilremiscar.remiscarmovil

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nomade.movilremiscar.remiscarmovil.net.RetrofitService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReclamosViewModel : ViewModel() {
    private val state = MutableLiveData<Boolean>()

    var TAG = "ReclamosViewModel"

    fun getState(): LiveData<Boolean> {
        return state
    }

    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.w(TAG, "Exception handled: ${throwable.localizedMessage}")
    }

    fun enviarReclamo(
        email: String,
        mensaje: String,
        movil: String
    ) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(TAG, "Enviando Reclamo")
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.enviarReclamo(
                        email,
                        "",
                        mensaje,
                        movil
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful()) {
                            Log.w(TAG, "Response Reclamo: ${response.body()}")
                            val jsonResponse = response.body().toString()
                            state.postValue(true)
                        } else {
                            Log.w(TAG, "Response Reclamo: error")
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG,
                            "Response Reclamo: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })

            } catch (e: Exception) {
                Log.w(TAG, "Response Reclamo: ${e}")
            }
        }

    }
}

