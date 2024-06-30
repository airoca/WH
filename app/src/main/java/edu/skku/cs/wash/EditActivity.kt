package edu.skku.cs.wash

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class EditActivity : AppCompatActivity() {

    private var userid: String? = null
    private var password: String? = null
    private var username: String? = null
    private var dormitory: String? = null
    private var gender: String? = null
    private var image: String? = null

    private lateinit var tvid: TextView
    private lateinit var imageView: ImageView
    private lateinit var etUsername: EditText
    private lateinit var etID: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPassword2: EditText
    private lateinit var sGender: TextView
    private lateinit var sDormitory: Spinner

    val maleDorms = arrayOf("사랑관", "소망관")
    val femaleDorms = arrayOf("아름관", "나래관")

    private var imageuri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_edit)

        tvid = findViewById(R.id.suserid_tv)
        imageView = findViewById(R.id.simageView)
        etUsername = findViewById(R.id.seditTextName)
        etPassword = findViewById(R.id.seditTextPassword)
        etPassword2 = findViewById(R.id.seditNewPassword)
        sGender = findViewById(R.id.savedGender)
        sDormitory = findViewById(R.id.sspinnerDorm)


        userid = intent.getStringExtra("userid")

        userid?.let {
            RetrofitClient.instance.getUserDetails(it)
                .enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            response.body()?.let { user ->
                                userid = user.userid
                                password = user.pw
                                username = user.username
                                dormitory = user.dormitory
                                gender = user.gender
                                image = user.image

                                tvid.text = userid
                                etUsername.hint = username
                                sGender.text = gender

                                if (gender == "남자"){
                                    val newAdapter = ArrayAdapter(this@EditActivity,android.R.layout.simple_spinner_item, maleDorms)
                                    newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    sDormitory.adapter = newAdapter
                                }else if(gender == "여자"){
                                    val newAdapter = ArrayAdapter(this@EditActivity,android.R.layout.simple_spinner_item, femaleDorms)
                                    newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    sDormitory.adapter = newAdapter
                                }

                                if (dormitory == "사랑관" || dormitory == "아름관") {
                                    sDormitory.setSelection(0)
                                }else{
                                    sDormitory.setSelection(1)
                                }
                                if (image != null){
                                    val decodedBytes = Base64.decode(image, Base64.DEFAULT)
                                    val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0 , decodedBytes.size)

                                    Glide.with(this@EditActivity)
                                        .load(decodedBitmap)
                                        .circleCrop()
                                        .into(imageView)
                                }else{
                                    Glide.with(this@EditActivity)
                                        .load(R.drawable.baseline_person_24)
                                        .circleCrop()
                                        .into(imageView)
                                }
                                Log.d("responsesuccessful in SelectScreen", user.toString())
                                Toast.makeText(this@EditActivity, username, Toast.LENGTH_SHORT).show()

                            }
                        } else {
                            Toast.makeText(this@EditActivity, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Toast.makeText(this@EditActivity, "에러: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("SelectActivity", "에러: ${t.message}")
                    }

                })
        } ?: run {
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show()
        }

        val toolbartitle = findViewById<TextView>(R.id.toolBarTitle)
        toolbartitle.text = "회원정보수정"
        val toolbardorm = findViewById<TextView>(R.id.toolBarDorm)
        toolbardorm.text = ""
        val backbtn = findViewById<ImageButton>(R.id.toolBarBtn)
        backbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val showbtn = findViewById<ImageButton>(R.id.sshowbutton)
        showbtn.tag = "0"
        showbtn.setOnClickListener {
            Log.d("buttonClicked", showbtn.tag.toString())
            when(it.tag){
                "0" -> {
                    Log.d("buttonClicked1", showbtn.tag.toString())
                    showbtn.tag = "1"
                    etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    showbtn.setImageResource(R.drawable.hide)
                }
                "1" -> {
                    Log.d("buttonClicked1", showbtn.tag.toString())
                    showbtn.tag = "0"
                    etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    showbtn.setImageResource(R.drawable.view)
                }
            }
            etPassword.setSelection(etPassword.text!!.length)
        }
        val showbtn2 = findViewById<ImageButton>(R.id.sshowbutton2)
        showbtn2.tag = "0"
        showbtn2.setOnClickListener {
            Log.d("buttonClicked", showbtn2.tag.toString())
            when(it.tag){
                "0" -> {
                    Log.d("buttonClicked1", showbtn2.tag.toString())
                    showbtn2.tag = "1"
                    etPassword2.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    showbtn2.setImageResource(R.drawable.hide)
                }
                "1" -> {
                    Log.d("buttonClicked1", showbtn2.tag.toString())
                    showbtn2.tag = "0"
                    etPassword2.transformationMethod = PasswordTransformationMethod.getInstance()
                    showbtn2.setImageResource(R.drawable.view)
                }
            }
            etPassword2.setSelection(etPassword2.text!!.length)
        }

        imageView.setOnClickListener {
            openGalleryForImage()
        }



        sDormitory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                dormitory = selectedItem
                Log.d("Selected item", selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val buttonSave = findViewById<Button>(R.id.buttonSave)
        buttonSave.setOnClickListener {
            saveUser()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            imageuri = data?.data!!
            Glide.with(this)
                .load(imageuri)
                .circleCrop()
                .into(imageView)
//            imageView.setImageURI(imageuri)
        }
    }

    private fun saveUser() {
        val userid = userid
        val password = etPassword.text.toString().trim()
        val newpassword = etPassword2.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val dormitory = dormitory
        val gender = gender
        val image = if(imageuri!=null) {
            encodeuriToBase64(imageuri!!)
        } else {
            image
        }

        if (newpassword.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "원래 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val updateRequest = UserUpdateRequest(
            userid = userid ?: "",
            currentPassword = password,
            newPassword = newpassword,
            username = if(username.isEmpty()){ etUsername.hint.toString() } else {username},
            dormitory = dormitory ?: "",
            gender = gender ?: "",
            image = image
        )

        RetrofitClient.instance.updateUser(updateRequest)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.message == true) {
                        Toast.makeText(this@EditActivity, "회원 정보 수정 성공", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EditActivity, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@EditActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })



        val intent = Intent(this@EditActivity, NavigateActivity::class.java)
        intent.putExtra("userid", userid)
        Thread.sleep(500)
        startActivity(intent)
        finish()
    }

    private fun encodeImageToBase64(imageView: ImageView): String {
        imageView.buildDrawingCache()
        val bitmap = imageView.drawingCache

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }
    private fun encodeuriToBase64(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()

        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}