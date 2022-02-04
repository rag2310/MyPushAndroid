package com.example.mypushandroid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            findViewById<TextView>(R.id.text_view_notification).text =
                intent.extras?.getString("message")
        }
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(messageReceiver, IntentFilter("MyData"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
        val bundle = intent.extras
        if (bundle != null) {
            findViewById<TextView>(R.id.text_view_notification).text = bundle.getString("text")
        }

        findViewById<Button>(R.id.button_retrieve_token).setOnClickListener {
            if (checkGooglePlayServices()) {
                FirebaseInstallations.getInstance().id.addOnCompleteListener(
                    OnCompleteListener { task ->
                        // 2
                        if (!task.isSuccessful) {
                            Log.w(
                                MainActivity::class.java.simpleName,
                                "getInstanceId failed",
                                task.exception
                            )
                            return@OnCompleteListener
                        }
                        // 3
                        val token = task.result

                        // 4
                        val msg = getString(R.string.token_prefix, token)
                        Log.d(MainActivity::class.java.simpleName, msg)
                        Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()

                    }
                )

                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(MainActivity::class.java.simpleName, "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    val token = task.result

                    // Log and toast
                    val msg = getString(R.string.token_prefix, token)
                    Log.d(MainActivity::class.java.simpleName, msg)
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                })

                auth.signInWithCustomToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwczovL2lkZW50aXR5dG9vbGtpdC5nb29nbGVhcGlzLmNvbS9nb29nbGUuaWRlbnRpdHkuaWRlbnRpdHl0b29sa2l0LnYxLklkZW50aXR5VG9vbGtpdCIsImlhdCI6MTY0MzkyNDgzOCwiZXhwIjoxNjQzOTI4NDM4LCJpc3MiOiJmaXJlYmFzZS1hZG1pbnNkay1sejZpbEBwdXNoLWFuZHJvaWQtanMuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iLCJzdWIiOiJmaXJlYmFzZS1hZG1pbnNkay1sejZpbEBwdXNoLWFuZHJvaWQtanMuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iLCJ1aWQiOiJyYWcyMzEwMTIzIn0.TXNSBnEomK0MmDZYXdiwBPtIJ8yNUf7jeZw6JzrQFKIphltHPDp57RZkEjdkPcz5tmIGWwo3FHqUqOZWBTTGtbyLTUFukDKHM0PYQWdFxgDIuK0CIbuc8XuJuwzgyx0ZhcuJDv9pkcNpG39Vo0bxWXiqlcATNai9Ctm7TbsXuvPrFQ7kpdkJ-lQ3VqNat1r9VMTeaaTh9YYtnLasyopVrdp0AZX56S7MbUKw7iroISN8d3THc--ippFY3fTZb-LAIB-OlJqBqckba4bZZfRRXxk3ioBBwSkiAixPcNTXLEmqPI7BybCBjiCOLlFaax5vWyiYYIAbdtmFtD3KiJPqWg")
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCustomToken:success")
                            auth.currentUser!!.getIdToken(true).addOnCompleteListener { task1 ->
                                if (task1.isSuccessful) {
                                    val idToken = task1.result.token
                                    val msg = getString(R.string.token_prefix, idToken)
                                    Log.d(MainActivity::class.java.simpleName, msg)
                                } else {
                                    // Handle error -> task.getException();
                                }
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                //You won't be able to send notifications to this device
                Log.w(
                    MainActivity::class.java.simpleName,
                    "Device doesn't have google play services"
                )
            }

        }
    }

    private fun checkGooglePlayServices(): Boolean {
        // 1
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        // 2
        return if (status != ConnectionResult.SUCCESS) {
            Log.e(MainActivity::class.java.simpleName, "Error")
            // ask user to update google play services and manage the error.
            false
        } else {
            // 3
            Log.i(MainActivity::class.java.simpleName, "Google play services updated")
            true
        }
    }

    companion object {
        private var TAG = MainActivity::class.java.simpleName
    }
}