package com.example.firebase

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.firebase.databinding.ActivityMainBinding
import com.example.firebase.ui.SharedViewModel
import com.google.android.gms.location.LocationServices


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var locationPermissionRequest: ActivityResultLauncher<Array<String>>? = null
    private var sharedViewModel : SharedViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val navView : BottomNavigationView = findViewById(R.id.nav_view)

        val appBarConfiguration: AppBarConfiguration = AppBarConfiguration.Builder(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
        )
            .build()
        val navController = findNavController(this, R.id.nav_host_fragment_activity_main)
        setupActionBarWithNavController(this, navController, appBarConfiguration)
        setupWithNavController(binding.navView, navController)

        sharedViewModel = ViewModelProvider(this).get(
            SharedViewModel::class.java
        )

        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedViewModel?.setFusedLocationClient(mFusedLocationClient)

        sharedViewModel?.getCheckPermission()?.observe(this) { s -> checkPermission() }

        locationPermissionRequest = registerForActivityResult( ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fineLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)
            val coarseLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false)
            if (fineLocationGranted != null && fineLocationGranted) {
                sharedViewModel?.startTrackingLocation(false);
            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                sharedViewModel?.startTrackingLocation(false);
            } else {
                Toast.makeText(this, "No concedeixen permisos", Toast.LENGTH_SHORT).show();
            }
        }

//        navView: BottomNavigationView = binding.navView
//
//        val navController = findNavController(R.id.nav_host_fragment_activity_main)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
    }
    fun checkPermission(){
        Log.i("PERMISSIONS","Check Permissions")
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("PERMISSIONS", "Request permisssions")
            locationPermissionRequest!!.launch(
                arrayOf<String>(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            sharedViewModel!!.startTrackingLocation(false)
        }
    }
}