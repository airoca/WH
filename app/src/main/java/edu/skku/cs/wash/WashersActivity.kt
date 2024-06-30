package edu.skku.cs.wash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WashersActivity : AppCompatActivity() {

    private var washerList = mutableListOf<Washer>()

    private var userid: String? = null
    private var password: String? = null
    private var username: String? = null
    private var dormitory: String? = null
    private var gender: String? = "true"
    private var image: String? = null
    private var toolbardormText: String? = null

    private lateinit var toolbardorm:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_washers)

        val toolbartitle = findViewById<TextView>(R.id.toolBarTitle)
        toolbartitle.text = "세탁기"

        userid = intent.getStringExtra("userid")
        toolbardormText = intent.getStringExtra("toolbardormText").toString()
        Log.d("toolbardormText",toolbardormText.toString())

        val backbtn = findViewById<ImageButton>(R.id.toolBarBtn)
        backbtn.setOnClickListener {
            val intent = Intent(this, NavigateActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("userid",userid)
            intent.putExtra("toolbardormText", toolbardormText)
            startActivity(intent)
            finish()
        }

        toolbardorm = findViewById(R.id.toolBarDorm)
        toolbardorm.text = toolbardormText

        val recyclerView = findViewById<RecyclerView>(R.id.washers)

        val layoutManager = LinearLayoutManager(this)

        val call: Call<List<Washer>> = when (toolbardormText) {
            "사랑관" -> RetrofitClient.instance.getWashersDorm1()
            "소망관" -> RetrofitClient.instance.getWashersDorm2()
            "아름관" -> RetrofitClient.instance.getWashersDorm3()
            "나래관" -> RetrofitClient.instance.getWashersDorm4()
            else -> RetrofitClient.instance.getWashers()
        }

        call.enqueue(object : Callback<List<Washer>> {
            override fun onResponse(call: Call<List<Washer>>, response: Response<List<Washer>>) {
                if (response.isSuccessful) {
                    Log.d("WasherList", response.body().toString())
                    val fetchedList = response.body()
                    fetchedList?.let {
                        washerList.clear()
                        washerList.addAll(it)
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                    Log.d("WasherList2", washerList.toString())
                } else {
                }
            }

            override fun onFailure(call: Call<List<Washer>>, t: Throwable) {
                Log.e("WasherList", "Failed: ${t.message}")
            }
        })
        recyclerView.layoutManager = layoutManager
        userid?.let { nonNullUserId ->
            recyclerView.adapter =
                toolbardormText?.let { WasherAdapter(washerList, nonNullUserId, it) }
        } ?: run {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, NavigateActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra("userid",userid)
        intent.putExtra("toolbardormText", toolbardormText)
        startActivity(intent)
        finish()
    }
}