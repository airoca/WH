package edu.skku.cs.wash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import edu.skku.cs.wash.databinding.FragmentSelectBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class SelectFragment : Fragment() {

    private var _binding: FragmentSelectBinding? = null
    private val binding get() = _binding!!
    private val client = OkHttpClient()
    private val host = "https://api.weatherapi.com/v1/current.json"
    private val apiKey = "93d39797e98c4b38ac471328242105"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectBinding.inflate(inflater, container, false)
        val root: View = binding.root
        var allNum = 0
        var availableNum = 0

        // Weather API 요청
        val path = "?key=$apiKey&q=Suwon"
        val req = Request.Builder().url(host + path).build()

        client.newCall(req).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val data = response.body()?.string()
                    val dataModel = Gson().fromJson(data, DataModel::class.java)
                    val tempC = dataModel.current.temp_c
                    val windMph = dataModel.current.wind_mph
                    val humidity = dataModel.current.humidity

                    CoroutineScope(Dispatchers.Main).launch {
                        root.findViewById<TextView>(R.id.temperatureTextView).text = "온도: $tempC°C"
                        root.findViewById<ProgressBar>(R.id.temperatureProgressBar).progress = (tempC ?: 0.0).toInt()

                        root.findViewById<TextView>(R.id.humidityTextView).text = "습도: $humidity%"
                        root.findViewById<ProgressBar>(R.id.humidityProgressBar).progress = (humidity ?: 0.0).toInt()

                        root.findViewById<TextView>(R.id.windSpeedTextView).text = "풍속: $windMph mph"
                        root.findViewById<ProgressBar>(R.id.windSpeedProgressBar).progress = (windMph ?: 0.0).toInt()
                    }
                }
            }
        })

        val navigateActivity = activity as? NavigateActivity

        val call: Call<List<Washer>> = when (publicDorm) {
            "사랑관" -> RetrofitClient.instance.getWashersDorm1()
            "소망관" -> RetrofitClient.instance.getWashersDorm2()
            "아름관" -> RetrofitClient.instance.getWashersDorm3()
            "나래관" -> RetrofitClient.instance.getWashersDorm4()
            else -> RetrofitClient.instance.getWashers()
        }

        Log.d("Select publicDorm", publicDorm)
        call.enqueue(object : retrofit2.Callback<List<Washer>> {
            override fun onResponse(call: Call<List<Washer>>, response: Response<List<Washer>>) {
                if (response.isSuccessful) {
                    Log.d("WasherList", response.body().toString())
                    val fetchedList = response.body()
                    fetchedList?.forEach {
                        allNum += 1
                        if (it.washerstatus == "사용 가능") {
                            availableNum += 1
                        }

                        Log.d("num", "( ${availableNum} / ${allNum} )")
                    }
                    Log.d("num", "( ${availableNum} / ${allNum} )")
                    val howmany = root.findViewById<TextView>(R.id.howManyAvailableWasher)
                    howmany.text = "( ${availableNum} / ${allNum} )"
                } else {
                }
            }

            override fun onFailure(call: Call<List<Washer>>, t: Throwable) {
                Log.e("WasherList", "Failed: ${t.message}")
            }
        })

        val buttonWasher = root.findViewById<CardView>(R.id.select_washer)

        buttonWasher.setOnClickListener {
            val intent = Intent(requireContext(), WashersActivity::class.java)
            val toolbardormText = navigateActivity?.getToolbarDormText()
            val receivedIntent = activity?.intent
            val userid = receivedIntent?.getStringExtra("userid")
            intent.putExtra("userid", userid)
            intent.putExtra("toolbardormText", toolbardormText)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}