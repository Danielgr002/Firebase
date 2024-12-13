package com.example.firebase.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.firebase.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val locationPermissionRequest:  ActivityResultLauncher<String>? = null
    private var mLastLocation: Location? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.localitzacio
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        val locationPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val fineLocationGranted: Boolean = permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION, false
                )
                val coarseLocationGranted: Boolean = permissions.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION, false
                )
                if (fineLocationGranted != null && fineLocationGranted) {
                    getLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    getLocation()
                } else {
                    Toast.makeText(requireContext(), "No concedeixen permisos", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fun OnClickListener() {
            getLocation()
        }
        return root
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(requireContext(), "Request permisssions", Toast.LENGTH_SHORT).show()
            locationPermissionRequest?.launch( arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).toString())
        } else {
            mFusedLocationClient!!.lastLocation.addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    mLastLocation = location
                    binding.localitzacio.text = String.format(
                        "Latitud: %1$.4f \n Longitud: %2$.4f\n Hora: %3\$tr",
                        mLastLocation.getLatitude(),
                        mLastLocation.getLongitude(),
                        mLastLocation.getTime()
                    )
                } else {
                    binding.localitzacio.text = "Sense localització coneguda"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}