package com.example.sewadalparkingapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sewadalparkingapp.databinding.LiveVehicleFomatLooklikeBinding

class LiveVehicleAdapter(private val live_vehicles:ArrayList<VehicleEntity>, private val deleteListener: (Id:Int) -> Unit):RecyclerView.Adapter<LiveVehicleAdapter.ViewHolder>() {

    class ViewHolder(private val binding:LiveVehicleFomatLooklikeBinding):RecyclerView.ViewHolder(binding.root){
        val liveslnonooklike = binding.liveSlNoLookLike
        val livenamelooklike = binding.liveNameLookLike
        val licevehnolooklike = binding.liceVehNoLookLike
        val livemodelnamelooklike = binding.liveModelNameLookLike
        val livemobilenolooklike = binding.liveMobileNoLookLike

        val livevehiclemain = binding.liveVehicleMain
        val ivdelete = binding.ivDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LiveVehicleFomatLooklikeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.liveslnonooklike.text = live_vehicles[position].Id.toString()
        holder.livenamelooklike.text = live_vehicles[position].Name
        holder.licevehnolooklike.text = live_vehicles[position].Vehicle_no
        holder.livemodelnamelooklike.text = live_vehicles[position].Model
        holder.livemobilenolooklike.text = live_vehicles[position].Mobile_no

        if(position %2 == 0){
            holder.livevehiclemain.setBackgroundColor(ContextCompat.getColor(holder.itemView.context,R.color.khakhi))
        }else{
            holder.livevehiclemain.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.skyblue))
        }

        holder.ivdelete.setOnClickListener{
            deleteListener.invoke(live_vehicles[position].Id)
        }
    }

    override fun getItemCount(): Int {
        return live_vehicles.size
    }
}