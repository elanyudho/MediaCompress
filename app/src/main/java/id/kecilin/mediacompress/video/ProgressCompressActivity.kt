package id.kecilin.mediacompress.video

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import id.kecilin.mediacompress.videocompress.CompressionListener
import id.kecilin.mediacompress.videocompress.VideoCompressor
import id.kecilin.mediacompress.videocompress.VideoQuality
import id.kecilin.mediacompress.videocompress.config.Configuration
import id.kecilin.mediacompress.videocompress.config.StorageConfiguration
import id.kecilin.mediacompress.videocompress.databinding.ActivityProgressCompressBinding
import id.kecilin.mediacompress.videocompress.utils.getFileSize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class ProgressCompressActivity : AppCompatActivity() {

    lateinit var binding :ActivityProgressCompressBinding

    private var fileName :String? = ""
    private var sourceFile :Uri?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressCompressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCancel.setOnClickListener {
            VideoCompressor.cancel()
            onBackPressed()
        }


        sourceFile = intent.getParcelableExtra(INTENT_URIFILE)
        fileName = intent.getStringExtra(INTENT_FILENAME)


        processVideo(sourceFile)
    }

    private fun processVideo(uri: Uri?) {

        uri?.let { data ->

            /**
             * Set Path Based On Android Version
             * **/
            val path: String =
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    "${Environment.DIRECTORY_MOVIES}/Kecilin"
                }
                else {
                    "${Environment.getExternalStorageDirectory()}/Kecilin"
                }

            VideoCompressor.start(
                context = this,
                uris = listOf(data),
                isStreamable = true,
                storageConfiguration = StorageConfiguration(
                    fileName = fileName,
                    saveAt = path,
                    isExternal = true,
                ),
                configureWith = Configuration(
                    quality = VideoQuality.MEDIUM,
                    disableAudio = false,
                    isMinBitrateCheckEnabled = true,
                    keepOriginalResolution = true
                ),
                listener = object :CompressionListener {
                    override fun onStart(index: Int) {

                    }

                    override fun onSuccess(index: Int, size: Long, path: String?) {

                        //show path file
                        binding.tvTitle.text = "path :${path}, size compress video :${getFileSize(size)}"
                        Toast.makeText(this@ProgressCompressActivity, "sukses path${path}", Toast.LENGTH_SHORT).show()

                        //show 100% progress
                        binding.tvProgress.text = "${100f}"
                        binding.progressBar.progress = 100


                        //show preview video
                        binding.animProgress.visibility = View.INVISIBLE
                        binding.previewVideo.visibility = View.VISIBLE
                        Glide.with(this@ProgressCompressActivity).load(sourceFile).into(binding.previewVideo)
                    }

                    override fun onFailure(index: Int, failureMessage: String) {}

                    override fun onProgress(index: Int, percent: Float) {

                        if (percent <= 100 && percent.toInt() % 5 == 0)
                            runOnUiThread {

                                //show progress
                                binding.tvProgress.text = "${percent.toLong()}%"
                                binding.progressBar.progress = percent.toInt()
                            }
                    }

                    override fun onCancelled(index: Int) {}

                }
            )
        }

    }


    companion object {
        val INTENT_FILENAME = "file name"
        val INTENT_URIFILE = "uri file"
    }
}