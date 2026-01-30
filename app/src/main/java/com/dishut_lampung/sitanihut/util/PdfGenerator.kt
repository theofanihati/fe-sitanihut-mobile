package com.dishut_lampung.sitanihut.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.dishut_lampung.sitanihut.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

interface PdfService {
    suspend fun <T> generatePdf(
        fileName: String,
        reportTitle: String,
        headers: List<String>,
        data: List<T>,
        rowMapper: (T) -> List<String>
    ): Resource<String>

    suspend fun generatePlaceholderPdf(
        fileName: String,
        reportTitle: String
    ): Resource<String>
}

class PdfServiceImpl @Inject constructor(
    private val context: Context
) : PdfService {

    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 40f

    override suspend fun <T> generatePdf(
        fileName: String,
        reportTitle: String,
        headers: List<String>,
        data: List<T>,
        rowMapper: (T) -> List<String>
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            var pageNumber = 1
            val (firstPage, startY) = startNewPage(pdfDocument, pageNumber, paint)
            var page = firstPage
            var canvas = page.canvas
            var yPosition = startY

            // ===== JUDUL =====
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("REKAPITULASI DATA", (pageWidth / 2).toFloat(), yPosition, paint)

            yPosition += 20f
            canvas.drawText(reportTitle.uppercase(), (pageWidth / 2).toFloat(), yPosition, paint)

            yPosition += 15f
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            val dateStr =
                SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
            canvas.drawText("Dicetak pada: $dateStr", (pageWidth / 2).toFloat(), yPosition, paint)

            yPosition += 30f

            val colWidth = (pageWidth - (2 * margin)) / headers.size

            // ===== HEADER TABEL =====
            drawTableHeader(canvas, paint, headers, yPosition, colWidth)
            yPosition += 15f

            // ===== DATA =====
            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f

            for (item in data) {
                if (yPosition > pageHeight - 80) {
                    drawFooter(canvas, paint)
                    pdfDocument.finishPage(page)

                    pageNumber++
                    val (newPage, newY) =
                        startNewPage(pdfDocument, pageNumber, paint)
                    page = newPage
                    canvas = page.canvas
                    yPosition = newY

                    drawTableHeader(canvas, paint, headers, yPosition, colWidth)
                    yPosition += 15f
                }

                val rowData = rowMapper(item)
                rowData.forEachIndexed { index, text ->
                    val xPos = margin + (index * colWidth) + 5f
                    val safeText =
                        if (text.length > 25) text.take(23) + ".." else text
                    canvas.drawText(safeText, xPos, yPosition, paint)
                }

                yPosition += 15f
            }

            // ===== FOOTER TERAKHIR =====
            drawFooter(canvas, paint)
            pdfDocument.finishPage(page)

            // ===== SIMPAN FILE =====
            val dir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(dir, "$fileName.pdf")

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Resource.Success("PDF tersimpan di: ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Gagal membuat PDF: ${e.localizedMessage}")
        }
    }

    override suspend fun generatePlaceholderPdf(
        fileName: String,
        reportTitle: String
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var yPosition = drawKopSurat(canvas, paint) + 40f

            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.BLACK

            canvas.drawText("REKAPITULASI DATA", (pageWidth / 2).toFloat(), yPosition, paint)
            yPosition += 20f
            canvas.drawText(reportTitle.uppercase(), (pageWidth / 2).toFloat(), yPosition, paint)

            yPosition += 15f
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
            canvas.drawText("Dicetak pada: $dateStr", (pageWidth / 2).toFloat(), yPosition, paint)

            yPosition += 150f
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.DKGRAY

            val messages = listOf(
                "Mohon Maaf,",
                "Fitur export data laporan lengkap sedang",
                "dalam proses integrasi dengan server",
                "",
                "Pantau terus pada update berikutnya ya!"
            )

            for (line in messages) {
                canvas.drawText(line, (pageWidth / 2).toFloat(), yPosition, paint)
                yPosition += 20f
            }


            drawFooter(canvas, paint)
            pdfDocument.finishPage(page)

            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(dir, "$fileName.pdf")

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Resource.Success("PDF tersimpan di: ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Gagal membuat PDF: ${e.localizedMessage}")
        }
    }

    // ================= HEADER =================
    private fun startNewPage(
        pdfDocument: PdfDocument,
        pageNumber: Int,
        paint: Paint
    ): Pair<PdfDocument.Page, Float> {
        val pageInfo =
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val y = drawKopSurat(canvas, paint) + 30f
        return Pair(page, y)
    }

    private fun drawKopSurat(canvas: Canvas, paint: Paint): Float {
        val logo =
            BitmapFactory.decodeResource(context.resources, R.drawable.logo_prov_lampung)
        val scaledLogo = Bitmap.createScaledBitmap(logo, 60, 80, true)
        canvas.drawBitmap(scaledLogo, margin, margin, paint)

        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER

        val centerX = (pageWidth / 2).toFloat()
        var textY = margin + 15f

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("PEMERINTAH PROVINSI LAMPUNG", centerX, textY, paint)

        textY += 20f
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        canvas.drawText("DINAS KEHUTANAN", centerX, textY, paint)

        textY += 15f
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(
            "Jl. Zainal Abidin Pagar Alam Rajabasa – Bandar Lampung 35144.",
            centerX,
            textY,
            paint
        )

        textY += 15f
        canvas.drawText("Telp. (0721) 703177 Fax. 705058.", centerX, textY, paint)

        textY += 15f
        canvas.drawText(
            "Laman: https://dishut.lampungprov.go.id  Pos-el: dishut@lampungprov.go.id",
            centerX,
            textY,
            paint
        )

        val lineY = margin + 85f
        paint.strokeWidth = 2f
        canvas.drawLine(margin, lineY, pageWidth - margin, lineY, paint)
        paint.strokeWidth = 1f
        canvas.drawLine(margin, lineY + 3f, pageWidth - margin, lineY + 3f, paint)

        return lineY
    }

    private fun drawTableHeader(
        canvas: Canvas,
        paint: Paint,
        headers: List<String>,
        yPos: Float,
        colWidth: Float
    ) {
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        headers.forEachIndexed { index, header ->
            val x = margin + (index * colWidth) + 5f
            canvas.drawText(header, x, yPos, paint)
        }

        canvas.drawLine(
            margin,
            yPos + 5f,
            pageWidth - margin,
            yPos + 5f,
            paint
        )
    }

    // ================= FOOTER =================
    private fun drawFooter(canvas: Canvas, paint: Paint) {
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)

        val dateTime =
            SimpleDateFormat("dd-MM-yyyy: HH:mm:ss", Locale("id", "ID")).format(Date())

        val footer =
            "*dokumen dibuat secara otomatis pada $dateTime oleh Sitanihut."

        canvas.drawText(
            footer,
            (pageWidth / 2).toFloat(),
            pageHeight - 20f,
            paint
        )
    }
}
