package edu.skku.cs.wash

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.skku.cs.wash.databinding.FragmentReservedBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReservedFragment : Fragment() {

    private var _binding: FragmentReservedBinding? = null

    private val binding get() = _binding!!

    private var rsvForWasherList = mutableListOf<String>()

    private var isCanceled: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReservedBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val navigateActivity = activity as? NavigateActivity
        val userid = navigateActivity?.getUserId()
        var username = ""
        var canUse = false

        // Initialize and configure the toolbar
        val toolbarTitle = root.findViewById<TextView>(R.id.toolBarTitle)
        toolbarTitle.text = "예약중"
        val toolbarDorm = root.findViewById<TextView>(R.id.toolBarDorm)
        toolbarDorm.text = ""
        val backBtn = root.findViewById<ImageButton>(R.id.toolBarBtn)
        backBtn.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        navigateActivity?.supportActionBar?.hide()

        userid?.let {
            RetrofitClient.instance.getUserDetails(it)
                .enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            response.body()?.let { user ->
                                username = user.username
                            }
                        } else {
                        }
                    }
                    override fun onFailure(call: Call<User>, t: Throwable) {
                    }
                })
        } ?: run {
        }

        val washerrecyclerView = root.findViewById<RecyclerView>(R.id.reserved_people_for_washer)
        washerrecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val washerBtn = root.findViewById<LinearLayout>(R.id.rsvWasherCard)
        val washername = root.findViewById<TextView>(R.id.rsvWasherName)
        val washerdorm = root.findViewById<TextView>(R.id.rsvWasherdorm)
        val washerremaintime = root.findViewById<TextView>(R.id.rsvWasherTime)
        val washertexts = root.findViewById<LinearLayout>(R.id.rsvWasherTexts)
        val washername2 = root.findViewById<TextView>(R.id.rsvWasherName2)
        var washerid:Int? = null

        val washerCancelBtn = root.findViewById<TextView>(R.id.washerClearbtn)
        washerCancelBtn.setOnClickListener{
            userid?.let { it1 -> UserId(it1) }?.let { it2 ->
                RetrofitClient.instance.cancelWasherReservation(it2)
                    .enqueue(object : Callback<ApiResponse>{
                        override fun onResponse(
                            call: Call<ApiResponse>,
                            response: Response<ApiResponse>
                        ) {
                            rsvForWasherList.clear()

                            Log.d("canUse", canUse.toString())
                            if(canUse){
                                val scaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down)
                                washerBtn.animation = scaleDown
                                scaleDown.start()
                                canUse = false
                            }
                            washername.text = " - "
                            washerdorm.text = ""
                            washerremaintime.text = ""
                            washername.visibility = View.VISIBLE
                            washerdorm.visibility = View.VISIBLE
                            washerremaintime.visibility = View.VISIBLE
                            washertexts.visibility = View.INVISIBLE
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        }

                    })
            }
            isCanceled = true
            rsvForWasherList.clear()

            washerrecyclerView.visibility = View.INVISIBLE
        }

        userid?.let { userId ->
            RetrofitClient.instance.getWasherReservationsByUser(userId)
                .enqueue(object : Callback<List<Washer>> {
                    override fun onResponse(call: Call<List<Washer>>, response: Response<List<Washer>>) {
                        if (response.isSuccessful) {
                            response.body()?.forEach { washer ->
                                Log.d("ReservedFragment", "Reserved Washers: $washer")
                                washerid = washer.id
                                washername.text = washer.washername
                                washerdorm.text = washer.dorm
                                washerremaintime.text = ""

                                if (washer.washerstatus == "사용중") {
                                    val remainingTime = washer.starttime + washer.settime - System.currentTimeMillis()

                                    val timer = object : CountDownTimer(remainingTime, 1000) {
                                        override fun onTick(millisUntilFinished: Long) {
                                            if (isCanceled) {
                                                cancel()
                                                onFinish()
                                                return
                                            }
                                            val hours = millisUntilFinished / (1000 * 60 * 60)
                                            val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                                            val seconds = (millisUntilFinished % (1000 * 60)) / 1000

                                            val timeLeftFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                                            washerremaintime.text = timeLeftFormatted
                                        }

                                        override fun onFinish() {
                                            washerremaintime.text = ""

                                            if (rsvForWasherList.size>0 && rsvForWasherList[0] == username){
                                                canUse = true
                                                Log.d("anim", "rsvForWasherList[0]: ${rsvForWasherList[0]} username: ${username}")
                                                val scale_anim = AnimationUtils.loadAnimation(context, R.anim.scale_up)
                                                washerBtn.animation = scale_anim

                                                washername.visibility = View.INVISIBLE
                                                washerdorm.visibility = View.INVISIBLE
                                                washerremaintime.visibility = View.INVISIBLE
                                                scale_anim.start()

                                                val washertexts = root.findViewById<LinearLayout>(R.id.rsvWasherTexts)
                                                val washername2 = root.findViewById<TextView>(R.id.rsvWasherName2)
                                                washername2.text = washer.washername
                                                val fade_in = AnimationUtils.loadAnimation(context,R.anim.fade_in)
                                                washertexts.animation = fade_in
                                                fade_in.start()
                                                washertexts.visibility = View.VISIBLE

                                            }
                                        }
                                    }
                                    timer.start()
                                } else {
                                    washerremaintime.text = ""
                                }

                                Log.d("washerid",washerid.toString())
                                washerid?.let {
                                    RetrofitClient.instance.getUsernamesByWasher(it)
                                        .enqueue(object : Callback<List<String>> {
                                            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                                                if (response.isSuccessful) {
                                                    rsvForWasherList.clear()
                                                    response.body()?.let { usernames ->
                                                        rsvForWasherList = usernames.toMutableList()
                                                        val adapter = userid?.let { ReservationAdapter(rsvForWasherList, it) }
                                                        washerrecyclerView.adapter = adapter
                                                        Log.d("ReservedFragment", "Usernames for Washer $washerid: $usernames")
                                                        Log.d("rsvForWasherList", rsvForWasherList.toString())
                                                    }
                                                    Log.d("canUse", washer.washerstatus.toString() +" " + rsvForWasherList.toString()+" "+username)
                                                    if (washer.washerstatus == "예약중" && rsvForWasherList.size>0 && rsvForWasherList[0] == username){

                                                        canUse = true
                                                        val scale_anim = AnimationUtils.loadAnimation(context, R.anim.scale_up)
                                                        washerBtn.animation = scale_anim

                                                        washername.visibility = View.INVISIBLE
                                                        washerdorm.visibility = View.INVISIBLE
                                                        washerremaintime.visibility = View.INVISIBLE
                                                        scale_anim.start()

                                                        washername2.text = washer.washername
                                                        washertexts.visibility = View.VISIBLE
                                                    }
                                                } else {
                                                    Log.e("ReservedFragment", "Error: ${response.errorBody()?.string()}")
                                                }
                                            }

                                            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                                                Log.e("ReservedFragment", "Failed to fetch usernames for washer", t)
                                            }
                                        })
                                }
                            }
                        } else {
                            Log.e("ReservedFragment", "Error: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<List<Washer>>, t: Throwable) {
                        Log.e("ReservedFragment", "Failed to fetch reserved washers", t)
                    }
                })

            val useBtn = root.findViewById<TextView>(R.id.startUsingBtn)
            useBtn.setOnClickListener{
                val intent = Intent(context, TimeSetActivity::class.java)
                intent.putExtra("washername", washername.text)
                intent.putExtra("washerid", washerid)
                Log.d("washerid", washerid.toString())
                intent.putExtra("userid", userid)

                Log.d("userid", userid.toString())
                intent.putExtra("toolbardormText", publicDorm)
                context?.let { it1 -> ContextCompat.startActivity(it1, intent, null) }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Show the action bar when the fragment is destroyed
        (activity as? NavigateActivity)?.supportActionBar?.show()
    }
}
