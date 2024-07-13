package com.example.qunltichnh

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast

class NetworkChangeReceiver : BroadcastReceiver() {

    var isConnected: Boolean? = null

    override fun onReceive(context: Context, intent: Intent) {
        val currentConnectionStatus = isNetworkAvailable(context)
        if (isConnected != currentConnectionStatus) {
            isConnected = currentConnectionStatus
            if (currentConnectionStatus) {
                Toast.makeText(context, R.string.have_internet, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.loss_internet, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }
}