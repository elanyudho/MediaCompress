package id.kecilin.mediacompress

import android.annotation.SuppressLint
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import id.kecilin.mediacompress.image.ImageCompressActivity
import id.kecilin.mediacompress.video.VideoCompressActivity
import id.kecilin.mediacompress.videocompress.databinding.ActivityMainBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val expiryDateString = "01-Jan-2023"

        val formatter: DateFormat = SimpleDateFormat("dd-MMM-yyyy")
        val expiryDate: Date = formatter.parse(expiryDateString) as Date

        if (System.currentTimeMillis() > expiryDate.time) {
            binding.clMain.visibility = View.GONE
            binding.clExpired.visibility = View.VISIBLE
        }

        binding.btnVideoCompress.setOnClickListener {
            startActivity(Intent(this, VideoCompressActivity::class.java))
        }

        binding.btnImageCompress.setOnClickListener {
            startActivity(Intent(this, ImageCompressActivity::class.java))
        }
    }
}