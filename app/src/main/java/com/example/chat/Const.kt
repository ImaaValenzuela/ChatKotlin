package com.example.chat

import android.text.format.DateFormat
import java.util.Calendar
import java.util.Locale

object Const {
    fun getTimeD() : Long {
        return System.currentTimeMillis()
    }

    fun dateFormat(time : Long) : String{
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = time

        return DateFormat.format("dd/MM/yyyy", calendar).toString()
    }
}