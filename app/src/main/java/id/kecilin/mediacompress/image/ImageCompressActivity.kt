package id.kecilin.mediacompress.image

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import id.kecilin.mediacompress.FileUtilss
import id.kecilin.mediacompress.imagecompress.Compressor
import id.kecilin.mediacompress.imagecompress.constraint.quality
import id.kecilin.mediacompress.imagecompress.constraint.resolution
import id.kecilin.mediacompress.videocompress.databinding.ActivityImageCompressBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.Math.abs
import java.lang.Math.log10
import java.text.DecimalFormat
import kotlin.math.pow

class ImageCompressActivity : AppCompatActivity() {

    lateinit var binding: ActivityImageCompressBinding

    private var dataUriOri: Uri? = null
    private var dataUriCompress: Uri? = null

    private var sizeOri: String = ""
    private var sizeCompress: String = ""

    private var sizePerOri: Long = 0
    private var sizePerComp: Long = 0

    private val registerPickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                //mViewmodel.doSetEmptyLayout()
                try {
                    handleResult(it.data)
                    binding.tvCompressedSize.text = "Compressed Size: "
                } catch (e: Exception) {

                }
            }
        }

    /**
     * Set Path Based On Android Version
     * **/
    val path: String
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${Environment.DIRECTORY_PICTURES}/Kecilin"
            } else {
                "${Environment.getExternalStorageDirectory()}/Kecilin"
            }

    private fun handleResult(data: Intent?) {
        dataUriOri = data?.data
        val fileOri = FileUtilss.from(this, dataUriOri)


        binding.btnCompress.visibility = View.VISIBLE

        sizeOri = getReadableFileSize(fileOri.length() ?: 0L)

        binding.tvOriginalSize.text = "Original Size:\n${sizeOri}"
        sizePerOri = fileOri.length()


        Glide.with(this@ImageCompressActivity)
            .load(dataUriOri)
            .into(binding.ivOriginal)

    }

    private fun saveImage(fileOri: File, dataUriCompress: Uri?) {
        if (Build.VERSION.SDK_INT >= 30) {

            val folderName = path ?: Environment.DIRECTORY_PICTURES
            try {
                saveVideoInExternal(
                    this,
                    videoFileName = "${System.currentTimeMillis()}-${fileOri.name}",
                    videoFile = File(path),
                    folderName = folderName,
                    uri = dataUriCompress
                )

                Toast.makeText(this, "Saved in /Pictures", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {

            }

        } else {
            val directoryFolder = File(path)

            if (!directoryFolder.exists() && !directoryFolder.isDirectory) {
                directoryFolder.mkdirs()
            }

            val desFile = File(path, "${System.currentTimeMillis()}-${fileOri.name}")

            if (desFile.exists())
                desFile.delete()

            try {
                val inputStream = dataUriCompress?.let { contentResolver.openInputStream(it) }

                val inputData = getBytes(inputStream!!)
                desFile.createNewFile()
                val fos = FileOutputStream(desFile)
                fos.write(inputData)
                fos.close()

                Toast.makeText(this, "Saved in /Kecilin", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageCompressBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnPickImage.setOnClickListener {
            doTakePhotoFromGallery()
        }

        binding.btnCompress.setOnClickListener {

            val fileOri = FileUtilss.from(this, dataUriOri)

            GlobalScope.launch(Dispatchers.IO) {

                val compressFile = Compressor.compress(this@ImageCompressActivity, fileOri) {
                    resolution(1280, 720)
                    quality(80)
                }

                dataUriCompress = compressFile?.toUri()
                sizeCompress = getReadableFileSize(compressFile?.length() ?: 0L)

                //save
                withContext(Dispatchers.Main) {
                    saveImage(fileOri, dataUriCompress)

                    binding.tvCompressedSize.text = "Compressed Size:\n${sizeCompress}"
                    sizePerComp = compressFile.length()
                    binding.tvPercentage.text = getPercentage()

                    Glide.with(this@ImageCompressActivity)
                        .load(dataUriCompress)
                        .into(binding.ivCompress)
                }

            }


        }
    }

    private fun getPercentage(): String {
        return if (sizePerOri.toFloat() > 0) {
            val set =
                abs(((sizePerOri.toFloat() - sizePerComp.toFloat())) / sizePerOri.toFloat()) * 100
            "Percentage of Compression: " + String.format("%.0f", set) + "%"
        } else {
            "Percentage of Compression: 0%"
        }
    }

    private fun doTakePhotoFromGallery() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.type = "image/*"
        registerPickImage.launch(i)
    }


    private fun saveVideoInInternal(
        context: Context,
        videoFileName: String,
        videoFile: File
    ): File {
        context.openFileOutput(videoFileName, Context.MODE_PRIVATE)
            .use { outputStream ->
                FileInputStream(videoFile).use { inputStream ->
                    val buf = ByteArray(4096)
                    while (true) {
                        val sz = inputStream.read(buf)
                        if (sz <= 0) break
                        outputStream.write(buf, 0, sz)
                    }

                }
            }
        return File(context.filesDir, videoFileName)
    }


    private fun saveVideoInExternal(
        context: Context,
        videoFileName: String,
        folderName: String,
        videoFile: File,
        uri: Uri?
    ): File? {

        val parcelFileDescriptor = uri?.let { contentResolver.openFileDescriptor(it, "r") }
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor


        folderName
        val values = ContentValues().apply {

            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                videoFileName
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/*")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }


        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI


        contentResolver.insert(collection, values)?.also { uri ->
            contentResolver.openOutputStream(uri).use { outputStream ->

                val inputStream = FileInputStream(fileDescriptor)
                outputStream?.write(inputStream.readBytes())
            }
        }

        return null
    }


    @Suppress("DEPRECATION")
    private fun getMediaPath(context: Context, uri: Uri): String {

        val resolver = context.contentResolver
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = null
        try {
            cursor = resolver.query(uri, projection, null, null, null)
            return if (cursor != null) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                cursor.getString(columnIndex)

            } else ""

        } catch (e: Exception) {
            resolver.let {
                val filePath = (context.applicationInfo.dataDir + File.separator
                        + System.currentTimeMillis())
                val file = File(filePath)

                resolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        val buf = ByteArray(4096)
                        var len: Int
                        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(
                            buf,
                            0,
                            len
                        )
                    }
                }
                return file.absolutePath
            }
        } finally {
            cursor?.close()
        }
    }

    private fun getReadableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    @Throws(IOException::class)
    fun getBytes(inputStream: InputStream): ByteArray? {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }
}