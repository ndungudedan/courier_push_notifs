package com.courier.notifications

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.courier.android.Courier
import com.courier.android.activity.CourierActivity
import com.courier.android.models.CourierAuthenticationListener
import com.courier.android.modules.addAuthenticationListener
import com.courier.android.modules.isUserSignedIn
import com.courier.android.modules.signIn
import com.courier.android.modules.signOut
import com.courier.android.modules.userId
import com.courier.android.utils.isPushPermissionGranted
import com.courier.android.utils.requestNotificationPermission
import com.courier.notifications.databinding.ActivityMainBinding
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.launch

class MainActivity : CourierActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var listener: CourierAuthenticationListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Courier.initialize(this)

        if (Courier.shared.isUserSignedIn) {
            binding.signInBtn.text = "Sign Out"
            binding.user.text="Welcome ${Courier.shared.userId}"
        }

        binding.signInBtn.setOnClickListener {
            if (Courier.shared.isUserSignedIn) {
                Log.d("BTN CLICK: ", "Signing APP User IN")
                // Shows the request notification popup
                this.applicationContext?.requestNotificationPermission()

// Gets the value of the permission
                val isGranted = this.applicationContext?.isPushPermissionGranted ?: false

                if (isGranted) {
                    lifecycleScope.launch {
                        // Saves credentials locally and accesses the Courier API with them
                        // Uploads push notification devices tokens to Courier if needed
                        Courier.shared.signIn(
                            accessToken = "pk_prod_PHGK9FKHTZ4Z5VJ42E1813JRETCC",
                            //clientKey = "",
                            userId = "logged_in_user_id"
                        )
                    }
                }
            } else {


                Log.d("BTN CLICK: ", "Signing User Out")
                lifecycleScope.launch {
                    // Removes the locally saved credentials
                    // Deletes the user's push notification device tokens in Courier if needed
                    Courier.shared.signOut()
                }
            }
        }



        listener = Courier.shared.addAuthenticationListener { userId ->
            runOnUiThread {
                Log.d("Courier Listener: ", userId ?: "No userId found")

                if (userId != null) {
                    binding.signInBtn.text = "Sign Out"
                    binding.user.text=userId
                } else {
                    binding.signInBtn.text = "Sign In"
                    binding.user.text=""
                }
            }
        }
    }

    override fun onPushNotificationClicked(message: RemoteMessage) {
        Toast.makeText(this, "Message clicked:\n${message.data}", Toast.LENGTH_LONG).show()
    }

    override fun onPushNotificationDelivered(message: RemoteMessage) {
        Toast.makeText(this, "Message delivered:\n${message.data}", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        listener.remove()
    }

    private fun loginUser() {

    }
}