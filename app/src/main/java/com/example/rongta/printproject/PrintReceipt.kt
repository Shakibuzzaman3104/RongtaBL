package com.example.rongta.printproject

import android.content.Context
import com.example.rongta.R
import com.example.rongta.model.SalesModel
import com.example.rongta.utility.Utility

object PrintReceipt {
    fun printBillFromOrder(context: Context): Boolean {
        if (BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.IsNoConnection()) {
            return false
        }
        var totalBill = 0.00
        var netBill = 0.00
        var totalVat = 0.00

        //LF = Line feed
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.Begin()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetAlignMode(1.toByte()) //CENTER
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetLineSpacing(30.toByte()) //30 * 0.125mm
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetFontEnlarge(0x00.toByte()) //normal
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write("Company Name")
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetAlignMode(1.toByte())
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetLineSpacing(30.toByte())
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetFontEnlarge(0x00.toByte())

        //BT_Write() method will initiate the printer to start printing.
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write("""
    Branch Name: Stuttgart Branch
    Order No: 1245784256454
    Bill No: 554741254854
    Trn. Date:29/12/2015
    Salesman:Mr. Salesman
    """.trimIndent())
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write(context.resources.getString(R.string.print_line))
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetAlignMode(0.toByte()) //LEFT
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetLineSpacing(30.toByte()) //50 * 0.125mm
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetFontEnlarge(0x00.toByte()) //normal font

        //static sales record are generated
        SalesModel.generatedMoneyReceipt()
        for (i in StaticValue.arrayListSalesModel.indices) {
            val salesModel = StaticValue.arrayListSalesModel[i]
            BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write(salesModel.productShortName)
            BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
            BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write(" " + salesModel.salesAmount + "x" + salesModel.unitSalesCost +
                    "=" + Utility.doubleFormatter(salesModel.salesAmount * salesModel.unitSalesCost) + "" + StaticValue.CURRENCY)
            BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
            totalBill = totalBill + salesModel.unitSalesCost * salesModel.salesAmount
        }
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write(context.resources.getString(R.string.print_line))
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetAlignMode(2.toByte()) //RIGHT
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetLineSpacing(30.toByte()) //50 * 0.125mm
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetFontEnlarge(0x00.toByte()) //normal font
        totalVat = Utility.doubleFormatter(totalBill * (StaticValue.VAT / 100)).toDouble()
        netBill = totalBill + totalVat
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write("Total Bill:" + Utility.doubleFormatter(totalBill) + "" + StaticValue.CURRENCY)
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write(java.lang.Double.toString(StaticValue.VAT) + "% VAT:" + Utility.doubleFormatter(totalVat) + "" +
                StaticValue.CURRENCY)
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetAlignMode(1.toByte()) //center
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write(context.resources.getString(R.string.print_line))
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetLineSpacing(30.toByte())
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetAlignMode(2.toByte()) //Right
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetFontEnlarge(0x9.toByte())
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write("Net Bill:" + Utility.doubleFormatter(netBill) + "" + StaticValue.CURRENCY)
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetAlignMode(1.toByte()) //center
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetFontEnlarge(0x00.toByte()) //normal font
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write(context.resources.getString(R.string.print_line))
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetAlignMode(0.toByte()) //left
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write("VAT Reg. No:" + StaticValue.VAT_REGISTRATION_NUMBER)
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetAlignMode(0.toByte()) //left
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write(StaticValue.BRANCH_ADDRESS)
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.SetAlignMode(1.toByte()) //Center
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.BT_Write("\n\nThank You\nPOWERED By SIAS ERP")
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        BluetoothPrinterActivity.BLUETOOTH_PRINTER!!.LF()
        return true
    }
}