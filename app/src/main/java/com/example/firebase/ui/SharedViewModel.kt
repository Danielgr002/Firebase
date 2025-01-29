package com.example.firebase.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.firebase.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import java.io.IOException
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private var binding: FragmentHomeBinding? = null
    private val app: Application = application
    private val currentAddress: MutableLiveData<String> = MutableLiveData()
    private val checkPermission = MutableLiveData<String>()
    private val buttonText = MutableLiveData<String>()
    private val progressBar = MutableLiveData<Boolean>()
    private var mTrackingLocation = false
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var user : MutableLiveData<FirebaseUser> = MutableLiveData()
    private var signInLauncher : ActivityResultLauncher<Intent>? = null
    private val currentLatLng = MutableLiveData<LatLng>()

    fun getCurrentLatLng(): MutableLiveData<LatLng>  {
        return currentLatLng;
    }

    fun getCurrentAddress(): LiveData<String> {
        return currentAddress
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

    fun getCheckPermission(): LiveData<String> {
        return checkPermission
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { fetchAddress(it) }
        }
    }

    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .build()
    }


    fun switchTrackingLocation() {
        if (!mTrackingLocation) {
            startTrackingLocation(true)
        } else {
            stopTrackingLocation()
        }
    }

    private fun stopTrackingLocation() {
        if (mTrackingLocation) {
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
            mTrackingLocation = false
            progressBar.postValue(false)
            buttonText.value = "Comença a seguir la ubicació"
        }
    }

    @SuppressLint("MissingPermission")
    fun startTrackingLocation(needsChecking: Boolean) {
        if (needsChecking) {
            checkPermission.postValue("check")
        } else {
            mFusedLocationClient?.requestLocationUpdates(
                getLocationRequest(),
                mLocationCallback,
                Looper.getMainLooper()
            )

            currentAddress.postValue("Carregant...")

            progressBar.postValue(true)
            mTrackingLocation = true
            buttonText.setValue("Aturar el seguiment de la ubicació")
        }
    }


    private fun fetchAddress(location: Location) {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val geocoder = Geocoder(app.applicationContext, Locale.getDefault())

        executor.execute {
            var resultMessage = ""
            try {
                var latlng: LatLng = LatLng(location.latitude, location.longitude)
                currentLatLng.postValue(latlng)
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses.isNullOrEmpty()){
                    resultMessage = "NO se ha trobat la adreça"
                    Log.e("INCISIVE", resultMessage)
                } else{
                    val address = addresses[0]
                    val addressParts = mutableListOf<String>()
                    for (i in 0..address.maxAddressLineIndex) {
                        addressParts.add(address.getAddressLine(i))
                    }
                    resultMessage = TextUtils.join("\n", addressParts)
                }
            } catch (ioException: IOException) {
                resultMessage = "Servei no disponible"
                Log.e("INCIVISME", resultMessage, ioException)
            } catch (illegalArgumentException: IllegalArgumentException) {
                resultMessage = "Coordenades no vàlides"
                Log.e(
                    "INCIVISME",
                    resultMessage + ". " + "Latitude = " + location.latitude + ", Longitude = " + location.longitude,
                    illegalArgumentException
                )
            }

            val finalResultMessage = resultMessage
            handler.post {
                if (mTrackingLocation) {
                    currentAddress.postValue("Direcci: $finalResultMessage")
                }
            }
        }
    }

    fun getUser(): LiveData<FirebaseUser> {
        return user
    }

    fun setUser(passedUser : FirebaseUser){
        user.postValue(passedUser)
    }
}


