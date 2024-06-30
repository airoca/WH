package edu.skku.cs.wash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var etUserid: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    companion object {
        var isWashersReset = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

        etUserid = findViewById(R.id.etUserid)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        val logo = findViewById<ImageView>(R.id.logo)

        if (!isWashersReset) {
            resetWashersStatus()
        }

        btnLogin.setOnClickListener {
            loginUser()
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun resetWashersStatus() {
        RetrofitClient.instance.resetWashers().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.message == true) {
                    Log.d("ResetWashers", "All washers reset successfully")
                    // Set the global flag to indicate washers have been reset
                    isWashersReset = true
                } else {
                    Log.e("ResetWashers", "Failed to reset washers: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("ResetWashers", "Reset washers request failed", t)
            }
        })
    }

    private fun loginUser() {
        val userid = etUserid.text.toString().trim()
        val password = etPassword.text.toString().trim()

        RetrofitClient.instance.loginUser(User(userid, password, "", "", "", ""))
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.message == true) {
                        Toast.makeText(this@MainActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                        Log.d("Login", "Login successful for user: $userid")

                        userid?.let {
                            RetrofitClient.instance.getUserDetails(it)
                                .enqueue(object : Callback<User> {
                                    override fun onResponse(call: Call<User>, response: Response<User>) {
                                        if (response.isSuccessful) {
                                            response.body()?.let { user ->
                                                publicDorm = user.dormitory
                                                Log.d("publicDorm from login", publicDorm)
                                            }
                                        } else {
                                            Log.e("GetUserDetails", "Failed to get user details: ${response.errorBody()?.string()}")
                                        }
                                    }

                                    override fun onFailure(call: Call<User>, t: Throwable) {
                                        Log.e("GetUserDetails", "Error fetching user details", t)
                                    }
                                })
                        }

                        val intent = Intent(this@MainActivity, NavigateActivity::class.java)
                        intent.putExtra("userid", userid)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this@MainActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                        Log.e("Login", "Login failed: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("Login", "Login request failed", t)
                }
            })
    }
}
