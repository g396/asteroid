package sns.asteroid.model.user

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Size
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.serialization.json.Json
import sns.asteroid.CustomApplication
import sns.asteroid.R
import sns.asteroid.api.Media
import sns.asteroid.api.entities.MediaAttachment
import sns.asteroid.db.entities.Credential
import sns.asteroid.model.settings.SettingsValues
import java.io.ByteArrayOutputStream

class MediaModel(val credential: Credential) {
    data class Result(
        val isSuccess: Boolean,
        val mediaAttachment: MediaAttachment?,
        val message: String
    )

    data class MediaFile(
        val type: MediaType,
        val uri: Uri?,
        val thumbnail: Bitmap? = null,
        val description: String = "",
        val mediaAttachment: MediaAttachment? = null,
    )

    enum class MediaType {
        IMAGE,
        VIDEO,
        AUDIO,
    }

    companion object {
        fun getMediaType(name: String): MediaType {
            return when(name) {
                "image" -> MediaType.IMAGE
                "gifv" -> MediaType.VIDEO
                "video" -> MediaType.VIDEO
                "audio" -> MediaType.AUDIO
                else -> MediaType.IMAGE
            }
        }
        fun getThumbnail(media: MediaFile): MediaFile {
            if(media.uri == null) return media

            val context = CustomApplication.getApplicationContext()

            if(media.type == MediaType.AUDIO) {
                val bitmap = ResourcesCompat.getDrawable(context.resources, R.drawable.audiofile, null)?.toBitmap()
                return media.copy(thumbnail = bitmap)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val bitmap = context.contentResolver.loadThumbnail(media.uri, Size(256,256), null)
                return media.copy(thumbnail = bitmap)
            }

            // TODO: Android Q未満　未確認
            val contentResolver = context.contentResolver
            val cursor =
                contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    MediaStore.Images.ImageColumns.DATA +  " = ?",
                    arrayOf(media.uri.toString()),
                    null
                ) ?: return media

            if (!cursor.moveToFirst()) {
                cursor.close()
                return media
            }

            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)
            val id = cursor.getLong(index)

            val bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, MediaStore.Images.Thumbnails.MICRO_KIND, null)
            return media.copy(thumbnail = bitmap)
        }

        fun importFile(uri: Uri, autoResize: Int? = null): ByteArray? {
            val context = CustomApplication.getApplicationContext()
            return context.contentResolver.openInputStream(uri)?.let { stream ->
                val byteArray = stream.readBytes().also { stream.close() }

                if(autoResize != null) {
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    val matrix = Matrix().rotate(uri, context).scale(bitmap.width, bitmap.height, autoResize)
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                    ByteArrayOutputStream().also {
                        resizeBitmap(bitmap, matrix).compress(Bitmap.CompressFormat.JPEG, 90, it)
                    }.toByteArray()
                }
                else byteArray
            }
        }

        fun download(url: String): Boolean {

            val response = Media.download(url)
                ?: return false

            if(!response.isSuccessful)
                return false

            val byteArray = response.body!!.bytes()

            val name = "${url.hashCode()}_${response.hashCode()}"
            val mimeType = response.headers["content-type"] ?: return false

            val storedValue = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            }

            return try {
                val context = CustomApplication.getApplicationContext()
                context.contentResolver.let {
                    val uri = it.insert(MediaStore.Files.getContentUri("external"), storedValue)
                        ?: return false
                    it.openOutputStream(uri)?.apply {
                        write(byteArray)
                        close()
                    }
                }
                true
            } catch (e: Exception) {
                false
            }
        }

        private fun resizeBitmap(bitmap: Bitmap, matrix: Matrix): Bitmap {
            return Bitmap.createBitmap(bitmap, 0,0,bitmap.width, bitmap.height, matrix, true)
        }
        private fun Matrix.rotate(uri: Uri, context: Context): Matrix {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")!!
            val exifInterface = ExifInterface(parcelFileDescriptor.fileDescriptor)
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            parcelFileDescriptor.close()

            return this.apply {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(-90f)
                }
            }
        }
        private fun Matrix.scale(width: Int, height: Int, maxSize: Int): Matrix {
            if ((width <= maxSize) and (height <= maxSize)) return this

            val scale =
                if (width >= height) maxSize.toFloat() / width.toFloat()
                else maxSize.toFloat() / height.toFloat()

            return this.apply { postScale(scale, scale) }
        }
    }

    fun postMedia(uri: Uri, description: String?, resize: Boolean): Result {
        val context = CustomApplication.getApplicationContext()

        val file = readAndResize(uri, context, resize)
            ?: return Result(isSuccess = false, mediaAttachment = null, message = getString(R.string.failed_to_load_media))
        val response = Media(credential).postMedia(file.first, file.second, file.third, description)
            ?: return Result(isSuccess = false, mediaAttachment = null, message = getString(R.string.failed))

        if(!response.isSuccessful)
            return Result(isSuccess = false, mediaAttachment = null, message = response.body!!.string())
                .also { response.close() }

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val mediaAttachment = json.decodeFromString(MediaAttachment.serializer(), response.body!!.string())

        return Result(isSuccess = true, mediaAttachment = mediaAttachment, message = getString(R.string.uploaded))
            .also { response.close() }
    }

    private fun readAndResize(uri: Uri, context: Context, resize: Boolean): Triple<ByteArray, String, String>? {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return null

        val binary = inputStream.readBytes().also {
            inputStream.close()
        }
        val fileName = context.contentResolver.query(
            uri, null, null, null, null
        )?.let { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(index).also { cursor.close() }
        } ?: return null

        val mimeType = context.contentResolver.getType(uri)
            ?: return null

        if(!mimeType.startsWith("image/"))
            return Triple(binary, fileName, mimeType)
        if (!resize)
            return Triple(binary, fileName, mimeType)

        val compressed = try {
            val decoded = BitmapFactory.decodeByteArray(binary, 0, binary.size)
            val maxSize = SettingsValues.getInstance().imageSize
            val matrix = Matrix().rotate(uri, context).scale(decoded.width, decoded.height, maxSize)
            ByteArrayOutputStream().also {
                resizeBitmap(decoded, matrix).compress(Bitmap.CompressFormat.JPEG, 90, it)
            }.toByteArray()
        } catch (e: Exception) {
            return null
        }
        return Triple(compressed,  fileName, "image/jpeg")
    }

    private fun getString(resId: Int): String {
        return CustomApplication.getApplicationContext().getString(resId)
    }
}