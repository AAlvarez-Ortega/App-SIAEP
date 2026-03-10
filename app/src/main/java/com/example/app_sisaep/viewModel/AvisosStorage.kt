package com.example.app_sisaep.viewModel

import android.content.Context

object AvisosStorage {

    private const val PREFS = "avisos_prefs"
    private const val KEY = "avisos_vistos"

    fun fueVisto(context: Context, id: String): Boolean {

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val vistos = prefs.getStringSet(KEY, mutableSetOf()) ?: mutableSetOf()

        return vistos.contains(id.toString())

    }

    fun marcarVisto(context: Context, id: String) {

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val vistos = prefs.getStringSet(KEY, mutableSetOf())?.toMutableSet()
            ?: mutableSetOf()

        vistos.add(id.toString())

        prefs.edit()
            .putStringSet(KEY, vistos)
            .apply()

    }

}