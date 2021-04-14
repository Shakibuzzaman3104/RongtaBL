package com.example.rongta.printproject

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.rongta.R
import com.example.rongta.printproject.BluetoothPrinterActivity
import com.example.rongta.printproject.DeviceListActivity
import com.example.rongta.sharedpreference.MySharedPreference
import com.mocoo.hang.rtprinter.driver.Contants
import com.mocoo.hang.rtprinter.driver.HsBluetoothPrintDriver
import java.lang.ref.WeakReference

class BluetoothPrinterActivity : AppCompatActivity() {
    private var alertDlgBuilder: AlertDialog.Builder? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null

    lateinit var sharedPreferences:MySharedPreference

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "+++ ON CREATE +++")
        setContentView(R.layout.bluetooth_printer_activity)
        CONTEXT = applicationContext
        alertDlgBuilder = AlertDialog.Builder(this@BluetoothPrinterActivity)

        sharedPreferences = MySharedPreference.getPreferencesInstance(applicationContext)
        // Get device's Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // If the adapter is null, then Bluetooth is not available in your device
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if(sharedPreferences.getPrinter()!=null)
        {
            if (BLUETOOTH_PRINTER == null) {
                initializeBluetoothDevice()
            }
            device = mBluetoothAdapter!!.getRemoteDevice(sharedPreferences.getPrinter())
            BLUETOOTH_PRINTER!!.start()
            BLUETOOTH_PRINTER!!.connect(device)
        }
        //Initialize widgets
        InitUIControl()
    }

    private fun InitUIControl() {
        txtPrinterStatus = findViewById<View>(R.id.txtPrinerStatus) as TextView
        mBtnConnetBluetoothDevice = findViewById<View>(R.id.btn_connect_bluetooth_device) as Button
        mBtnConnetBluetoothDevice!!.setOnClickListener(mBtnConnetBluetoothDeviceOnClickListener)
        mBtnPrint = findViewById<View>(R.id.btn_print) as Button
        mBtnPrint!!.setOnClickListener(mBtnPrintOnClickListener)
        mImgPosPrinter = findViewById<View>(R.id.printer_imgPOSPrinter) as ImageView
    }

    public override fun onStart() {
        super.onStart()
        Log.e(TAG, "++ ON START ++")

        // If BT is not on, request that to be enabled.
        // initializeBluetoothDevice() will then be called during onActivityResult
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
            // Otherwise, setup the chat session
        } else {
            if (BLUETOOTH_PRINTER == null) {
                initializeBluetoothDevice()
            } else {
                if (BLUETOOTH_PRINTER!!.IsNoConnection()) {
                    mImgPosPrinter!!.setImageResource(R.drawable.pos_printer_offliine)
                } else {
                    txtPrinterStatus!!.setText(R.string.title_connected_to)
                    txtPrinterStatus!!.append(device!!.name)
                    mImgPosPrinter!!.setImageResource(R.drawable.pos_printer)
                }
            }
        }
    }

    private fun initializeBluetoothDevice() {
        Log.d(TAG, "setupChat()")
        // Initialize HsBluetoothPrintDriver class to perform bluetooth connections
        BLUETOOTH_PRINTER = HsBluetoothPrintDriver.getInstance() //
        BLUETOOTH_PRINTER?.setHandler(BluetoothHandler(this@BluetoothPrinterActivity))
    }

    /**
     * The Handler that gets information back from Bluetooth Devices
     */
    internal class BluetoothHandler(weakReference: BluetoothPrinterActivity) : Handler() {
        private val myWeakReference: WeakReference<BluetoothPrinterActivity>
        override fun handleMessage(msg: Message) {
            val bluetoothPrinterActivity = myWeakReference.get()
            if (bluetoothPrinterActivity != null) {
                super.handleMessage(msg)
                val data = msg.data
                when (data.getInt("flag")) {
                    Contants.FLAG_STATE_CHANGE -> {
                        val state = data.getInt("state")
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: $state")
                        when (state) {
                            HsBluetoothPrintDriver.CONNECTED_BY_BLUETOOTH -> {
                                txtPrinterStatus!!.setText(R.string.title_connected_to)
                                txtPrinterStatus!!.append(device!!.name)
                                StaticValue.isPrinterConnected = true
                                Toast.makeText(CONTEXT, "Connection successful.", Toast.LENGTH_SHORT).show()
                                mImgPosPrinter!!.setImageResource(R.drawable.pos_printer)
                            }
                            HsBluetoothPrintDriver.FLAG_SUCCESS_CONNECT -> txtPrinterStatus!!.setText(R.string.title_connecting)
                            HsBluetoothPrintDriver.UNCONNECTED -> txtPrinterStatus!!.setText(R.string.no_printer_connected)
                        }
                    }
                    Contants.FLAG_SUCCESS_CONNECT -> txtPrinterStatus!!.setText(R.string.title_connecting)
                    Contants.FLAG_FAIL_CONNECT -> {
                        Toast.makeText(CONTEXT, "Connection failed.", Toast.LENGTH_SHORT).show()
                        mImgPosPrinter!!.setImageResource(R.drawable.pos_printer_offliine)
                    }
                    else -> {
                    }
                }
            }
        }

        //Creating weak reference of BluetoothPrinterActivity class to avoid any leak
        init {
            myWeakReference = WeakReference(weakReference)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult $resultCode")
        when (requestCode) {
            REQUEST_CONNECT_DEVICE ->                 // When DeviceListActivity returns with a device to connect
                if (resultCode == RESULT_OK) {
                    // Get the device MAC address
                    val address = data!!.extras?.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
                    sharedPreferences.setPrinter(address)
                    // Get the BLuetoothDevice object
                    device = mBluetoothAdapter!!.getRemoteDevice(address)
                    // Attempt to connect to the device
                    BLUETOOTH_PRINTER!!.start()
                    BLUETOOTH_PRINTER!!.connect(device)

                }
            REQUEST_ENABLE_BT ->                 // When the request to enable Bluetooth returns
                if (resultCode == RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    initializeBluetoothDevice()
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled")
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    var mBtnQuitOnClickListener = View.OnClickListener { // Stop the Bluetooth chat services
        if (!PrintReceipt.printBillFromOrder(applicationContext)) {
            Toast.makeText(this@BluetoothPrinterActivity, "No printer is connected!!", Toast.LENGTH_LONG).show()
        }
    }
    var mBtnPrintOnClickListener = View.OnClickListener { PrintReceipt.printBillFromOrder(this@BluetoothPrinterActivity) }
    var mBtnConnetBluetoothDeviceOnClickListener: View.OnClickListener = object : View.OnClickListener {
        var serverIntent: Intent? = null
        override fun onClick(arg0: View) {

            //If bluetooth is disabled then ask user to enable it again
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
            } else { //If the connection is lost with last connected bluetooth printer
                if (BLUETOOTH_PRINTER!!.IsNoConnection()) {
                    serverIntent = Intent(this@BluetoothPrinterActivity, DeviceListActivity::class.java)
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE)
                } else { //If an existing connection is still alive then ask user to kill it and re-connect again
                    alertDlgBuilder!!.setTitle(resources.getString(R.string.alert_title))
                    alertDlgBuilder!!.setMessage(resources.getString(R.string.alert_message))
                    alertDlgBuilder!!.setNegativeButton(resources.getString(R.string.alert_btn_negative)
                    ) { dialog, which -> }
                    alertDlgBuilder!!.setPositiveButton(resources.getString(R.string.alert_btn_positive)
                    ) { dialog, which ->
                        BLUETOOTH_PRINTER!!.stop()
                        serverIntent = Intent(this@BluetoothPrinterActivity, DeviceListActivity::class.java)
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE)
                    }
                    alertDlgBuilder!!.show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BLUETOOTH_PRINTER!!.IsNoConnection()) BLUETOOTH_PRINTER!!.stop()
    }

    companion object {
        private const val TAG = "BloothPrinterActivity"
        private var device: BluetoothDevice? = null
        private var CONTEXT: Context? = null
        private const val REQUEST_CONNECT_DEVICE = 1
        private const val REQUEST_ENABLE_BT = 2
        @JvmField
        var BLUETOOTH_PRINTER: HsBluetoothPrintDriver? = null
        private var mBtnConnetBluetoothDevice: Button? = null
        private var mBtnPrint: Button? = null
        private var txtPrinterStatus: TextView? = null
        private var mImgPosPrinter: ImageView? = null
    }
}