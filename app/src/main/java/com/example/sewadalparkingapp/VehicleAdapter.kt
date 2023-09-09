package com.example.sewadalparkingapp

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sewadalparkingapp.databinding.VehicleFormatLooklikeBinding

class VehicleAdapter(private val vehicles:List<DialogVehicleEntity>, private val listener:OnItemClickListener, private val editListener: (DialogId:Int) -> Unit): RecyclerView.Adapter<VehicleAdapter.ViewHolder>() {

    class ViewHolder(binding: VehicleFormatLooklikeBinding, private val listener:OnItemClickListener, private val adapter: VehicleAdapter):RecyclerView.ViewHolder(binding.root){
        val slnolooklike = binding.slNoLookLike
        val namelooklike = binding.nameLookLike
        val vehnolooklike = binding.vehNoLookLike
        val modelnamelooklike = binding.modelNameLookLike
        val vehiclemain = binding.vehicleMain
        val mobilenolooklike = binding.mobileNoLookLike
        val ivedit = binding.ivEdit

        init {
            itemView.setOnClickListener{
                val position = absoluteAdapterPosition
                if(position != RecyclerView.NO_POSITION){
                    listener.onVehicleClick(adapter.filteredVehicles[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(VehicleFormatLooklikeBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.slnolooklike.text = filteredVehicles[position].DialogId.toString()
        holder.namelooklike.text = filteredVehicles[position].DialogName
        holder.vehnolooklike.text = filteredVehicles[position].DialogVehicle_no
        holder.modelnamelooklike.text = filteredVehicles[position].DialogModel
        holder.mobilenolooklike.text = filteredVehicles[position].DialogMobile_no

        if(position % 2 ==0){
            holder.vehiclemain.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.khakhi))
        }else{
            holder.vehiclemain.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.skyblue))
        }

        holder.ivedit.setOnClickListener{
            editListener.invoke(filteredVehicles[position].DialogId)
        }
    }

    override fun getItemCount(): Int {
        return filteredVehicles.size
    }

    private var filteredVehicles:List<DialogVehicleEntity> = vehicles

    //you can name the fun anything but the filter is also a predefined fun here which is used inside
    fun filter(query:String){
        filteredVehicles = if(query.isEmpty()){
            vehicles
        }else{
            vehicles.filter { vehicle->
                vehicle.DialogName.contains(query, ignoreCase = true) || vehicle.DialogVehicle_no.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    interface OnItemClickListener{
        fun onVehicleClick(vehicle:DialogVehicleEntity)
    }
}