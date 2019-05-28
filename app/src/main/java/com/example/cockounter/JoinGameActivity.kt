package com.example.cockounter

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
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

    fun scanQRCode() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        startActivityForResult(intentFor<QRCodeScannerActivity>(), CODE_SCAN_QR_CODE)
    }

    fun enterCode() {
        alert {
            customView {
                val code = editText {
                    hint = "Code"
                }
                yesButton {

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data == null) {
            return
        }
        when(requestCode) {
            CODE_SCAN_QR_CODE -> if(resultCode == Activity.RESULT_OK) {
                val message = data.getSerializableExtra(QRCodeScannerActivity.RESULT_SCANNED_MESSAGE) as String
                toast(message)
            }
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
            button("Enter manualy") {
                onClick {
                    owner.enterCode()
                }
            }
        }
    }

}
