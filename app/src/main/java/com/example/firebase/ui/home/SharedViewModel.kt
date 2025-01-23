package com.example.firebase.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.firebase.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Executors


class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private var binding: FragmentHomeBinding? = null
    private val app: Application = application
    val currentAddress: MutableLiveData<String> = MutableLiveData()
    private val checkPermission = MutableLiveData<String>()
    private val buttonText = MutableLiveData<String>()
    private val progressBar = MutableLiveData<Boolean>()

    private var mTrackingLocation : Boolean = false
    var mFusedLocationClient : FusedLocationProviderClient? = null

    fun getCurrentAddress() : LiveData<String> {
        return currentAddress;
    }

    fun getButtonText(): MutableLiveData<String> {
        return buttonText
    }

    fun getProgressBar(): MutableLiveData<Boolean> {
        return progressBar
    }

    fun setFusedLocationClient(mFusedLocationClient: FusedLocationProviderClient?) {
        this.mFusedLocationClient = mFusedLocationClient
    }

    fun getCheckPermission(): LiveData<String>{
        return checkPermission
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult != null) {
                fetchAddress(locationResult.lastLocation)
            }
        }
    }

    val locationRequest: LocationRequest
        get() {
            val locationRequest: LocationRequest = LocationRequest()
            locationRequest.setInterval(10000)
            locationRequest.setFastestInterval(5000)
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            return locationRequest
        }

    fun switchTrackingLocation() {
        if (!mTrackingLocation) {
            startTrackingLocation(true)
        } else {
            stopTrackingLocation()
        }
    }

    private fun stopTrackingLocation() {
        if (mTrackingLocation!!) {
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
            mTrackingLocation = false
            progressBar.postValue(false)
            buttonText.setValue("Comença a seguir la ubicació")
        }
    }

    @SuppressLint("MissingPermission")
    fun startTrackingLocation(needsChecking: Boolean) {
        if (needsChecking) {
            checkPermission.postValue("check");
        } else {
            mFusedLocationClient?.requestLocationUpdates(
                locationRequest,
                mLocationCallback, null
            );

            currentAddress.postValue("Carregant...");

            progressBar.postValue(true);
            mTrackingLocation = true;
            buttonText.setValue("Aturar el seguiment de la ubicació");
        }
    }


    private fun fetchAddress(location: Location?) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        val geocoder = Geocoder(app.applicationContext, Locale.getDefault())

        executor.execute {
            var addresses: List<Address>? = null
            var resultMessage = ""
            try {
                addresses = geocoder.getFromLocation(
                    location?.latitude!!,
                    location?.longitude!!,
                    1
                )


                if (addresses == null || addresses.size == 0) {
                    if (resultMessage.isEmpty()) {
                        resultMessage = "No s'ha trobat cap adreça"
                        Log.e("INCIVISME", resultMessage)
                    }
                } else {
                    val address = addresses[0]
                    val addressParts =
                        ArrayList<String?>()

                    for (i in 0..address.maxAddressLineIndex) {
                        addressParts.add(address.getAddressLine(i))
                    }

                    resultMessage = TextUtils.join("\n", addressParts)
                    val finalResultMessage = resultMessage
                    handler.post {
                        if (mTrackingLocation) binding?.localitzacio?.text = String.format(
                            "Direcció: %1\$s \n Hora: %2\$tr",
                            finalResultMessage,
                            System.currentTimeMillis()
                        )
                    }
                }
            } catch (ioException: IOException) {
                resultMessage = "Servei no disponible"
                Log.e("INCIVISME", resultMessage, ioException)
            } catch (illegalArgumentException: IllegalArgumentException) {
                resultMessage = "Coordenades no vàlides"
                Log.e(
                    "INCIVISME",
                    resultMessage + ". " + "Latitude = " + location!!.latitude + ", Longitude = " + location.longitude,
                    illegalArgumentException
                )
            }
        }
    }
}
