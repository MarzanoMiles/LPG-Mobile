package com.example.gastrack.data.remote.dto

data class OrderDto(
    val OrderID: Int,
    val CustomerID: Int,
    val OrderNo: String,
    val OrderType: String,
    val OrderStatus: String,
    val TotalAmount: String,
    val OrderDate: String,
    val CustomerName: String,
    val ItemCount: Int
)