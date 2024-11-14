package com.example.chat

import android.text.format.DateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Locale

object Const {

    const val MESSAGE_TYPE_TEXT = "TEXTO"
    const val MESSAGE_TYPE_IMAGE = "IMAGEN"

    fun getTimeD() : Long {
        return System.currentTimeMillis()
    }

    fun dateFormat(time : Long) : String{
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = time

        return DateFormat.format("dd/MM/yyyy", calendar).toString()
    }

    fun getDateHour(time : Long) : String{
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = time

        return DateFormat.format("dd/MM/yyyy hh:mm:a", calendar).toString()
    }

    fun routeChat(receiverUid : String,transmitterUid : String ) : String{
        val arrayUid = arrayOf(receiverUid, transmitterUid)
        Arrays.sort(arrayUid)
        return "${arrayUid[0]}_${arrayUid[1]}" // Concatena ambos uids para saber entre quienes se entabla la conversacion
    }
}