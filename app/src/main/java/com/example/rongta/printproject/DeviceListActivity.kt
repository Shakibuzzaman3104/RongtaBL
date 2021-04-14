package com.example.rongta.printproject

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rongta.R
import com.example.rongta.printproject.DeviceListActivity

class DeviceListActivity : Activity() {
    // Member fields

    val PERMISSION_REQUEST_BACKGROUND_LOCATION = 110
    private var mBtAdapter: BluetoothAdapter? = null
    private var mPairedDevicesArrayAdapter: ArrayAdapter<String>? = null
    private var mNewDevicesArrayAdapter: ArrayAdapter<String>? = null
    private var alertDialogBuilder: AlertDialog.Builder? = null
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.bluetooth_device_list)

        // Set result CANCELED incase the user backs out
        setResult(RESULT_CANCELED)
        alertDialogBuilder = AlertDialog.Builder(
                applicationContext)

        // Initialize the button to perform device discovery
        val scanButton = findViewById<View>(R.id.button_scan) as Button
        scanButton.setOnClickListener { checkPermission() }

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = ArrayAdapter(this, R.layout.list_layout_bluetooth_device)
        mNewDevicesArrayAdapter = ArrayAdapter(this, R.layout.list_layout_bluetooth_device)

        // Find and set up the ListView for paired devices
        val pairedListView = findViewById<View>(R.id.paired_devices) as ListView
        pairedListView.adapter = mPairedDevicesArrayAdapter
        pairedListView.onItemClickListener = mDeviceClickListener

        // Find and set up the ListView for newly discovered devices
        val newDevicesListView = findViewById<View>(R.id.new_devices) as ListView
        newDevicesListView.adapter = mNewDevicesArrayAdapter
        newDevicesListView.onItemClickListener = mDeviceClickListener

        // Register for broadcasts when a device is discovered
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        // Get a set of currently paired devices
        val pairedDevices = mBtAdapter?.bondedDevices

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices?.size!! > 0) {
            findViewById<View>(R.id.title_paired_devices).visibility = View.VISIBLE
            for (device in pairedDevices) {
                mPairedDevicesArrayAdapter!!.add("${device.name}${device.address}".trimIndent())
            }
        } else {
            val noDevices = resources.getText(R.string.none_paired).toString()
            mPairedDevicesArrayAdapter!!.add(noDevices)
        }
    }


        @RequiresApi(Build.VERSION_CODES.M)
        private fun checkPermission()
        {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        val builder =
                                AlertDialog.Builder(this)
                        builder.setTitle("This app needs background location access")
                        builder.setMessage("Please grant location access so this app can detect beacons in the background.")
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.setOnDismissListener {

                            requestPermissions(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                    PERMISSION_REQUEST_BACKGROUND_LOCATION
                            )
                        }
                        builder.show()
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val builder =
                                    AlertDialog.Builder(this)
                            builder.setTitle("Functionality limited")
                            builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.")
                            builder.setPositiveButton(android.R.string.ok, null)
                            builder.setOnDismissListener {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri: Uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                // This will take the user to a page where they have to click twice to drill down to grant the permission
                                startActivity(intent)
                            }
                            builder.show()
                        }
                    }
                }
                else
                {
                    mNewDevicesArrayAdapter!!.clear()
                    doDiscovery()
                }
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(
                            arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                    /*Manifest.permission.ACCESS_BACKGROUND_LOCATION*/
                            ),
                            PERMISSIONS_REQUEST_LOCATION
                    )
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        // This will take the user to a page where they have to click twice to drill down to grant the permission
                        startActivity(intent)
                    }
                    builder.show()
                }
            }
        }

    private fun permissionChecking() {
        //--------------------------------------LOCATION Requesting Permission at Run Time--------------------------------------
        // Here, thisActivity is the current activity
        if (CURRENT_ANDROID_VERSION >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { //Marshmallow
            if (ContextCompat.checkSelfPermission(this@DeviceListActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation? This method returns true if the app has requested this
                // permission previously and the user denied the request.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@DeviceListActivity,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    alertDialogBuilder!!.setTitle("Location Service")
                    alertDialogBuilder!!
                            .setMessage("We need to access your device's location service in order to use bluetooth.")
                            .setCancelable(false)
                            .setPositiveButton("Allow") { dialog: DialogInterface, id: Int ->
                                ActivityCompat.requestPermissions(this@DeviceListActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                        PERMISSIONS_REQUEST_LOCATION)
                                dialog.cancel()
                            }
                            .setNegativeButton("Not allow") { dialog: DialogInterface, id: Int -> dialog.cancel() }

                    // create alert dialog
                    val alertDialog = alertDialogBuilder!!.create()
                    alertDialog.show()
                } else { // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this@DeviceListActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                            PERMISSIONS_REQUEST_LOCATION)

                    // MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else { //permission already granted
                mNewDevicesArrayAdapter!!.clear()
                doDiscovery()
            }
        } else { //android version lower than 23
            mNewDevicesArrayAdapter!!.clear()
            doDiscovery()
        }
    }

    //When your app requests permissions, the system presents a dialog box to the user.
    //When the user responds, the system invokes your app's onRequestPermissionsResult()
    //method, passing it the user response.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mNewDevicesArrayAdapter!!.clear()
                    doDiscovery()
                } else {
                    Toast.makeText(this@DeviceListActivity, "Permission is Denied!", Toast.LENGTH_LONG).show()
                }
                return
            }

            PERMISSION_REQUEST_BACKGROUND_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mNewDevicesArrayAdapter!!.clear()
                    doDiscovery()
                } else {
                    Toast.makeText(this@DeviceListActivity, "Permission is Denied!", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter!!.cancelDiscovery()
        }

        // Unregister broadcast listeners
        unregisterReceiver(mReceiver)
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private fun doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()")

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true)
        setTitle(R.string.scanning)

        // Turn on sub-title for new devices
        findViewById<View>(R.id.title_new_devices).visibility = View.VISIBLE

        // If we're already discovering, stop it
        if (mBtAdapter!!.isDiscovering) {
            mBtAdapter!!.cancelDiscovery()
        }

        // Request discover from BluetoothAdapter
        mBtAdapter!!.startDiscovery()
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private val mDeviceClickListener = OnItemClickListener { av, v, arg2, arg3 -> // Cancel discovery because it's costly and we're about to connect
        mBtAdapter!!.cancelDiscovery()

        // Get the device MAC address, which is the last 17 chars in the View
        val info = (v as TextView).text.toString()
        val address = info.substring(info.length - 17)

        // Create the result Intent and include the MAC address
        val intent = Intent()
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address)

        // Set result and finish this Activity
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * The BroadcastReceiver that listens for discovered devices and
     * changes the title when discovery is finished
     */
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Get the BluetoothDevice object from the Intent
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // If it's already paired, skip it, because it's been listed already
                if (device!!.bondState != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter!!.add("${device.name}${device.address}".trimIndent())
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setProgressBarIndeterminateVisibility(false)
                setTitle(R.string.select_device)
                if (mNewDevicesArrayAdapter!!.count == 0) {
                    val noDevices = resources.getText(R.string.none_found).toString()
                    mNewDevicesArrayAdapter!!.add(noDevices)
                }
            }
        }
    }

    companion object {
        // Debugging
        private const val TAG = "DeviceListActivity"
        private const val D = true
        const val PERMISSIONS_REQUEST_LOCATION = 100
        val CURRENT_ANDROID_VERSION = Build.VERSION.SDK_INT

        // Return Intent extra
        @JvmField
        var EXTRA_DEVICE_ADDRESS = "device_address"
    }
}