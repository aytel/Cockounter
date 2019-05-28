package com.example.cockounter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.jetbrains.anko.toast

class QRCodeScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    companion object {
        const val RESULT_SCANNED_MESSAGE = "RESULT_SCANNED_MESSAGE"
    }

    private lateinit var scannerView: ZXingScannerView
    override fun handleResult(rawResult: Result?) {
        val result = Intent()
        result.putExtra(RESULT_SCANNED_MESSAGE, rawResult?.text.toString())
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerView = ZXingScannerView(this)
        setContentView(scannerView)
    }

    override fun onResume() {
        super.onResume()
        scannerView.setResultHandler(this)
        scannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        scannerView.stopCamera()
    }
}
