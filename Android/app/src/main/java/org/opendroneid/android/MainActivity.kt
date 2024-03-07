package org.opendroneid.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.*
import java.util.concurrent.CompletableFuture

public class MainActivity : AppCompatActivity() {

    private lateinit var web3Auth: Web3Auth
    private val gson = Gson()
    private lateinit var contentTextView: TextView
    private lateinit var signInButton: Button
    private lateinit var signOutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)  // Specify superclass method
        setContentView(R.layout.activity_main)

        // Initialize Web3Auth with the necessary options
        web3Auth = Web3Auth(
            Web3AuthOptions(
                context = this,
                clientId = getString(R.string.web3auth_project_id), // Replace with your actual project ID (don't include quotes)
                network = Network.MAINNET, // Choose the network you want to use (MAINNET, TESTNET, CYAN, AQUA)
                redirectUrl = Uri.parse("org.opendroneid.android.app://auth") // Define your app's redirect URL
            )
        )

        // Handle user signing in when the app is not running
        web3Auth.setResultUrl(intent?.data)

        // Set up click listeners for buttons
        signInButton.setOnClickListener { signIn() }
        signOutButton.setOnClickListener { signOut() }

        // Initially, hide sign out button as user isn't logged in
        signOutButton.visibility = View.GONE
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // Handle user signing in when app is active
        web3Auth.setResultUrl(intent?.data)
    }

    private fun signIn() {
        val selectedLoginProvider = Provider.GOOGLE   // Can be GOOGLE, FACEBOOK, TWITCH etc.
        val loginCompletableFuture: CompletableFuture<Web3AuthResponse> = web3Auth.login(LoginParams(selectedLoginProvider))

        loginCompletableFuture.whenComplete { loginResponse, error ->
            if (error == null) {
                // Login successful, update UI and credentials
                println(loginResponse)
                contentTextView.text = "Logged in with: " + gson.toJson(loginResponse.userInfo)
                signInButton.visibility = View.GONE
                signOutButton.visibility = View.VISIBLE

                // (Optional) Retrieve user private key for further actions (refer to Web3Auth documentation)
                // String privateKey = web3Auth.getPrivkey();
            } else {
                // Login failed, handle error
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong" )
                contentTextView.text = "Login failed!"
            }
        }
    }

    private fun signOut() {
        val logoutCompletableFuture =  web3Auth.logout()
        logoutCompletableFuture.whenComplete { _, error ->
            if (error == null) {
                // Logout successful, update UI
                contentTextView.text = "Logged Out"
                signInButton.visibility = View.VISIBLE
                signOutButton.visibility = View.GONE
            } else {
                // Logout failed, handle error
                Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong" )
                contentTextView.text = "Logout failed!"
            }
        }
    }

    private fun reRender() {
        val contentTextView = findViewById<TextView>(R.id.contentTextView)
        val signInButton = findViewById<Button>(R.id.signInButton)
        val signOutButton = findViewById<Button>(R.id.signOutButton)

        var key: String? = null
        var userInfo: UserInfo? = null
        try {
            key = web3Auth.getPrivkey()
            userInfo = web3Auth.getUserInfo()
        } catch (ex: Exception) {
            print(ex)
        }
        println(userInfo)
        if (key is String && key.isNotEmpty()) {
            contentTextView.text = gson.toJson(userInfo) + "\n Private Key: " + key
            contentTextView.visibility = View.VISIBLE
            signInButton.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
            //   getAddressButton.visibility = View.VISIBLE
            //   getBalanceButton.visibility = View.VISIBLE
            //    getMessageButton.visibility = View.VISIBLE
            //    getTransactionButton.visibility = View.VISIBLE
            //   getEnableMFAButton.visibility = View.VISIBLE
            //  getLaunchWalletServicesButton.visibility = View.VISIBLE
        } else {
            contentTextView.text = getString(R.string.not_logged_in)
            contentTextView.visibility = View.GONE
            signInButton.visibility = View.VISIBLE
            signOutButton.visibility = View.GONE
            //   getAddressButton.visibility = View.GONE
            //  getBalanceButton.visibility = View.GONE
            //  getMessageButton.visibility = View.GONE
            //   getTransactionButton.visibility = View.GONE
            //   getEnableMFAButton.visibility = View.GONE
            //  getLaunchWalletServicesButton.visibility = View.GONE
        }

    }
}
