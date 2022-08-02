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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import id.kecilin.mediacompress.FileUtilss
import id.kecilin.mediacompress.imagecompress.Compressor
import id.kecilin.mediacompress.videocompress.databinding.ActivityImageCompressBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.Math.log10
import java.text.DecimalFormat
import kotlin.math.pow

class ImageCompressActivity : AppCompatActivity() {

    lateinit var binding : ActivityImageCompressBinding

    private var dataUriOri : Uri? =null
    private var dataUriCompress : Uri? =null

    private var sizeOri :String = ""
    private var sizeCompress :String = ""

    private val registerPickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            //mViewmodel.doSetEmptyLayout()

            handleResult(it.data)
        }
    }

    /**
     * Set Path Based On Android Version
     * **/
    val path: String =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            "${Environment.DIRECTORY_PICTURES}/Kecilin"
        }
        else {
            "${Environment.getExternalStorageDirectory()}/Kecilin"
        }

    private fun handleResult(data: Intent?) {
        dataUriOri = data?.data
        val fileOri =FileUtilss.from(this,dataUriOri)

        GlobalScope.launch(Dispatchers.IO){

            val compressFile = Compressor.compress(this@ImageCompressActivity, fileOri)

            dataUriCompress =compressFile?.toUri()
            sizeCompress = getReadableFileSize(compressFile?.length() ?: 0L)
            sizeOri = getReadableFileSize(fileOri.length() ?: 0L)


            //save


            withContext(Dispatchers.Main){
                saveImage(fileOri,dataUriCompress)

                binding.tvCompressedSize.text = "Compressed Size : ${sizeCompress}"
                binding.tvOriginalSize.text = "Original Size : ${sizeOri}"

                Glide.with(this@ImageCompressActivity)
                    .load(dataUriOri)
                    .into(binding.ivOriginal)

                Glide.with(this@ImageCompressActivity)
                    .load(dataUriCompress)
                    .into(binding.ivCompress)
            }

        }

    }

    private fun saveImage(fileOri: File, dataUriCompress: Uri?) {
        if(Build.VERSION.SDK_INT >= 30){

            val folderName = path ?: Environment.DIRECTORY_PICTURES
            try{
                saveVideoInExternal(this, videoFileName ="${System.currentTimeMillis()}-${fileOri.name}", videoFile = File(path) , folderName = folderName)
            }catch (e:Exception){

            }

        }else{
            val directoryFolder = File(path)

            if (directoryFolder.exists() && directoryFolder.isDirectory){

            }else{
                directoryFolder.mkdir()
            }

            val desFile = File(path, "${System.currentTimeMillis()}-${fileOri.name}")

            if (desFile.exists())
                desFile.delete()

            try {
                val inputStream =dataUriCompress?.let { contentResolver.openInputStream(it) }

                val inputData = getBytes(inputStream!!)
                desFile.createNewFile()
                val fos = FileOutputStream(desFile)
                fos.write(inputData)
                fos.close()

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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveVideoInExternal(
        context: Context,
        videoFileName: String,
        folderName: String,
        videoFile: File
    ): File? {
        val values = ContentValues().apply {

            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                videoFileName
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/*")
            put(MediaStore.Images.Media.RELATIVE_PATH, folderName)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val collection =
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val fileUri = context.contentResolver.insert(collection, values)

        fileUri?.let {
            context.contentResolver.openFileDescriptor(fileUri, "rw")
                .use { descriptor ->
                    descriptor?.let {
                        FileOutputStream(descriptor.fileDescriptor).use { out ->
                            FileInputStream(videoFile).use { inputStream ->
                                val buf = ByteArray(4096)
                                while (true) {
                                    val sz = inputStream.read(buf)
                                    if (sz <= 0) break
                                    out.write(buf, 0, sz)
                                }
                            }
                        }
                    }
                }

            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            context.contentResolver.update(fileUri, values, null, null)

            return File(getMediaPath(context, fileUri))
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
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
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