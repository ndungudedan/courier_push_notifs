package com.courier.notifications

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.courier.android.Courier
import com.courier.android.activity.CourierActivity
import com.courier.android.models.CourierAuthenticationListener
import com.courier.android.modules.addAuthenticationListener
import com.courier.android.modules.getFCMToken
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

    private val notificationCountLiveData = MutableLiveData<Int>().apply {
        value=0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Courier.initialize(this)

        notificationCountLiveData.observe(this, Observer { count ->
            binding.notifsCount.text = count.toString()
        })

        if (Courier.shared.isUserSignedIn) {
            binding.signInBtn.text = "Sign Out"
            binding.user.text = "Welcome ${Courier.shared.userId}"
        } else {
            binding.signInBtn.text = "Sign In"
        }

        binding.signInBtn.setOnClickListener {
            if (!Courier.shared.isUserSignedIn) {
                Log.d("BTN CLICK: ", "Signing APP User IN")
                // Shows the request notification popup
                this.requestNotificationPermission()

                val isGranted = this.isPushPermissionGranted ?: false

                if (isGranted) {
                    lifecycleScope.launch {
                        // Saves credentials locally and accesses the Courier API with them
                        // Uploads push notification devices tokens to Courier if needed
                        Courier.shared.signIn(
                            accessToken = "pk_prod_PHGK9FKHTZ4Z5VJ42E1813JRETCC",
                            clientKey = "MDA1NjI5ODEtZDkyOC00YjYwLWE4ZGUtMTExYjU4MTI2MWUz",
                            userId = "appUser1"
                        )
                       val fcmToken= Courier.shared.getFCMToken()
                        Log.d("token",fcmToken?:"")
                    }
                }else{
                    Toast.makeText(this, "You need to enable push notifications permissions", Toast.LENGTH_LONG).show()
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
                    binding.user.text = userId
                } else {
                    binding.signInBtn.text = "Sign In"
                    binding.user.text = ""
                }
            }
        }
    }

    override fun onPushNotificationClicked(message: RemoteMessage) {
        Toast.makeText(this, "Message clicked:\n${message.data}", Toast.LENGTH_LONG).show()
    }

    override fun onPushNotificationDelivered(message: RemoteMessage) {
        Toast.makeText(this, "Message delivered:\n${message.data}", Toast.LENGTH_LONG).show()
            notificationCountLiveData.value = notificationCountLiveData.value!! + 1
    }

    override fun onDestroy() {
        super.onDestroy()
        listener.remove()
    }

}