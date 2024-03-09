package org.opendroneid.android

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.Network
import com.web3auth.core.types.Web3AuthOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sol4k.Connection
import org.sol4k.Keypair
import org.sol4k.RpcUrl
import org.sol4k.Transaction
import org.sol4k.instruction.TransferInstruction
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalStdlibApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val connection = Connection(RpcUrl.DEVNET)

        // Initialize Web3Auth SDK
        val web3Auth: Web3Auth = Web3Auth(
            Web3AuthOptions(
                clientId = "BAt3mbhdYrmD311fwjY1vY4XLAK_uRZIiUZRVT2ipE2_ajzlkogL0gwFflgiVgUpPIMU3VxszsZM-ZUErxZvmyY",  // Replace with your actual Client ID
                context = this,
                network = Network.TESTNET, // Choose the network you want to use (MAINNET, TESTNET, CYAN, AQUA)
                redirectUrl = Uri.parse("org.opendroneid.android://auth") // Define your app's redirect URL
            )
        )

        // Check user authentication status
        val isUserAuthenticated = web3Auth.getPrivkey().isNotEmpty()

        // Handle user authentication logic here
        if (isUserAuthenticated) {
            try {
                val userInfo = web3Auth.getUserInfo()!!
                // Use user info
            } catch (e: Exception) {
                // Handle error
            }

            val ed25519PrivateKey = web3Auth.getEd25519PrivKey()
            val solanaKeyPair = Keypair.fromSecretKey(ed25519PrivateKey.hexToByteArray())

            val userAccount = solanaKeyPair.publicKey.toBase58()

            try {
                val publicKey = solanaKeyPair.publicKey
                val balanceResponse = connection.getBalance(publicKey).toBigDecimal()
                val userBalance = balanceResponse.divide(BigDecimal.TEN.pow(9)).toString()
                // Display user balance: $userBalance
            } catch (e: Exception) {
                // Handle error
            }

            // Prepare and send a signed transaction
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val transaction = prepareSignedTransaction(solanaKeyPair)
                    connection.sendTransaction(transaction = transaction)
                    // Transaction successful!
                } catch (e: Exception) {
                    // Handle error
                }
            }
        } else {
            // User is not authenticated, handle login flow
        }
    }

    private suspend fun prepareSignedTransaction(sender: Keypair): Transaction =
        withContext(Dispatchers.IO) {
            var connection = null
            val blockHash = connection.getLatestBlockhash()
            val instruction =
                TransferInstruction(sender.publicKey, sender.publicKey, lamports = 10000000)
            val transaction = Transaction(blockHash, instruction, feePayer = sender.publicKey)
            transaction.sign(sender)
            transaction
        }
}

private fun Nothing?.getLatestBlockhash(): String {
    TODO("Not yet implemented")
}




