package com.example.firebase.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.firebase.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback


class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private var locationPermissionRequest: ActivityResultLauncher<Array<String>>? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mTrackingLocation = false
    private var mLocationCallback: LocationCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val homeViewModel = ViewModelProvider(this).get(
            HomeViewModel::class.java
        )

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        SharedViewModel.currentAddress.observe(
            viewLifecycleOwner,
            Observer<String> { address: String? ->
                binding!!.localitzacio.text = String.format(
                    "Direcció: %1\$s \n Hora: %2\$tr",
                    address, System.currentTimeMillis()
                )
            })
        sharedViewModel.getButtonText().observe(viewLifecycleOwner) { s ->
            binding!!.buttonLocation.setText(

            )
        }
        sharedViewModel.getProgressBar().observe(viewLifecycleOwner) { visible ->
            if (visible) binding!!.loading.visibility = ProgressBar.VISIBLE
            else binding!!.loading.visibility = ProgressBar.INVISIBLE
        }


        binding!!.buttonLocation.setOnClickListener { view ->
            Log.d("DEBUG", "Clicked Get Location")
            sharedViewModel.switchTrackingLocation();
        }

        return root
    }

//    private fun startTrackingLocation() {
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            Toast.makeText(requireContext(), "Request permisssions", Toast.LENGTH_SHORT).show()
//            locationPermissionRequest!!.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//            )
//        } else {
//            Toast.makeText(requireContext(), "getLocation: permissions granted", Toast.LENGTH_SHORT)
//                .show()
//            mFusedLocationClient!!.requestLocationUpdates(
//                locationRequest,
//                mLocationCallback!!, null
//            )
//        }
//        binding!!.localitzacio.text = "Carregant..."
//        binding!!.loading.setVisibility(ProgressBar.VISIBLE)
//        mTrackingLocation = true
//        binding!!.buttonLocation.text = "Aturar el seguiment de la ubicació"
//    }

//    private val locationRequest: LocationRequest
//        get() {
//            val locationRequest: LocationRequest = LocationRequest()
//            locationRequest.setInterval(10000)
//            locationRequest.setFastestInterval(5000)
//            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//            return locationRequest
//        }
//
//    private fun stopTrackingLocation() {
//        if (mTrackingLocation) {
//            binding?.loading?.setVisibility(ProgressBar.INVISIBLE)
//            mTrackingLocation = false
//            binding!!.buttonLocation.text = "Comença a seguir la ubicació"
//            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback!!)
//        }
//    }

//    private fun fetchAddress(location: Location?) {
//        val executor = Executors.newSingleThreadExecutor()
//        val handler = Handler(Looper.getMainLooper())
//
//        val geocoder = Geocoder(requireContext(), Locale.getDefault())
//
//        executor.execute {
//            // Aquest codi s'executa en segon pla
//            var addresses: List<Address>? = null
//            var resultMessage = ""
//            try {
//                addresses = geocoder.getFromLocation(
//                    location!!.latitude,
//                    location.longitude,  // En aquest cas, sols volem una única adreça:
//                    1
//                )
//
//
//                if (addresses == null || addresses.size == 0) {
//                    if (resultMessage.isEmpty()) {
//                        resultMessage = "No s'ha trobat cap adreça"
//                        Log.e("INCIVISME", resultMessage)
//                    }
//                } else {
//                    val address = addresses[0]
//                    val addressParts =
//                        ArrayList<String?>()
//
//                    for (i in 0..address.maxAddressLineIndex) {
//                        addressParts.add(address.getAddressLine(i))
//                    }
//
//                    resultMessage = TextUtils.join("\n", addressParts)
//                    val finalResultMessage = resultMessage
//                    handler.post {
//                        // Aquest codi s'executa en primer pla.
//                        if (mTrackingLocation) binding!!.localitzacio.text = String.format(
//                            "Direcció: %1\$s \n Hora: %2\$tr",
//                            finalResultMessage,
//                            System.currentTimeMillis()
//                        )
//                    }
//                }
//            } catch (ioException: IOException) {
//                resultMessage = "Servei no disponible"
//                Log.e("INCIVISME", resultMessage, ioException)
//            } catch (illegalArgumentException: IllegalArgumentException) {
//                resultMessage = "Coordenades no vàlides"
//                Log.e(
//                    "INCIVISME",
//                    resultMessage + ". " + "Latitude = " + location!!.latitude + ", Longitude = " + location.longitude,
//                    illegalArgumentException
//                )
//            }
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
