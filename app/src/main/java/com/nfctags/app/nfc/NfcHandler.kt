package com.nfctags.app.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcHandler @Inject constructor() {

    companion object {
        private const val ENCODING = "UTF-8"
    }

    data class NfcResult(
        val tagId: String,
        val rawMessage: String?
    )

    fun leerTag(tag: Tag): NfcResult? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val message = ndef.ndefMessage
            val record = message?.records?.firstOrNull()
            val payload = record?.payload
            val text = if (payload != null) {
                // El primer byte es el status del encoding, el resto es el texto
                String(payload.copyOfRange(3, payload.size), StandardCharsets.UTF_8)
            } else null

            val tagId = text ?: generarIdUnico(tag)

            ndef.close()
            NfcResult(tagId = tagId, rawMessage = text)
        } catch (e: Exception) {
            NfcResult(tagId = generarIdUnico(tag), rawMessage = null)
        }
    }

    fun escribirIdEnTag(tag: Tag, tagId: String): Boolean {
        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                val record = NdefRecord.createMime(
                    "text/plain",
                    tagId.toByteArray(StandardCharsets.UTF_8)
                )
                ndef.writeNdefMessage(NdefMessage(arrayOf(record)))
                ndef.close()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun generarIdUnico(tag: Tag? = null): String {
        return if (tag != null) {
            val hexId = tag.id.joinToString("") { "%02x".format(it) }
            "NFC_${hexId}_${System.currentTimeMillis()}"
        } else {
            "NFC_${UUID.randomUUID().toString().take(8)}_${System.currentTimeMillis()}"
        }
    }
}
