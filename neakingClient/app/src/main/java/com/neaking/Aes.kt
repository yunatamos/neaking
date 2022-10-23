package com.neaking

import kotlin.Throws
import com.neaking.Aes
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object Aes {
    @Throws(Exception::class)
    fun encryptLarge(seed: ByteArray, `in`: File, out: File?) {
        val skeySpec = SecretKeySpec(getRawKey(seed), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
        val inputStream = FileInputStream(`in`)
        val fileOutputStream = FileOutputStream(out)
        var read: Int
        val buffer = ByteArray(4096)
        val cis = CipherInputStream(inputStream, cipher)
        while (cis.read(buffer).also { read = it } != -1) {
            fileOutputStream.write(buffer, 0, read)
        }
        fileOutputStream.close()
        cis.close()
        `in`.delete()
    }

    @Throws(Exception::class)
    fun decryptLarge(seed: ByteArray, `in`: File, out: File?) {
        val skeySpec = SecretKeySpec(getRawKey(seed), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, skeySpec)
        val inputStream = FileInputStream(`in`)
        val fileOutputStream = FileOutputStream(out)
        var read: Int
        val buffer = ByteArray(4096)
        val cis = CipherInputStream(inputStream, cipher)
        while (cis.read(buffer).also { read = it } != -1) {
            fileOutputStream.write(buffer, 0, read)
        }
        fileOutputStream.close()
        cis.close()
        `in`.delete()
    }

    @Throws(Exception::class)
    fun encrypt(seed: ByteArray, cleartext: ByteArray): ByteArray {
        val rawKey = getRawKey(seed)
        return encryptAes(rawKey, cleartext)
    }

    @Throws(Exception::class)
    fun decrypt(seed: ByteArray, enc: ByteArray): ByteArray {
        val rawKey = getRawKey(seed)
        return decryptAes(rawKey, enc)
    }

    @Throws(Exception::class)
    private fun getRawKey(seed: ByteArray): ByteArray {
        val sKey: SecretKey = SecretKeySpec(seed, "AES")
        return sKey.encoded
    }

    @Throws(Exception::class)
    private fun encryptAes(raw: ByteArray, clear: ByteArray): ByteArray {
        val skeySpec = SecretKeySpec(raw, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
        return cipher.doFinal(clear)
    }

    @Throws(Exception::class)
    private fun decryptAes(raw: ByteArray, encrypted: ByteArray): ByteArray {
        val skeySpec = SecretKeySpec(raw, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, skeySpec)
        return cipher.doFinal(encrypted)
    }
}