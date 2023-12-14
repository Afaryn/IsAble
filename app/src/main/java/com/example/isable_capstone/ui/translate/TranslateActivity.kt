package com.example.isable_capstone.ui.translate

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.isable_capstone.R
import com.example.isable_capstone.databinding.ActivityCameraBinding
import com.example.isable_capstone.databinding.ActivityTranslateBinding
import com.example.isable_capstone.ml.SignLanguageModelV1
import org.w3c.dom.Text

class TranslateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTranslateBinding
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var tvOutput:TextView
    private val GALLERY_REQUEST_CODE = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTranslateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        imageView = binding.ivPhoto
        button = binding.btnTakePhoto
        tvOutput  = binding.tvOutput
        val buttonLoad = binding.btnLoadImage

        button.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)
            {
                takePicturePreview.launch(null)
            }
            else{
                requestPermission.launch(android.Manifest.permission.CAMERA)
            }
        }
        buttonLoad.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                val mimeTypes = arrayOf("image/png","image/png","image/jpg")
                intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                onresult.launch(intent)
            }
            else{
                requestPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

    }

    //request camera permission
    private val requestPermission = registerForActivityResult( ActivityResultContracts.RequestPermission()){ granted->
        if(granted){
            takePicturePreview.launch(null)
        }else{
            Toast.makeText(this,"Permission Denied!! Try Again",Toast.LENGTH_SHORT).show()
        }
    }

    //launch camera and take picture
    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){bitmap->
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap)
            outputGenerator(bitmap)
        }
    }

    //to get image from gallery
    private val onresult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        Log.i("TAG","this is the result: ${result.data} ${result.resultCode}")
//        onResultRecieved(GALLERY_REQUEST_CODE,result)
    }

    private fun onResultRecieved(requestCode:Int, result: Instrumentation.ActivityResult?){
        when(requestCode){
            GALLERY_REQUEST_CODE->{
//                if(result?.resultCode== Activity.RESULT_OK){
//                    result.data?.data?.let{uri->
//                        Log.i("TAG","OnResultRecieved:$uri")
//                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
//                        imageView.setImageBitmap(bitmap)
//                        outputGenerator(bitmap)
//                    }
//                }else{
//                    Log.e("TAG","OnActivityResult:error in selecting image")
//                }
            }
        }
    }

    private fun outputGenerator(bitmap: Bitmap){
        //declearing tensorflow lite model variable

        val model = SignLanguageModelV1.newInstance(this)

// Creates inputs for reference.
//        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 244, 244, 1), DataType.FLOAT32)
//        inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
//        val outputs = model.process(inputFeature0)
//        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//
//// Releases model resources if no longer used.
//        model.close()
    }
}