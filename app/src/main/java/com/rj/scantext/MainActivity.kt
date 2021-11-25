package com.rj.scantext

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color.parseColor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    lateinit var btnChooseImg : Button
    lateinit var btnCopy : Button
    lateinit var tvResult : TextView
    lateinit var scrollView: ScrollView
    lateinit var mainLayout: ConstraintLayout


    var intentActivityResultLauncher : ActivityResultLauncher<Intent>?=null

    lateinit var inputImage : InputImage
    lateinit var textRecognizer : TextRecognizer

    private val STORAGE_PERMISSION_CODE = 111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tvResult)
        btnCopy = findViewById(R.id.btnCopy)
        btnChooseImg = findViewById(R.id.btnChooseImg)
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        scrollView = findViewById(R.id.scrollView2)
        mainLayout = findViewById(R.id.mainLayout)


        intentActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
//                will handle result
                val data = it.data
                val imageUri = data?.data

                convertImageToText(imageUri)
            }
        )

        btnChooseImg.setOnClickListener {

            val chooseIntent = Intent()
            chooseIntent.type = "image/*"
            chooseIntent.action = Intent.ACTION_GET_CONTENT

            intentActivityResultLauncher!!.launch(chooseIntent)

        }

        btnCopy.setOnClickListener {
            copyTextToClipboard()
        }
    }

    private fun convertImageToText(imageUri: Uri?) {

        try {
//            prepare the input image
            inputImage = InputImage.fromFilePath(applicationContext, imageUri)

//            get text from Image
            val result : Task<Text> = textRecognizer.process(inputImage)
                .addOnSuccessListener {
                    tvResult.text = it.text
                }.addOnFailureListener {
                    tvResult.text = "Error : ${it.message}"
                }


        }catch (e: Exception){

        }
        mainLayout.setBackgroundColor(resources.getColor(R.color.black))
        tvResult.setTextColor(resources.getColor(R.color.white))
    }

    override fun onResume() {
        super.onResume()
        checkForPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)

    }

    private fun checkForPermission(permission: String, requestCode:Int) {
        if(ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED){
//            Take Permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_DENIED){
                Toast.makeText(this@MainActivity, "Storage Permission Denied", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this@MainActivity, "Storage Permission Granted", Toast.LENGTH_LONG).show()
            }
        }
    }

//    Copy Texts
    private fun copyTextToClipboard() {
        val textToCopy = tvResult.text
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
    }
}