package edu.skku.cs.wash

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import edu.skku.cs.wash.databinding.ActivityNavigateBinding
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

public var publicDorm: String = ""

class NavigateActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavigateBinding

    private var userid: String? = null
    private var password: String? = null
    private var username: String? = null
    private var dormitory: String? = null
    private var gender: String? = null
    private var image: String? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)

        binding = ActivityNavigateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = binding.appBarNavigate.toolbar

        setSupportActionBar(binding.appBarNavigate.toolbar)

        val title = toolbar.findViewById<TextView>(R.id.toolbarTitle)
        val toolbarbtn = toolbar.findViewById<ImageButton>(R.id.toolbarBtn)
        toolbarbtn.setImageDrawable(null)

        val toolbardorm = toolbar.findViewById<TextView>(R.id.toolbarDorm)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = findViewById(R.id.nav_view)

        val headerView = navView.getHeaderView(0)
        val sidename = headerView.findViewById<TextView>(R.id.sidename_tv)
        val sidedorm = headerView.findViewById<TextView>(R.id.sidedorm_tv)
        val sideimg = headerView.findViewById<CircleImageView>(R.id.headerimageView)
        val sideeditbtn = headerView.findViewById<ImageButton>(R.id.editBtn)
        val logoutbtn = headerView.findViewById<TextView>(R.id.logoutBtn)

        val toolbardormText = intent.getStringExtra("toolbardormText")
        publicDorm = toolbardormText.toString()
        userid = intent.getStringExtra("userid")

        userid?.let {
            RetrofitClient.instance.getUserDetails(it)
                .enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            response.body()?.let { user ->
                                this@NavigateActivity.userid = user.userid
                                this@NavigateActivity.password = user.pw
                                this@NavigateActivity.username = user.username
                                this@NavigateActivity.dormitory = user.dormitory
                                this@NavigateActivity.gender = user.gender
                                this@NavigateActivity.image = user.image
                                sidename.text = username + "님"
                                sidedorm.text = dormitory
                                toolbardorm.text = dormitory
                                publicDorm = dormitory as String

                                howManyWashersAvailable()

                                Log.d("publicDorm", publicDorm)
                                if (image != null){
                                    val decodedBytes = Base64.decode(image, Base64.DEFAULT)
                                    val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0 , decodedBytes.size)

                                    Glide.with(this@NavigateActivity)
                                        .load(decodedBitmap)
                                        .circleCrop()
                                        .into(sideimg)
                                }else{
                                    Glide.with(this@NavigateActivity)
                                        .load(R.drawable.baseline_person_24)
                                        .circleCrop()
                                        .into(sideimg)
                                }



                                Log.d("responsesuccessful in SelectScreen", user.toString())

                            }
                        } else {
                            Toast.makeText(this@NavigateActivity, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Toast.makeText(this@NavigateActivity, "에러: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("SelectActivity", "에러: ${t.message}")
                    }

                })
        } ?: run {
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show()
        }



        Log.d("toolbardormText", "Navreceived ${toolbardormText}")
        toolbardorm.text = toolbardormText
        Log.d("toolbardormText", "toolbar ${toolbardorm.text}")

        val navController = findNavController(R.id.nav_host_fragment_content_navigate)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_select, R.id.nav_using, R.id.nav_reserved
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        supportActionBar?.apply{
            setDisplayShowTitleEnabled(false)
        }

        navController.addOnDestinationChangedListener{ _, destination, _ ->
            val navtitle = when (destination.id) {
                R.id.nav_select -> getString(R.string.menu_select)
                R.id.nav_using -> "사용중"
                R.id.nav_reserved -> "예약중"
                else -> getString(R.string.app_name)
            }
            title.text = navtitle
        }

        logoutbtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        sideeditbtn.setOnClickListener{
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("userid", userid)
            startActivity(intent)
        }

        toolbardorm.setOnClickListener{
            val popup = PopupMenu(this, toolbardorm)
            popup.menuInflater.inflate(R.menu.navigate, popup.menu)
            if (gender=="남자") {
                popup.menu?.findItem(R.id.action_settings)?.title = "사랑관"
                popup.menu?.findItem(R.id.action_settings2)?.title = "소망관"
            } else {
                popup.menu?.findItem(R.id.action_settings)?.title = "아름관"
                popup.menu?.findItem(R.id.action_settings2)?.title = "나래관"
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_settings -> {
                        val newdorm = if (gender == "남자") "사랑관" else "아름관"
                        toolbardorm.text = newdorm
                        sidedorm.text = newdorm
                        publicDorm = newdorm
                        howManyWashersAvailable()

                        Log.d("publicDorm", publicDorm)
                        true
                    }
                    R.id.action_settings2 -> {
                        val newdorm = if (gender == "남자") "소망관" else "나래관"
                        toolbardorm.text = newdorm
                        sidedorm.text = newdorm
                        publicDorm = newdorm
                        howManyWashersAvailable()

                        Log.d("publicDorm", publicDorm)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_navigate)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun getToolbarDormText(): String {
        return binding.appBarNavigate.toolbar.findViewById<TextView>(R.id.toolbarDorm).text.toString()
    }

    fun getToolbarDorm(): TextView {
        return binding.appBarNavigate.toolbar.findViewById<TextView>(R.id.toolbarDorm)
    }

    fun getUserId(): String? {
        return userid
    }

    fun howManyWashersAvailable(){

        var allNum = 0
        var availableNum = 0

        val call: Call<List<Washer>> = when (publicDorm) {
            "사랑관" -> RetrofitClient.instance.getWashersDorm1()
            "소망관" -> RetrofitClient.instance.getWashersDorm2()
            "아름관" -> RetrofitClient.instance.getWashersDorm3()
            "나래관" -> RetrofitClient.instance.getWashersDorm4()
            else -> RetrofitClient.instance.getWashers()
        }


        Log.d("publicDorm", publicDorm)
        call.enqueue(object : Callback<List<Washer>> {
            override fun onResponse(call: Call<List<Washer>>, response: Response<List<Washer>>) {
                if (response.isSuccessful) {
                    Log.d("WasherList", response.body().toString())
                    val fetchedList = response.body()
                    fetchedList?.forEach {
                        allNum+=1
                        if (it.washerstatus == "사용 가능"){
                            availableNum+=1
                        }

                        Log.d("num","( ${availableNum} / ${allNum} )" )
                    }
                    Log.d("num","( ${availableNum} / ${allNum} )" )
                    val howmany = findViewById<TextView>(R.id.howManyAvailableWasher)
                    howmany.text = "( ${availableNum} / ${allNum} )"
                } else {
                }
            }

            override fun onFailure(call: Call<List<Washer>>, t: Throwable) {
                Log.e("WasherList", "Failed: ${t.message}")
            }
        })
    }
}