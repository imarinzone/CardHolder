package com.example.cardwallet.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.util.EnumMap

object BarcodeGenerator {

    val supportedFormats = listOf(
        "QR_CODE",
        "CODE_128",
        "EAN_13",
        "AZTEC",
        "PDF_417",
        "CODE_39",
        "UPC_A"
    )

    fun getBarcodeFormat(formatName: String): BarcodeFormat {
        return try {
            when (formatName.uppercase()) {
                "QR_CODE", "QR" -> BarcodeFormat.QR_CODE
                "CODE_128", "CODE128" -> BarcodeFormat.CODE_128
                "EAN_13", "EAN13" -> BarcodeFormat.EAN_13
                "AZTEC" -> BarcodeFormat.AZTEC
                "PDF_417", "PDF417" -> BarcodeFormat.PDF_417
                "CODE_39", "CODE39" -> BarcodeFormat.CODE_39
                "UPC_A", "UPCA" -> BarcodeFormat.UPC_A
                else -> BarcodeFormat.QR_CODE
            }
        } catch (_: Exception) {
            BarcodeFormat.QR_CODE
        }
    }

    fun is2DFormat(formatName: String): Boolean {
        return when (formatName.uppercase()) {
            "QR_CODE", "QR", "AZTEC", "PDF_417", "PDF417" -> true
            else -> false
        }
    }

    fun generateImageBitmap(
        content: String,
        formatName: String = "QR_CODE",
        width: Int = 500,
        height: Int = 500
    ): ImageBitmap? {
        if (content.isBlank()) return null
        return try {
            val format = getBarcodeFormat(formatName)
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
            }

            val writer = MultiFormatWriter()
            val bitMatrix: BitMatrix = try {
                writer.encode(content, format, width, height, hints)
            } catch (_: Exception) {
                // If standard 1D encoder fails (e.g. invalid length or chars for EAN/UPC), fallback to Code 128 or QR
                try {
                    writer.encode(content, BarcodeFormat.CODE_128, width, height, hints)
                } catch (_: Exception) {
                    writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
                }
            }

            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)

            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
