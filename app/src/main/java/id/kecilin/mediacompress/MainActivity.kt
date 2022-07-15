package id.kecilin.mediacompress

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.kecilin.mediacompress.video.VideoCompressActivity
import id.kecilin.mediacompress.videocompress.R
import id.kecilin.mediacompress.videocompress.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVideoCompress.setOnClickListener {
            startActivity(Intent(this,VideoCompressActivity::class.java))
        }

        binding.btnImageCompress.setOnClickListener {

        }
    }
}