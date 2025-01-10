package com.example.firebase.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.controls.ControlsProviderService.TAG
import android.text.TextUtils
import android.util.Log
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
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


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
                    getLocation()
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    getLocation()
                } else {
                    Toast.makeText(requireContext(), "No concedeixen permisos", Toast.LENGTH_SHORT)
                        .show()
                }
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
            mFusedLocationClient!!.lastLocation.addOnSuccessListener {
                location ->
                if (location != null) {
                    fetchAddress(location)
                } else {
                    binding.localitzacio.text = "Sense localització coneguda"
                }
            }
        }
        binding.localitzacio.setText("Carregant...")
    }

    fun fetchAddress( location: Location){
        var executor : ExecutorService = Executors.newSingleThreadExecutor()
        var handler : Handler = Handler(Looper.getMainLooper())

        var geocoder : Geocoder = Geocoder(requireContext(), Locale.getDefault())
        executor.execute {
          var addresses : List<Address>? = null
          var resultMessage: String = ""

          try {
              addresses = geocoder.getFromLocation(
                  location.latitude,
                  location.longitude,
                  1
              )

              if (addresses == null || addresses.size == 0){
                  if (resultMessage.isEmpty()) {
                      resultMessage = "No se ha encontrado ninguna ubicacion"
                      Log.e("INCISIVE" , resultMessage)
                  }
              } else {
                  var address : Address = addresses.get(0)
                  var addressParts : ArrayList<String>? = null
                  for (i in 0..address.maxAddressLineIndex){
                      addressParts?.add(address.getAddressLine(i))
                  }
                  resultMessage = TextUtils.join("\n", addressParts!!)
                  var finalResultManager : String = resultMessage
                  handler.post {
                   binding.localitzacio.setText(String.format(
                       "Direcció: %1$s \n Hora: %2$tr",
                       finalResultManager, System.currentTimeMillis()))
                  }
              }
          }catch (e : IOException){
              resultMessage = "Servicio no disponible"
              Log.e("INCISIVE", resultMessage, e)
          } catch (e : IllegalArgumentException){
              resultMessage = "Coordenadas no validas"
              Log.e("INCISIVE", resultMessage + ". "+ "Latitude = "+ location.latitude + ", Longitude = "+ location.longitude, e)
          }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

