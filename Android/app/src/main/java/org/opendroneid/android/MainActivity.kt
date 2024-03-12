package org.opendroneid.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.LoginParams
import com.web3auth.core.types.Network
import com.web3auth.core.types.Provider
import com.web3auth.core.types.Web3AuthOptions
import com.web3auth.core.types.Web3AuthResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sol4k.Connection
import org.sol4k.Keypair
import org.sol4k.PublicKey
import org.sol4k.RpcUrl
import org.sol4k.Transaction
import org.sol4k.instruction.TransferInstruction
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture

private val Any.publicKey: PublicKey
    get() {
        TODO("Not yet implemented")
    }

class MainActivity : AppCompatActivity() {

    private lateinit var connection: Connection
    private lateinit var web3Auth: Web3Auth

    @OptIn(ExperimentalStdlibApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize connection and Web3Auth
        connection = Connection(RpcUrl.DEVNET)
        web3Auth = Web3Auth(
            Web3AuthOptions(
                clientId = "BAt3mbhdYrmD311fwjY1vY4XLAK_uRZIiUZRVT2ipE2_ajzlkogL0gwFflgiVgUpPIMU3VxszsZM-ZUErxZvmyY", // Replace with your actual Client ID
                context = this,
                network = Network.TESTNET, // Choose the network you want to use
                redirectUrl = Uri.parse("org.opendroneid.android://auth") // Define your app's redirect URL
            )
        )

        // Handle user signing in when app is not alive (moved to onCreate)
        handleSignInResult(intent)

        // ... rest of your onCreate logic
    }

    // Handle user signing in when app is not alive (placed back)
    private fun handleSignInResult(intent: Intent?) {
        if (intent?.data != null) {
            web3Auth.setResultUrl(intent.data)
        }
    }

    // Handle user signing in when app is active
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleSignInResult(intent)
    }

    private fun signIn() {
        val selectedLoginProvider = Provider.GOOGLE // Choose your preferred provider
        val loginCompletableFuture: CompletableFuture<Web3AuthResponse> =
            web3Auth.login(LoginParams(selectedLoginProvider))

        loginCompletableFuture.whenComplete { loginResponse, error ->
            if (error == null) {
                // User is logged in, proceed with authenticated actions
                println(loginResponse)

                // Use loginResponse to get user info and perform other actions
                val userInfo = loginResponse.userInfo
                // ...
            } else {
                // Handle login error
                println(error)
            }
        }
    }

    private fun signOut() {
        val logoutCompletableFuture = web3Auth.logout()
        logoutCompletableFuture.whenComplete { _, error ->
            if (error == null) {
                // User is logged out, update UI and data accordingly
            } else {
                // Handle logout error
                println(error)
            }
        }
    }

    // Assuming a function to retrieve user balance exists in your library
    private fun getBalance(userAccount: String): String {
        try {
            val publicKey = Keypair.fromBase58(userAccount)
            val balanceResponse = connection.getBalance(publicKey as PublicKey).toBigDecimal()
            return balanceResponse.divide(BigDecimal.TEN.pow(9)).toString()
        } catch (e: Exception) {
            // Handle error
            return "Error fetching balance"
        }
    }

    // Assuming a function to perform a Solana transaction exists in your library
    private suspend fun sendTransaction(sender: Keypair, recipient: String, amount: Long): String {
        try {
            val transaction = prepareSignedTransaction(sender, recipient, amount)
            connection.sendTransaction(transaction = transaction)
            return "Transaction successful!"
        } catch (e: Exception) {
            // Handle error
            return "Transaction failed: ${e.message}"
        }
    }

    private suspend fun prepareSignedTransaction(
        sender: Keypair,
        recipient: String,
        amount: Long
    ) {
        suspend fun prepareSignedTransaction(
            sender: Keypair,
            recipient: String,
            amount: Long
        ): Transaction {
            // Assuming `getLatestBlockhash` is implemented in your Solana library
            val blockHash = connection.getLatestBlockhash()

            // Replace with the actual transaction creation logic from your library
            val instruction = TransferInstruction(
                sender.publicKey,
                Keypair.fromBase58(recipient).publicKey,
                lamports = amount
            )
            val transaction = Transaction(blockHash, instruction, feePayer = sender.publicKey)
            transaction.sign(sender)
            return transaction
        }
    }
}

private fun Any.fromBase58(recipient: String): Any {
    TODO("Not yet implemented")
}

private fun Connection.sendTransaction(transaction: Unit) {
    TODO("Not yet implemented")
}
