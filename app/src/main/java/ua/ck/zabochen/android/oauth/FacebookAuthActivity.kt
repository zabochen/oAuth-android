package ua.ck.zabochen.android.oauth

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_facebook_auth.*
import java.net.URL
import java.security.MessageDigest

class FacebookAuthActivity : AppCompatActivity() {

    private var callbackManager: CallbackManager? = null
    private val emailPermission = "email"
    private val profilePermission = "public_profile"

    private var isSignIn = false

    private var accessTokenTracker: AccessTokenTracker? = null
    private var profileTokenTracker: ProfileTracker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook_auth)
        checkFacebookSignIn()
        initFacebookSignInt()
    }

    private fun checkFacebookSignIn() {
        val accessToken = AccessToken.getCurrentAccessToken()
        Log.i("FacebookAuthActivity", "checkFacebookSignIn - token: $accessToken")
        this.isSignIn = accessToken != null && !accessToken.isExpired
        if (isSignIn) {
            getUserInfo(accessToken)
        }
    }

    private fun initFacebookSignInt() {

        this.callbackManager = CallbackManager.Factory.create()

        // Facebook Button
        btnFacebookSignIn.apply {
            setPermissions(emailPermission, profilePermission)
            // If you are using in a fragment, call loginButton.setFragment(this);

            registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    Log.i("FacebookAuthActivity", "Facebook login - onSuccess")
                }

                override fun onCancel() {
                    Log.i("FacebookAuthActivity", "Facebook login - onCancel")
                }

                override fun onError(error: FacebookException?) {
                    Log.i("FacebookAuthActivity", "Facebook login - onError")
                }
            })
        }

        this.accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(oldAccessToken: AccessToken?, currentAccessToken: AccessToken?) {
                Log.i("FacebookAuthActivity", "onCurrentAccessTokenChanged")
                currentAccessToken?.apply { getUserInfo(this) }
            }
        }

        this.profileTokenTracker = object : ProfileTracker() {
            override fun onCurrentProfileChanged(oldProfile: Profile?, currentProfile: Profile?) {
                Log.i("FacebookAuthActivity", "onCurrentProfileChanged")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("FacebookAuthActivity", "onActivityResult: ")
        callbackManager?.apply {
            onActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getUserInfo(accessToken: AccessToken) {
        val graphRequest = GraphRequest.newMeRequest(accessToken) { jsonObject, response ->
            try {
                val userPhotoUrl = URL(
                    "https://graph.facebook.com/"
                            + jsonObject.getString("id")
                            + "/picture?width=250&height=250"
                )
                val firstName = jsonObject.getString("first_name")
                val lastName = jsonObject.getString("last_name")
                val email = jsonObject.getString("email")
                val id = jsonObject.getString("id")

                Log.i("FacebookAuthActivity", "getUserInfo: $firstName, $lastName, $email, $id, $userPhotoUrl")
                Log.i("FacebookAuthActivity", "response: ${response.toString()}")
            } catch (e: Exception) {
            }
        }
        val graphParameters = Bundle()
        graphParameters.putString("fields", "first_name,last_name,email,id")
        graphRequest.parameters = graphParameters
        graphRequest.executeAsync()
    }

    // Examples:
    // https://stackoverflow.com/questions/52041805/how-to-use-packageinfo-get-signing-certificates-in-api-28
    @RequiresApi(Build.VERSION_CODES.P)
    private fun getKeyHash() {
        try {
            val packageInfo = packageManager.getPackageInfo(
                "ua.ck.zabochen.android.oauth",
                PackageManager.GET_SIGNING_CERTIFICATES
            )

            // signingCertificateHistory
            // apkContentsSigners
            packageInfo.signingInfo.apkContentsSigners.forEach { signature ->
                val messageDigest = MessageDigest.getInstance("SHA")
                messageDigest.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT)
                Log.i("FacebookAuthActivity", "getKeyHash: $keyHash")
            }
        } catch (e: Exception) {
        }
    }
}