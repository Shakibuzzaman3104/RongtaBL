package com.example.rongta.model

import com.example.rongta.printproject.StaticValue

class SalesModel(productSName: String?, amount: Int, unitSCost: Double) {
    var productShortName: String? = productSName
    var salesAmount = amount
    var unitSalesCost = unitSCost


    companion object {
        fun generatedMoneyReceipt() {
            val salesModel = SalesModel("Vegetable Noodle", 1, 3.0)
            StaticValue.arrayListSalesModel.add(salesModel)
            val salesModel1 = SalesModel("Chicken Fry", 1, 5.0)
            StaticValue.arrayListSalesModel.add(salesModel1)
            val salesModel2 = SalesModel("Coke-Small", 1, 1.0)
            StaticValue.arrayListSalesModel.add(salesModel2)
        }
    }
}