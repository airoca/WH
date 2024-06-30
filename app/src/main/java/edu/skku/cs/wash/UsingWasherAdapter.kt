package edu.skku.cs.wash

import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class UsingWasherAdapter(private val usingList: List<Washer>) : RecyclerView.Adapter<UsingWasherAdapter.UsingViewHolder>() {

    class UsingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usingname: TextView = itemView.findViewById(R.id.txtUsingName)
        val remainingTime: TextView = itemView.findViewById(R.id.txtuserRemainingTime)
        val dorm: TextView = itemView.findViewById(R.id.txtDorm)
        val usingcard: LinearLayout = itemView.findViewById(R.id.usingItem)
        lateinit var mTimer: CountDownTimer
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.using_item, parent, false)
        return UsingViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: UsingViewHolder, position: Int) {
        val currentUsing = usingList[position]
        holder.usingname.text = currentUsing.washername
        holder.dorm.text = "("+usingList[position].dorm+")"
        val usingid = currentUsing.id


        val remainingTime = currentUsing.starttime + currentUsing.settime - System.currentTimeMillis()

        if(remainingTime>0){
            if (remainingTime > 0) {
                holder.mTimer = object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val hours = millisUntilFinished / (1000 * 60 * 60)
                        val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                        val seconds = (millisUntilFinished % (1000 * 60)) / 1000

                        val timeLeftFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        holder.remainingTime.text = timeLeftFormatted
                    }

                    override fun onFinish() {
                        holder.remainingTime.text = "세탁 완료"
                        Toast.makeText(holder.itemView.context, "세탁이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }.start()
            }
        }
        else{
            holder.remainingTime.text=""
        }


        if (usingid<1000){
            holder.usingcard.setBackgroundColor(ContextCompat.getColor(holder.usingcard.context, R.color.lightblue))
            Log.d("color",usingid.toString()+" "+holder.usingcard.background)
        }else{
            holder.usingcard.setBackgroundColor(ContextCompat.getColor(holder.usingcard.context, R.color.lightred))
            Log.d("color",usingid.toString()+" "+holder.usingcard.background)
        }
    }

    override fun getItemCount(): Int {
        return usingList.size
    }
}