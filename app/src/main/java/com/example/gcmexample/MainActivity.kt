package com.example.gcmexample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gcmexample.ui.theme.GCMExampleTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
import com.google.firebase.analytics.analytics
import com.sourcepoint.cmplibrary.NativeMessageController
import com.sourcepoint.cmplibrary.SpClient
import com.sourcepoint.cmplibrary.core.nativemessage.MessageStructure
import com.sourcepoint.cmplibrary.creation.delegate.spConsentLibLazy
import com.sourcepoint.cmplibrary.data.network.model.optimized.GCMStatus
import com.sourcepoint.cmplibrary.data.network.util.CampaignsEnv
import com.sourcepoint.cmplibrary.exception.CampaignType
import com.sourcepoint.cmplibrary.model.ConsentAction
import com.sourcepoint.cmplibrary.model.MessageLanguage
import com.sourcepoint.cmplibrary.model.exposed.SPConsents
import com.sourcepoint.cmplibrary.util.clearAllData
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val spConsentLib by spConsentLibLazy {
        activity = this@MainActivity
        spClient = LocalClient()
        config {
            accountId = 22
            propertyName = "mobile.multicampaign.demo"
            propertyId = 16893
            messLanguage = MessageLanguage.ENGLISH
            campaignsEnv = CampaignsEnv.PUBLIC
            +(CampaignType.GDPR)
        }
    }

    override fun onResume() {
        super.onResume()
        spConsentLib.loadMessage()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics
        setContent {
            GCMExampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("CMP")
                    Buttons(
                        "Review Preferences" to { spConsentLib.loadPrivacyManager("488393", CampaignType.GDPR) },
                        "Clear Preferences" to { clearAllData(this) },
                    )
                }
            }
        }
    }

    internal inner class LocalClient : SpClient {

        override fun onNoIntentActivitiesFound(url: String) {
            Log.i(this::class.java.name, "onNoIntentActivitiesFound")
        }

        override fun onUIReady(view: View) {
            spConsentLib.showView(view)
            Log.i(this::class.java.name, "onUIReady")
        }

        override fun onAction(view: View, consentAction: ConsentAction): ConsentAction {
            Log.i(this::class.java.name, "ActionType: ${consentAction.actionType}")
            return consentAction
        }

        override fun onNativeMessageReady(message: MessageStructure, messageController: NativeMessageController) {
            Log.i(this::class.java.name, "onNativeMessageReady: $message")
        }

        override fun onUIFinished(view: View) {
            spConsentLib.removeView(view)
            Log.i(this::class.java.name, "onUIFinished")
        }

        override fun onError(error: Throwable) {
            Log.i(this::class.java.name, "onError: ${error.message}")
            error.printStackTrace()
        }

        override fun onSpFinished(sPConsents: SPConsents) {
            val gcmData = sPConsents.gdpr?.consent?.googleConsentMode
            val consentMap = mapOf(
                ConsentType.ANALYTICS_STORAGE to if(gcmData?.analyticsStorage == GCMStatus.GRANTED) ConsentStatus.GRANTED else ConsentStatus.DENIED,
                ConsentType.AD_STORAGE to if(gcmData?.adStorage == GCMStatus.GRANTED) ConsentStatus.GRANTED else ConsentStatus.DENIED,
                ConsentType.AD_USER_DATA to if(gcmData?.adUserData == GCMStatus.GRANTED) ConsentStatus.GRANTED else ConsentStatus.DENIED,
                ConsentType.AD_PERSONALIZATION to if(gcmData?.adPersonalization == GCMStatus.GRANTED) ConsentStatus.GRANTED else ConsentStatus.DENIED
            )
            firebaseAnalytics.setConsent(consentMap)

            Log.i(this::class.java.name, "onSpFinish: $sPConsents")
            Log.i(this::class.java.name, "==================== onSpFinish ==================")
        }

        override fun onConsentReady(consent: SPConsents) {
            Log.i(this::class.java.name, "onConsentReady: $consent")
        }

        override fun onMessageReady(message: JSONObject) {}
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun Buttons(vararg buttons: Pair<String, () -> Unit>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for ((text, onClick) in buttons) {
            Button(onClick = onClick) {
                Text(text = text)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GCMExampleTheme {
        Greeting("Android")
    }
}