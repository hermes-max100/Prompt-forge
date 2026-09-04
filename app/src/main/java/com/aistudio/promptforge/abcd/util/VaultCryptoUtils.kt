package com.aistudio.promptforge.abcd.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object VaultCryptoUtils {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val ITERATION_COUNT = 10_000
    private const val KEY_LENGTH = 256

    // Default internal salt for obfuscated local store
    private val DEFAULT_SALT = "AutoForge_Local_Master_Salt_2026".toByteArray(Charsets.UTF_8)

    private fun deriveKey(passphrase: String, salt: ByteArray = DEFAULT_SALT): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    /**
     * Encrypts plaintext string using AES-GCM with a random IV.
     * Output format: Base64(IV + Ciphertext)
     */
    fun encrypt(plainText: String, secretKeyPassphrase: String = "autoforge-internal-vault-seed"): String {
        if (plainText.isEmpty()) return ""
        val random = SecureRandom()
        val iv = ByteArray(IV_LENGTH_BYTE)
        random.nextBytes(iv)

        val keySpec = deriveKey(secretKeyPassphrase)
        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)

        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts Base64(IV + Ciphertext) using AES-GCM.
     */
    fun decrypt(encryptedBase64: String, secretKeyPassphrase: String = "autoforge-internal-vault-seed"): String {
        if (encryptedBase64.isEmpty()) return ""
        val decoded = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        if (decoded.size < IV_LENGTH_BYTE) {
            throw IllegalArgumentException("Invalid encrypted payload size")
        }

        val iv = ByteArray(IV_LENGTH_BYTE)
        System.arraycopy(decoded, 0, iv, 0, IV_LENGTH_BYTE)

        val cipherTextSize = decoded.size - IV_LENGTH_BYTE
        val cipherText = ByteArray(cipherTextSize)
        System.arraycopy(decoded, IV_LENGTH_BYTE, cipherText, 0, cipherTextSize)

        val keySpec = deriveKey(secretKeyPassphrase)
        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

        val decryptedBytes = cipher.doFinal(cipherText)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
