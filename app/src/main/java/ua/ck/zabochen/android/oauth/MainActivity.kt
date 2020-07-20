package ua.ck.zabochen.android.oauth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*

// Google SignIn
// https://developers.google.com/identity/sign-in/android/start-integrating

class MainActivity : AppCompatActivity() {

    private val googleSignInRequestCode = 1
    private var googleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initGoogleSignIn()
    }

    private fun initGoogleSignIn() {

        val googleSignInOptions: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        this.googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        getLastGoogleSignInAccount()

        // SignIn
        btnGoogleSignIn.apply {
            setSize(SignInButton.SIZE_STANDARD)
            setOnClickListener {
                startGoogleSignInFlow()
            }
        }

        // SignOut
        btnGoogleSignOut.setOnClickListener {
            startSignOutFlow()
        }

        // Open Facebook Login Screen
        btnOpenFacebookLoginScreen.setOnClickListener {
            navigateToFacebookLoginScreen()
        }
    }

    private fun getLastGoogleSignInAccount() {
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            updateGoogleSignInUi(account)
        } else {
            Log.i("MainActivity", "getLastGoogleSignInAccount: NULL")
        }
    }

    private fun startGoogleSignInFlow() {
        this.googleSignInClient?.apply {
            val signInIntent = this.signInIntent
            startActivityForResult(signInIntent, googleSignInRequestCode)
        }
    }

    private fun startSignOutFlow() {
        this.googleSignInClient?.apply {
            signOut().addOnCompleteListener {
                Log.i("MainActivity", "startSignOutFlow: SIGN_OUT")
            }
        }
    }

    private fun updateGoogleSignInUi(account: GoogleSignInAccount) {
        Log.i("MainActivity", "updateGoogleSignInUi: ${account.displayName}")
        Log.i("MainActivity", "updateGoogleSignInUi: ${account.givenName}")
        Log.i("MainActivity", "updateGoogleSignInUi: ${account.familyName}")
        Log.i("MainActivity", "updateGoogleSignInUi: ${account.email}")
        Log.i("MainActivity", "updateGoogleSignInUi: ${account.id}")
        Log.i("MainActivity", "updateGoogleSignInUi: ${account.photoUrl}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == googleSignInRequestCode) {
            val googleSignInTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(googleSignInTask)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            account?.apply { updateGoogleSignInUi(this) }
        } catch (e: Exception) {
            Log.i("MainActivity", "handleSignInResult: Exception")
        }
    }

    private fun navigateToFacebookLoginScreen() {
        startActivity(Intent(this, FacebookAuthActivity::class.java))
        finish()
    }
}