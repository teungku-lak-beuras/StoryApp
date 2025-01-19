package my.storyapp.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val fileNameFormat = "yyyyMMdd_HHmmss"
val timeStamp: String = SimpleDateFormat(fileNameFormat, Locale.US).format(Date())

fun provideFile(context: Context, imageUri: Uri): File {
    val filesDir = context.externalCacheDir
    val myFile = File.createTempFile(timeStamp, ".jpg", filesDir)
    val inputStream = context.contentResolver.openInputStream(imageUri) as InputStream
    val outputStream = FileOutputStream(myFile)
    val buffer = ByteArray(1024)
    var length: Int

    while (inputStream.read(buffer).also { length = it } > 0) {
        outputStream.write(buffer, 0, length)
    }

    outputStream.close()
    inputStream.close()

    return reduceFileImage(myFile)
}

fun reduceFileImage(file: File): File {
    val maxSize = 1000000
    val bitmap = BitmapFactory.decodeFile(file.path)
    var compressQuality = 100
    var streamLength: Int

    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > maxSize)

    bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    return file
}
