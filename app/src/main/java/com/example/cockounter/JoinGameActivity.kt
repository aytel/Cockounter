package com.example.cockounter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class JoinGameActivity : AppCompatActivity() {
    companion object {
        private const val CODE_SCAN_QR_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JoinGameUI().setContentView(this)
    }

    private fun scanQRCode() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        startActivityForResult(intentFor<QRCodeScannerActivity>(), CODE_SCAN_QR_CODE)
    }

    private fun enterCode() {
        alert {
            customView {
                val code = editText {
                    hint = "Code"
                }
                yesButton {
                    askName(code.text.toString())
                }
            }
        }
    }

    private fun askName(uuid: String) {
        alert {
            customView {
                val name = editText {
                    hint = "Your in-game name"
                }
                yesButton {
                    joinGame(name.text.toString(), uuid)
                }
            }
        }
    }

    private fun joinGame(name: String, uuid: String) {
        startActivity(
            intentFor<MultiPlayerGameActivity>(
                MultiPlayerGameActivity.MODE to MultiPlayerGameActivity.MODE_JOIN_GAME,
                MultiPlayerGameActivity.ARG_UUID to uuid
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_SCAN_QR_CODE -> if (resultCode == Activity.RESULT_OK) {
                val message = data.getSerializableExtra(QRCodeScannerActivity.RESULT_SCANNED_MESSAGE) as String
                toast(message)
                joinGame("df", message)
            }
        }
    }

    private class JoinGameUI : AnkoComponent<JoinGameActivity> {
        override fun createView(ui: AnkoContext<JoinGameActivity>): View = with(ui) {
            verticalLayout {
                button("Scan QR code") {
                    onClick {
                        owner.scanQRCode()
                    }
                }
                button("Enter manually") {
                    onClick {
                        owner.enterCode()
                    }
                }
            }
        }
    }
}

