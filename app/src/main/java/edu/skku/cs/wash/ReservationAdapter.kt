package edu.skku.cs.wash

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReservationAdapter(private val reservedList: List<String>, private val userid: String) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {


    var username: String? = null
    class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reservedusername = itemView.findViewById<TextView>(R.id.reservedUserName)
        val reservedorder = itemView.findViewById<TextView>(R.id.reservedOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reserved_item, parent, false)

        Log.d("userid in reservationadapter",userid)
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
        }

        Log.d("username", username.toString())
        return ReservationViewHolder(view)

    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val currentUser = reservedList[position]
        holder.reservedorder.text = "${position+1}"
        holder.reservedusername.text = currentUser

        Log.d("username, currentuser", username+" "+currentUser)
        if (currentUser.trim() == username){
            holder.itemView.findViewById<LinearLayout>(R.id.reservedItemCard)
            holder.itemView.setBackgroundColor(R.color.blue)
        }
    }

    override fun getItemCount(): Int {
        return reservedList.size
    }
}