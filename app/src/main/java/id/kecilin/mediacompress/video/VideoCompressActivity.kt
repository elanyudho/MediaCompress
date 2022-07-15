package id.kecilin.mediacompress.video

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import id.kecilin.mediacompress.videocompress.R
import id.kecilin.mediacompress.videocompress.databinding.ActivityVideoCompressBinding

class VideoCompressActivity : AppCompatActivity() {

    lateinit var binding : ActivityVideoCompressBinding

    private var dataUri : Uri?= null

    private val registerPickVideo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            //mViewmodel.doSetEmptyLayout()
            handleResult(it.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCompressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setReadStoragePermission()
        setCameraPermission()

        binding.btnCompress.visibility =View.INVISIBLE

        binding.btnPickVideo.setOnClickListener {
            doTakeVideoFromGallery()
        }


        binding.btnRecordVideo.setOnClickListener {
            doTakeVideoIntent()
        }

        binding.btnCompress.setOnClickListener {
            val intent = Intent(this,ProgressCompressActivity::class.java)
            intent.putExtra(ProgressCompressActivity.INTENT_FILENAME,"FileTest-${System.currentTimeMillis()}")
            intent.putExtra(ProgressCompressActivity.INTENT_URIFILE,dataUri)
            startActivity(intent)
        }

    }

    private fun handleResult(data: Intent?) {
        if (data != null && data.data != null) {
            dataUri = data.data

            //after pick video
            binding.btnCompress.visibility =View.VISIBLE
            Glide.with(this).load(data.data).into(binding.ivPreviewVideo)

        }
    }
    private fun doTakeVideoFromGallery() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.type = "video/*"
        registerPickVideo.launch(i)
    }

    private fun doTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(this.packageManager)?.also {
                registerPickVideo.launch(takeVideoIntent)
                //startActivityForResult(takeVideoIntent,4)
            }
        }
    }

    private fun setReadStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        }
    }

    private fun setCameraPermission(){
        if(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED

        ) {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
            )){
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    2
                )
            }
        }
    }
}