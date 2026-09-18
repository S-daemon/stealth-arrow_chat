package com.example.arrows.crypto

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * End-to-End Encryption Manager using AES-256-GCM with PBKDF2 key derivation.
 * Ensures zero-knowledge encryption: only parties with the secret passphrase
 * can decrypt messages.
 */
object CryptoUtils {
    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH = 256
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12
    private const val SALT_LENGTH = 16

    /**
     * Derives an AES-256 key from a user passphrase and salt.
     */
    fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val secret = factory.generateSecret(spec)
        return SecretKeySpec(secret.encoded, "AES")
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     * Returns: Base64(salt[16] + iv[12] + ciphertext)
     */
    fun encrypt(plaintext: String, passphrase: String): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH).also { random.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { random.nextBytes(it) }

        val key = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Combine: [salt (16 bytes)] + [iv (12 bytes)] + [ciphertext + tag]
        val combined = ByteArray(salt.size + iv.size + ciphertext.size)
        System.arraycopy(salt, 0, combined, 0, salt.size)
        System.arraycopy(iv, 0, combined, salt.size, iv.size)
        System.arraycopy(ciphertext, 0, combined, salt.size + iv.size, ciphertext.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts an encrypted payload using the passphrase.
     * Returns the original plaintext or null if password/integrity check fails.
     */
    fun decrypt(encryptedBase64: String, passphrase: String): String? {
        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size < SALT_LENGTH + IV_LENGTH) return null

            val salt = ByteArray(SALT_LENGTH)
            val iv = ByteArray(IV_LENGTH)
            val ciphertextSize = combined.size - SALT_LENGTH - IV_LENGTH
            val ciphertext = ByteArray(ciphertextSize)

            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH)
            System.arraycopy(combined, SALT_LENGTH, iv, 0, IV_LENGTH)
            System.arraycopy(combined, SALT_LENGTH + IV_LENGTH, ciphertext, 0, ciphertextSize)

            val key = deriveKey(passphrase, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            val decryptedBytes = cipher.doFinal(ciphertext)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}

