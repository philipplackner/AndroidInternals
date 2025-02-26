package com.plcoding.androidinternals

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object Crypto {

    private const val KEY_ALIAS = "MyCryptoKey"
    private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

    private val cipher = Cipher.getInstance(TRANSFORMATION)
    private val keyStore = KeyStore
        .getInstance("AndroidKeyStore")
        .apply { load(null) }

    private val key: SecretKey
        get() {
            val existingKey = keyStore
                .getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            return existingKey?.secretKey ?: createKey()
        }

    private fun createKey(): SecretKey {
        return KeyGenerator
            .getInstance(ALGORITHM)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_DECRYPT or
                        KeyProperties.PURPOSE_ENCRYPT
                    )
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(PADDING)
                        .setRandomizedEncryptionRequired(true)
                        .apply {
                            if(Build.VERSION.SDK_INT >= 28) {
                                setIsStrongBoxBacked(true)
                            }
                        }
                        .build()
                )
            }
            .generateKey()
    }

    fun encrypt(bytes: ByteArray): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(bytes)
        return iv + encrypted
    }

    fun decrypt(bytes: ByteArray): ByteArray {
        val iv = bytes.copyOfRange(0, cipher.blockSize)
        val encrypted = bytes.copyOfRange(cipher.blockSize, bytes.size)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return cipher.doFinal(encrypted)
    }
}