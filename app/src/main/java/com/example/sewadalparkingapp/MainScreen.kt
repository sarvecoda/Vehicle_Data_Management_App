package com.example.sewadalparkingapp

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.sewadalparkingapp.databinding.ActivityMainScreenBinding
import com.example.sewadalparkingapp.databinding.EditDialogVehicleBinding
import com.example.sewadalparkingapp.databinding.FinalVehicleAddingDialogBinding
import com.example.sewadalparkingapp.databinding.FullListVehicleDialogBinding
import com.example.sewadalparkingapp.databinding.UnaddedVehiclesDialogBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainScreen : AppCompatActivity(), VehicleAdapter.OnItemClickListener {

    private var binding:ActivityMainScreenBinding? = null

    private lateinit var fullListVehicleDialog:Dialog
    private var nextId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        super.onCreate(savedInstanceState)
        binding = ActivityMainScreenBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        hardCodedList()

        val vehicleDao2 = (application as VehicleApp).db.vehicleDao()

        binding?.addVehicleBtn?.setOnClickListener{
            setupDialog()
        }

        lifecycleScope.launch{
            vehicleDao2.fetchallVehicles().collect(){
                val list = ArrayList(it)

                val adapter1 = LiveVehicleAdapter(list,
                    {
                        deleteId ->
                        deleteRecord(deleteId,vehicleDao2)
                    }
                    )

                binding?.finalRecyclerList?.layoutManager = LinearLayoutManager(this@MainScreen)
                binding?.finalRecyclerList?.adapter = adapter1
            }
        }

        binding?.floatAddUnAddedVehicles?.setOnClickListener{
            setupUnAddedVehiclesDialog()
        }

    }

    fun setupDialog(){
        // Setting up the dialog with the full list of Vehicles and we are not creating the dialog
        // here because we want to use the dialog in other fun's as well
        fullListVehicleDialog = Dialog(this)

        // Set the custom background drawable for the dialog
        fullListVehicleDialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.rounded_dialog_background))

        var binding1 = FullListVehicleDialogBinding.inflate(layoutInflater)

        fullListVehicleDialog.setContentView(binding1.root)

        val vehicleDao3 = (application as VehicleApp).dialogdb.dialogvehicleDao()

        // Seting the dialog width to match the screen width
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(fullListVehicleDialog.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        fullListVehicleDialog.window?.attributes = layoutParams

        fullListVehicleDialog.show()

        binding1.cancelRecyclerList.setOnClickListener{
            fullListVehicleDialog.dismiss()
        }


        // Setting up the adapter and Recyclerview
        val unaddedVehicleDao = (application as DialogVehicleApp).dialogdb.dialogvehicleDao()
        lifecycleScope.launch{

            unaddedVehicleDao.fetchallDialogVehicles().collect(){
                val list1 = ArrayList(it)
                val adapter = VehicleAdapter(list1, this@MainScreen,
                    {
                        editId ->
                        editRecord(editId, vehicleDao3 )
                    }
                )

                binding1.fullRecyclerList.layoutManager = LinearLayoutManager(this@MainScreen )
                binding1.fullRecyclerList.adapter = adapter

                binding1.searchVehiclesList.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        newText?.let{
                            adapter.filter(it)
                        }
                        return true
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onVehicleClick(vehicle: DialogVehicleEntity) {

        //here we are intializing the Dao and using the data of the clicked item to set the variable for different data
        val vehicleDao1 = (application as VehicleApp).db.vehicleDao()

        val name = vehicle.DialogName
        val vehicle_no = vehicle.DialogVehicle_no
        val model = vehicle.DialogModel
        val mobile_no = vehicle.DialogMobile_no

        // now setting up the dialog which will ask to add the record

        val addVehicleDialog = Dialog(this)

        var binding2 = FinalVehicleAddingDialogBinding.inflate(layoutInflater)

        addVehicleDialog.setContentView(binding2.root)

        //here wr are checking if the vehicle is already there in the list or not
        lifecycleScope.launch{
            val existingVehicle = vehicleDao1.getVechicleByName(name)

            if(existingVehicle == null){
                binding2.aboutToBeAddedValues.setText("${name}, ${vehicle_no}, ${model}")
                addVehicleDialog.show()
            }else{
                Toast.makeText(this@MainScreen, "${name}'s vechicle is already in the list", Toast.LENGTH_SHORT).show()
                addVehicleDialog.dismiss()
            }
        }

        binding2.finalVehicleAddingYesbtn.setOnClickListener {

            //finally we are adding the vehicle in our list
                lifecycleScope.launch {
                    val maxId = vehicleDao1.getMaxId() ?: 0
                    val nextId = maxId + 1
                    vehicleDao1.insert(VehicleEntity(Id = nextId, Name = name, Vehicle_no = vehicle_no, Mobile_no = mobile_no, Model = model))
                }

                Toast.makeText(this@MainScreen, "${vehicle.DialogName}'s vehicle has been added to the list", Toast.LENGTH_SHORT).show()
                fullListVehicleDialog.dismiss()
                addVehicleDialog.dismiss()
        }

        binding2.finalVehicleAddingNobtn.setOnClickListener {
            addVehicleDialog.dismiss()
        }
    }



    private fun deleteRecord(id:Int, vehicleDao: VehicleDao){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("YES"){builderInterface, _ ->
            lifecycleScope.launch{
                vehicleDao.delete(VehicleEntity(id))
                vehicleDao.updateIdsAfterDeletion(id)
                Toast.makeText(applicationContext, "Recode is deleted", Toast.LENGTH_SHORT).show()
            }
            builderInterface.dismiss()
        }
        builder.setNegativeButton("NO"){builderInterface, _ ->
            builderInterface.dismiss()
        }
        val alertDialog : AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun setupUnAddedVehiclesDialog(){

        val unaddedVehicleDao = (application as DialogVehicleApp).dialogdb.dialogvehicleDao()
        val vehicleDao1 = (application as VehicleApp).db.vehicleDao()

        var unaddedVehiclesDialog:Dialog

        unaddedVehiclesDialog = Dialog(this)

        var binding3 = UnaddedVehiclesDialogBinding.inflate(layoutInflater)

        unaddedVehiclesDialog.setContentView(binding3.root)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(unaddedVehiclesDialog.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        unaddedVehiclesDialog.window?.attributes = layoutParams

        unaddedVehiclesDialog.show()

        binding3.addUnaddedRecord.setOnClickListener {
            val name = binding3.unaddedName.text.toString()
            val vehicle_no = binding3.unaddedVehicleNo.text.toString()
            val model_no = binding3.unaddedModelNo.text.toString()
            val mobile_no = binding3.unaddedMobileNo.text.toString()

            lifecycleScope.launch{

                val maxid = unaddedVehicleDao.getMaxId() ?:0
                val nextid = maxid+1

                val maxId = vehicleDao1.getMaxId() ?: 0
                val nextId = maxId + 1

                unaddedVehicleDao.insert(DialogVehicleEntity(DialogId = nextid,DialogName = name, DialogVehicle_no = vehicle_no, DialogMobile_no = mobile_no, DialogModel = model_no))
                vehicleDao1.insert(VehicleEntity(Id = nextId, Name = name, Vehicle_no = vehicle_no, Mobile_no = mobile_no,Model = model_no))
                Toast.makeText(this@MainScreen, "${name}'s vehicle has been added to the Today's list and in Main list as well", Toast.LENGTH_SHORT).show()
                unaddedVehiclesDialog.dismiss()
            }
        }

    }

    fun hardCodedList() {
        val unaddedVehicleDao = (application as DialogVehicleApp).dialogdb.dialogvehicleDao()

        data class VehicleData(val name:String, val VehicleNumber:String, val Mobile_no:String, val Model:String)

        val vehicles = listOf(
            VehicleData("ANUJ CHANDANI JI", "KA 03 NB 3622", "7204563886", ""),
            VehicleData("SUNDEERLAL JI", "KA 52 2705 3", "9008878809", "OMNI (SILVER)"),
            VehicleData("HEMANT JI", "KA 03 NE 0445", "7353356794", "DZIRE (BLUE)"),
            VehicleData("RAHUL JI", "UK 04 AD 0077", "9910080747", ""),
            VehicleData("PANKAJ JI", "KA 53 MB 0182", "8431005096", ""),
            VehicleData("HAPPY JI", "KA 03 MR 5612", "9480917320", ""),
            VehicleData("AMIT SARDANA JI", "KA 01 MG 7118", "8551957000", "ETOS (GOLDEN)"),
            VehicleData("SWINSHEEL JI", "KA 05 NA 1697", "9164625704", ""),
            VehicleData("AMIT KUHIKAR JI", "KA 50 P 8185", "8879007333", "DUSTER (CHOCOLATE)"),
            VehicleData("SONAL JI", "KA 04 MY 0412", "9845617470", ""),
            VehicleData("DEVENDRA JI", "KA 03 MP 7456", "9611049433", ""),
            VehicleData("PRAMOD JI", "DL 3 CCC 4101", "9891466481", "AMAZE (SILVER)"),
            VehicleData("SAURABH JI", "KA 03 NJ 6124", "9911053145", ""),
            VehicleData("RAJKUMAR JI", "KA 01 MM 1140", "9880029857", ""),
            VehicleData("ASHISH JI", "KA 02 MN 1352", "9886104793", ""),
            VehicleData("AVNEESH RATRA JI", "KA 03 NA 2165", "8951268552", "XUV 500 (WHITE)"),
            VehicleData("ANAND JI", "KA 04 MV 3690", "9900000747", "INNOVA (WHITE)"),
            VehicleData("VANSH JI", "KA 51 MD 9487", "7204311991", "VERNA (SILVER)"),
            VehicleData("VISHWASH JI", "KA 04 MN 7347", "6362240835", "THAR (GREEN)"),
            VehicleData("SUNIL KRAPLINI JI", "KA 01 MQ 2650", "9379529736", "HONDA CITY (GOLDEN BROWN)"),
            VehicleData("NARESH JI", "KA 04 MK 9188", "9686854300", "i 20 (silver)"),
            VehicleData("BAKSHI RAM JI", "", "9886204963", ""),
            VehicleData("BALJEET JI", "KA 53 MC 6164", "8073635982", "DUSTER (BROWN)"),
            VehicleData("GAURAV PRUTHI JI", "23 BH 0574 E", "9008715341", "XUV 700 (WHITE)"),
            VehicleData("RAJAN PAHUJA JI", "DL 4C AW 6980", "9619014978", "HONDA CITY (BROWN)"),
            VehicleData("RAJU YADAV JI", "KA 03 NE 3724", "9945592909", "MARUTI ERTIGA (RED)"),
            VehicleData("RAVI RANJAN JI", "KA 01 MR 3201", "8095971229", "CELERIO (SILVER)"),
            VehicleData("SANDEEP CHAUDHRY JI", "KA 51 MP 3060", "9739603785", "KIA SELTOS (BLACK)"),
            VehicleData("SATISH DHIMAN JI", "GJ 01 EW 1763", "9964138111", "TATA HARRIER (BLACK)"),
            VehicleData("VIRENDRA KUMAR JI", "KA 01 ME 4952", "9880235707", "i10 (WHITE)"),
            VehicleData("AKASH MISHRA JI", "JH 02 AG 4155", "9740364304", "WAGON R (SILVER)"),
            VehicleData("ABHISHEK SINGH JI", "TN 22 DL 5664", "9860604735", "MARUTI BALENO (LIGHT GREY)"),
            VehicleData("EKNATH JI", "KA 51 MG 334", "9986357337", "i20 (RED)"),
            VehicleData("MANJU NAYAK JI", "JK 14 B 4013", "6360347540", "SANTRO (SILVER)"),
            VehicleData("AKSHAY JI", "TN 10 BA 0015", "9840230693", "FORTUNER (WHITE)"),
            VehicleData("ASHMEET JI", "KA 05 MU 8228", "9513385557", "HONDA CITY (WHITE)"),
            VehicleData("HARISH JI", "KA 02 MJ 5845", "7795827600", "i10 ASTA (SILVER)"),
            VehicleData("DAYAANAND JI", "KA 03 MW 6603", "9740177570", "i20 (RED)"),
            VehicleData("ABHINAV RATHORE JI", "KA 01 MU 1361", "9035926422", "i20 (RED)"),
            VehicleData("SATISH CHABRA JI", "KA 03 MN 5053", "9448009858", "WAGON R (GREY)"),
            VehicleData("SIDDHARTH CHABRA JI", "MH 12 OM 8291", "9830958080", "AMAZE (GREY)"),
            VehicleData("MAYUR JI", "MH 43 BY 6752", "8722029000", "ALCAZAR (WHITE)"),
            VehicleData("YOGESH MAHABUBANI JI", "KA 01 ME 1494", "7204365251", "ALTO (MAROON)"),
            VehicleData("GAUTHAM BAJAJ JI", "KA 01 MK 7599", "9900488733", "CRETA (WHITE)"),
            VehicleData("RAHUL GANDHI JI", "JK 08 G 8577", "9779994717", "i20 (WHITE)"),
            VehicleData("ARJUN BAJAJ JI", "KA 01 MR 2651", "9900488533", "FORTUNER (WHITE)"),
            VehicleData("ASHISH DIXIT JI", "KA 02 ML 1502", "9886904793", "ECO SPORT (SILVER)"),
            VehicleData("DEEP ANAND JI", "KA 03 ND 1455", "9342032856", "BALENO (BLACK)"),
            VehicleData("PRASHANTH JI", "KA 50 P 1363", "9739493434", "INDIGO (SILVER)"),
            VehicleData("LOKESH KHATRI JI", "KA 04 MP 5028", "9886096104", "RITZ (SILVER)"),
            VehicleData("RAHUL THAKUR JI", "KA 04 MR 5583", "7259378545", "ALTO K10 (WHITE)"),
            VehicleData("JEETU JI", "KA 01 MK 1968", "9986987280", "ETOS (BLUE)"),
            VehicleData("MOHIT SOOD JI", "KA 03 NL 8947", "9845293771", "KIA CARENS (WHITE)"),
            VehicleData("NEHRU JI", "KA 02 MR 2921", "7259790300", "MARAZO (SILVER WHITE)"),
            VehicleData("SANJAT GUPTA JI", "MH 48 AC 9718", "9967654159", "HONDA CITY (WHITE)"),
            VehicleData("AYUSH JINDAL JI", "KA 01 MT 0602", "9991188764", "HONDA CITY (BROWN)"),
            VehicleData("NAVDEEP SACHDEVA JI", "KA 01 MS 7181", "9871104750", "XUV 500 (WHITE)"),
            VehicleData("CK KARUN JI", "KA 04 MY 7875", "9844013264", "i10 ASTA (BLUE)"),
            VehicleData("BHUVI SHARMA JI", "KA 51 MM 7892", "9971891188", ""),
            VehicleData("VINOD BANSILAL JI", "KA 05 MX 3004", "9845016616", ""),
            VehicleData("SATPATI JI", "KA 51 MP 2469", "9663711198", ""),
            VehicleData("ANAND JI", "KA 04 MV 7479", "9900000747", ""),
            VehicleData("NAVDISHA JI", "KA 01 MR 6735", "9916626133", ""),
            VehicleData("NITESH JI", "KA 03 NK 5768", "8872323112", "Nexon (green)"),
            VehicleData("DHARMESH JI", "KA 51 MN 3578", "9900596153", "JAZZ (RED)"),
            VehicleData("YOGESH JI", "KA 04 MQ 2955", "9742263014", "SWIFT (SILVER)"),
            VehicleData("A JI", "KA 03 NF 7730", "6360210918", ""),
            VehicleData("ANSHUL JI", "MP 04 CR 3195", "7694081888", ""),
            VehicleData("SUNIL JI", "KA 53 MF 5842", "8095632642", "DUSTER (BROWN)"),
            VehicleData("ASHOK SHARMA JI", "KA 01 MG 1399", "9341924248", "YELLOW"),
            VehicleData("KAILASH JI", "KA 01 AK 2830", "9538040546", "AUTO (GREEN)"),
            VehicleData("SANTOSH JI", "UP 14 BP 2610", "9538500602", "SANTRO (SILVER)"),
            VehicleData("AJEET JI", "KA 50 MB 0312", "9767214098", "ALTROZ (GREEN)"),
            VehicleData("DEEPAK JI", "KA 03 MV 2686", "9036650022", "i20 (WHITE)"),
            VehicleData("MANORAG SAAENA", "RJ 07 CC 8978", "9999728650", "DUSTER (SILVER)"),
            VehicleData("Mahendar", "KA 34 DB 9109", "8553469810", "Pulsar"),
            VehicleData("Dheeraj ji", "KA 01 HN 1847", "8861776047", "Hobda Activa"),
            VehicleData("Chandan ji", "KA 03 NA 7924", "9845420273", ""),
            VehicleData("V S YADAV JI", "KA 03 MX 9961", "9916246100", ""),
            VehicleData("Saurav ji", "KA 03 NP 8631", "8792604344", ""),
            VehicleData("Avnish ji", "KA 03 NA 2105", "8951268552", ""),
            VehicleData("Sandeep ji", "KA 01 MX 7809", "9535875990", "Nexa XL 6 (White)"),
            VehicleData("Amit ji", "KA 05 MB 8814", "8240889025", ""),
            VehicleData("Shritin ji", "KA 53 MF 2917", "", ""),
            VehicleData("Amit Sardana ji", "KA 51 MU 1631", "8551957000", "Brezza (red)"),
            VehicleData("Surbhi ji", "KA 51 MM 7897", "8971018670", ""),
            VehicleData("Vishal ji", "PB 70 F 9450", "8283858532", "Wagnor"),
            VehicleData("MAYUR JI", "MH 43 BX 6725", "8722029000", ""),
            VehicleData("Ishwar ji", "KA 04 MX 6041", "8660962874", "Eco (grey)"),
            VehicleData("Manish ji", "PB 03 BF 0361", "7536871675", ""),
            VehicleData("Sant tyagi ji", "KA 53 MK 3318", "9591183559", "Punch (grey)"),
            VehicleData("ASHKOE GUPTA DEVANHALLI", "KA 43 N 3340", "9164292398", "ERTIGA (WHITE)"),
            VehicleData("KARTHIK", "KA 51 MS 3179", "8951135398", "GRAND I10 (DARK GREEN)"),
            VehicleData("REV SUNIL RATRA JI,ZI", "KA 04 MD 8216", "8951135398", "INNOVA (GOLD)"),
            VehicleData("ANSHUL JI", "DL 2C AU 9668", "8744002644", "SWIFT, DARK GREY"),
            VehicleData("COL MANHAS", "PB 35 F 4082", "9945131767", "WAGNOR SILVER"),
            VehicleData("MANISH JI", "KA 30 NA 0998", "9844984772", ""),
            VehicleData("AMAN JI", "KA 05 NC 6745", "7000269225", "RENAULT KIGER"),
            VehicleData("PUNEETH JI", "KA 05 MN 1595", "9964539057", "BALENO SILVER"),
            VehicleData("AMIT VISHNANI JI", "MH 10 AN 3202", "9986951693", "SANTRO"),
            VehicleData("KARAN DODEJJA JI", "KA 05 MT 1442", "9611226555", "SWIFT GREAY"),
            VehicleData("GANSHYAM JI", "KA 01 MH 9525", "9900024370", "SWIFT"),
            VehicleData("AMITH WALIYA JI", "KA 50 MB 8814", "8123434946", "GRAND I10 ASTER"),
            VehicleData("RAJESH JI", "KA 03 NM 4064", "9110841031", "TATA NEXON"),
            VehicleData("SUMITH GUPTA JI", "KA 03 NJ 3304", "9620821809", "JEEP COMPASS"),
            VehicleData("GOPAL CHAND JI", "KA 03 NE 3084", "9480068870", "THAR"),
            VehicleData("VIJAY MISHRA", "KA 53 P 6902", "8197404797", "CIVIC HONDA"),
            VehicleData("SANTOSH JI", "KA 01 MY 0487", "9538500602", "KIA GARENCE"),
            VehicleData("ANUPAM JI", "JH 24 G 5377", "8904735945", "KIA SONNET"),
            VehicleData("SATWINDER JI", "KA 05 4764", "9141520327", ""),
            VehicleData("RAM GOPAL JI", "KA 05 Z 0425", "9845961805", ""),
            VehicleData("MANJUNATH JI", "KA 03 NJ 6419", "9686843287", "SWIFT"),
            VehicleData("ROHINI JI", "TS 9255", "9938049718", "I20 WHITE"),
            VehicleData("REESAW SHREEVASTAV", "KA 51 MS 8284", "7011107317", ""),
            VehicleData("MANISH KUMAR", "KA 03 MN 1651", "9606570928", ""),
            VehicleData("ARPITH JI", "UP 78 AX 8050", "8939648836", ""),
            VehicleData("SONAL", "5054", "9845617470", ""),
            VehicleData("AMITH JI", "KA 51 MA 9124", "9845985328", ""),
            VehicleData("PRABHAKAR JI", "KA 05 MU 1110", "9731399669", ""),
            VehicleData("KUSHAL JI", "910", "9538800336", ""),
            VehicleData("M C PANDEY JI", "KA 03 NJ 7171", "7829611794", "S PRESSO (ORANGE)"),
            VehicleData("JEETU JI", "KA 05 MM 6498", "9986987280", "DUSTER (BROWN)"),
            VehicleData("NIRAJ JI", "JK 02 BR 4315", "9682527195", "SWIFT (WHITE)"),
            VehicleData("SONIYA JI", "23 BH 2225CV", "9611613222", "NEXON (BLACK)"),
            VehicleData("VANDANA JI", "KA 53 MB 5282", "7259637117", "ALTO K 10 (RED)"),
            VehicleData("SUDARSHAN SHAH JI", "KA 01 MC 8092", "9845166446", "INNOVA (BLACK)"),
            VehicleData("ROOP JI", "KA 03 MR 2855", "9845020081", "HONDA CITY (WHITE)"),
            VehicleData("RAMESH JI", "KA 04 MW 0305", "9342458606", "SANTRO (SLIVER)"),
            VehicleData("PRINCE JI", "KA 03 ND 3114", "9972054000", "BREZZA (WHITE)"),
            VehicleData("AVNEESH JI", "KA 04 MM 3272", "9916166246", "WAGON R (BROWN)"),
            VehicleData("CP SARDANA JI", "KA 03 MK 4190", "9868246910", "HONDA CITY (RED)"),
            VehicleData("JAI SHANKAR JI", "JH 0100", "9945140500", "i 10 (SILVER)"),
            VehicleData("SANJEEV GANDJI JI", "DL 10 CL 3194", "8826231621", "NEXON (WHITE)"),
            VehicleData("ASHOK KUMAR JI", "KA 03 MX 1756", "9019225069", "WAGON R (RED)"),
            VehicleData("PUNEET JI", "KA 03 NP 4122", "8095323837", "TATA PUNCH (GREY)"),
            VehicleData("SURAJ JI", "KA 02 MT 8628", "8712864763", "VENUE (SILVER)"),
            VehicleData("SONIA JI", "4737", "9141520327", "i 10 (SILVER)"),
            VehicleData("RAVI KANT SHARMA JI", "KA 03 NE 8723", "9886359048", ""),
            VehicleData("SANDEEP NANGIA JI", "DL 2C AW 3275", "9711444888", "FORD FIGO (GREY)"),
            VehicleData("PAVAN JI", "KA 04 AC 2238", "9741409444", "SWIFT DIZ (WHITE)"),
            VehicleData("SANJEEV KUMAR JI", "KA 01 MJ 2667", "9036034217", ""),
            VehicleData("CHANDER PRAKASH JI", "KA 03 MK 4190", "9868246910", ""),
            VehicleData("PAVAN ARORA JI", "KA 03 MS 4407", "8095010055", "RITZS"),
            VehicleData("VISHAL JI", "PB 70 F 9450", "8283828532", ""),
            VehicleData("PRATIK JI", "KA 03 NP 5038", "9262691951", "PUNCH"),
            VehicleData("DHEERAJ JI", "KA 01 ML 1733", "8861776047", "GRAND I 10"),
            VehicleData("RASHMITA JI", "KA 04 MV 3690", "9900992000", "SWIFT"),
            VehicleData("SANYA JI", "KA 51 MF 5019", "9343340270", ""),
            VehicleData("OJASVI JI", "KA 05 NH 6376", "9606508085", "KIA SONET"),
            VehicleData("VINOD JI", "KA 05 NH 0551", "9591757630", "BREZZA (BLACK)"),
            VehicleData("VISHAL JI", "PB 70 F 9405", "8283850535", ""),
            VehicleData("SANJAY JI", "KA 05 NE 8711", "9845767293", ""),
            VehicleData("HIMANSHU JI", "UP 14 CD 2774", "8800367373", ""),
            VehicleData("SUDEER JI", "MH 03 EB 0456", "9619580340", ""),
            VehicleData("VIVEK NANDAN JI", "GJ 06 LS 9658", "9764097711", "XCENT (WHITE)"),
            VehicleData("ABHAY SAGGU JI", "KA 04 NA 8227", "9739096996", "XL 6 (MAROON)"),
            VehicleData("ASHWIN JI", "KA MH 05 5395", "8178530209", "HYUNDAI XCENT"),
            VehicleData("AKASHJEET JI", "KA 04 MZ 7180", "9731481880", "XUV 300"),
            VehicleData("RAVI JI", "KA 53 MK 5240", "7503179779", "NEXON"),
            VehicleData("AVNISH JI", "KA 51 MS 9632", "7406000146", "XUV 300 (BLACK)"),
            VehicleData("ARPIT JI", "KA 02 ME 9876", "8618641362", "WAGON R"),
            VehicleData("PANU RANG JI", "KA 02 AE 7247", "8450002062", "TATA INDICA (SILVER)"),
            VehicleData("SUMIT KUMAR JI", "KA 01 MX 9641", "9988434984", "ALTROZ"),
            VehicleData("ANKITA MEHRA JI", "KA 34 P 3166", "9673011121", "HYUNDAI (RED)"),
            VehicleData("SANJAY NIRANKARI", "KA 51 MU 8847", "8792211863", "TIAGO"),
            VehicleData("AJAY PRASAD", "KA 03 NN 5420", "7892502746", "NEXON (DARK)"),
            VehicleData("AVINASH", "KA 04 MJ 6209", "9019897097", "ALTO K10"),
            VehicleData("VIVEK JI", "KA 04 MY 9540", "7349342024", "HARRIER"),
            VehicleData("SUMIT CHHABRA JI", "KA 01 MW 9844", "9716240551", "TATA PUNCH"),
            VehicleData("SUSHIL KUMAR JI", "KA 01 MW 5993", "8249581494", ""),
            VehicleData("VIKAS SONI JI ", "KA 51 MJ 1419", "8310827415", "SWIFT")
            )
        lifecycleScope.launch {
            for (vehicle in vehicles) {
                val existingVehicle = unaddedVehicleDao.getVehicleByVehicle_no(vehicle.VehicleNumber)
                if (existingVehicle == null) {
                    val maxId = unaddedVehicleDao.getMaxId() ?: 0
                    val nextId = maxId + 1
                    unaddedVehicleDao.insert(DialogVehicleEntity(DialogId = nextId, DialogName = vehicle.name,
                        DialogVehicle_no = vehicle.VehicleNumber, DialogMobile_no = vehicle.Mobile_no, DialogModel = vehicle.Model))
                }
            }
        }
    }

    private fun editRecord(Id:Int, vehicledao:DialogVehicleDAO){
        val editDialogVehicle = Dialog(this)

        var binding4 = EditDialogVehicleBinding.inflate(layoutInflater)
        editDialogVehicle.setContentView(binding4.root)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(editDialogVehicle.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        editDialogVehicle.window?.attributes = layoutParams

        lifecycleScope.launch{
            vehicledao.fetchDialogVehicleById(Id).collect(){
                binding4.editName.setText(it.DialogName)
                binding4.editVehicleNo.setText(it.DialogVehicle_no)
                binding4.editMobileNo.setText(it.DialogMobile_no)
                binding4.editModelNo.setText(it.DialogModel)
            }
        }
        editDialogVehicle.show()


        binding4.editRecord.setOnClickListener {
            val name = binding4.editName.text.toString()
            val vehicle_no = binding4.editVehicleNo.text.toString()
            val mobile_no = binding4.editMobileNo.text.toString()
            val model = binding4.editModelNo.text.toString()

            lifecycleScope.launch{
                vehicledao.update(DialogVehicleEntity(DialogId = Id,DialogName = name, DialogVehicle_no = vehicle_no, DialogMobile_no = mobile_no, DialogModel = model))
            }
            editDialogVehicle.dismiss()
        }


    }

}