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
import com.example.firebase.ui.SharedViewModel
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
        val homeViewModel : HomeViewModel = ViewModelProvider(this).get(
            HomeViewModel::class.java
        )

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        sharedViewModel.getCurrentAddress().observe(
            viewLifecycleOwner,
            Observer<String> { address: String? ->
                binding!!.localitzacio.text = String.format(
                    "Direcció: %1\$s \n Hora: %2\$tr",
                    address, System.currentTimeMillis()
                )
            })
        sharedViewModel.getButtonText().observe(viewLifecycleOwner) { s ->
            binding!!.buttonLocation.setText(s
            )
        }
        sharedViewModel.getProgressBar().observe(viewLifecycleOwner) { visible ->
            if (visible) binding?.loading?.visibility = ProgressBar.VISIBLE
            else binding!!.loading.visibility = ProgressBar.INVISIBLE
        }


        binding!!.buttonLocation.setOnClickListener { view ->
            Log.d("DEBUG", "Clicked Get Location")
            sharedViewModel.switchTrackingLocation();
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
