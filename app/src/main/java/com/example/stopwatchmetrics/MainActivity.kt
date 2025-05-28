@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.stopwatchmetrics

//import androidx.compose.ui.tooling.preview.Preview
import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.example.stopwatchmetrics.ui.theme.MyApplicationTheme
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.ClientAnchor
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFClientAnchor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.camera.core.Preview as CameraXPreview

// --- Helper Functions & Data Classes ---

@SuppressLint("DefaultLocale")
fun formatTime(timeMs: Long, timeFormatSetting: TimeFormatSetting = TimeFormatSetting()): String {
    return if (timeFormatSetting.useShortFormat) {
        Log.d("DEBUG", "Formatting time in short format")
        // Format as "ss.00"
        val seconds = timeMs / 1000
        val hundredths = (timeMs % 1000) / 10
        String.format("%02d.%02d", seconds, hundredths)
    } else {
        Log.d("DEBUG", "Formatting time in long format")
        // Format as "mm:ss.00"
        val minutes = timeMs / 60000
        val seconds = (timeMs / 1000) % 60
        val hundredths = (timeMs % 1000) / 10
        String.format("%02d:%02d.%02d", minutes, seconds, hundredths)
    }
}

fun formatPointInTime(timeMs: Long): String {
    val date = Date(timeMs)
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
}

data class PointData(
    var elapsedTime: Long,
    val pointStartTime: Long,
    var comment: String = "",
    var imagePath: String? = null
)
data class SheetSettings(
    val showPoint: Boolean = true,
    val showTime: Boolean = true,
    val showTMU: Boolean = false,
    val showStartTime: Boolean = true,
    val showComment: Boolean = true,
    val showImage: Boolean = true,
    val showEmptyComment: Boolean = false,
    val showEmptyImage: Boolean = false,
    val showInstructions: Boolean = true,
    val showGraph: Boolean = false
)

fun generateCSV(
    points: List<PointData>,
    settings: SheetSettings,
    timeFormatSetting: TimeFormatSetting

): String {
    val allHeaders = listOf("Point", "Time", "TMU", "Start Time", "Comment", "Image")
    val enabledHeaders = allHeaders.filter { header ->
        when (header) {
            "Point" -> settings.showPoint
            "Time" -> settings.showTime
            "TMU" -> settings.showTMU
            "Start Time" -> settings.showStartTime
            "Comment" -> settings.showComment
            "Image" -> settings.showImage
            else -> true
        }
    }
    val headerLine = enabledHeaders.joinToString(",")

    // For debugging – log the time format setting:
    Log.d("DEBUG", "generateCSV: useShortFormat = ${timeFormatSetting.useShortFormat}")

    val rows = points.mapIndexed { index, point ->
        val formattedTime = formatTime(point.elapsedTime, timeFormatSetting)
        if (index == 0) {
            Log.d("DEBUG", "Point #1 formatted time: $formattedTime")
        }
        val allCells = listOf(
            "#${index + 1}",
            formattedTime,
            "${(point.elapsedTime / 36).toInt()}",
            formatPointInTime(point.pointStartTime),
            point.comment,
            point.imagePath ?: ""
        )
        val enabledCells = allHeaders.mapIndexedNotNull { i, header ->
            if (enabledHeaders.contains(header)) allCells[i] else null
        }
        enabledCells.joinToString(",")
    }
    return "$headerLine\n${rows.joinToString("\n")}"
}


fun isImageReferencedByOtherCsvFiles(
    imageUri: String,
    currentCsv: File,
    csvDir: File
): Boolean {
    val otherCsvFiles = csvDir.listFiles()?.filter {
        it != currentCsv && it.name.lowercase().endsWith(".csv")
    } ?: emptyList()

    for (csv in otherCsvFiles) {
        try {
            val content = csv.readText()
            // If the CSV contains the imageUri, return true.
            if (content.contains(imageUri)) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return false
}

object ToastHelper {
    private var currentToast: Toast? = null

    /**
     * Show a toast message with a specific display time (in milliseconds).
     * This cancels any previous toast.
     */
    fun showToast(context: Context, message: String, displayTime: Long = 1000L) {
        // Cancel any existing toast.
        currentToast?.cancel()
        currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        currentToast?.show()
        // Cancel it after displayTime ms to shorten the appearance.
        Handler(Looper.getMainLooper()).postDelayed({ currentToast?.cancel() }, displayTime)
    }
}

fun deleteAssociatedImages(csvFile: File) {
    try {
        val content = csvFile.readText()
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return

        // Extract header row and locate the "Image" column index.
        val headers = lines.first().split(",").map { it.trim() }
        val imageIndex = headers.indexOf("Image")
        if (imageIndex == -1) return  // No "Image" column found

        // Use the directory of the current CSV for scanning other files.
        val csvDir = csvFile.parentFile

        // Iterate over each row (after the header) to get image paths.
        for (line in lines.drop(1)) {
            val cells = line.split(",").map { it.trim() }
            if (cells.size > imageIndex) {
                val imageUriStr = cells[imageIndex]
                if (imageUriStr.isNotEmpty()) {
                    // Remove the "file://" prefix if necessary.
                    val path = if (imageUriStr.startsWith("file://"))
                        imageUriStr.removePrefix("file://")
                    else imageUriStr

                    val imageFile = File(path)
                    if (imageFile.exists()) {
                        // Only delete if no other CSV file references this image.
                        val referencedElsewhere = csvDir?.let {
                            isImageReferencedByOtherCsvFiles(imageUriStr, csvFile, it)
                        } ?: false

                        if (!referencedElsewhere) {
                            val deleted = imageFile.delete()
                            Log.d("DeleteCSV", "Deleted image at $path: $deleted")
                        } else {
                            Log.d("DeleteCSV", "Image at $path is still referenced, not deleted.")
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun deleteCsvFileAndAssociatedImages(csvFile: File) {
    // First, delete associated images (if they're not referenced elsewhere)
    deleteAssociatedImages(csvFile)

    // Then, delete the CSV file itself.
    if (csvFile.exists()) {
        val deleted = csvFile.delete()
        Log.d("DeleteCSV", "Deleted CSV file ${csvFile.name}: $deleted")
    }
}

fun getTotalStorageUsedFromFiles(files: List<File>): String {
    val totalBytes = files.sumOf { it.length() }
    val totalMB = totalBytes.toDouble() / (1024 * 1024)  // Convert bytes to MB
    return String.format("%.2f MB", totalMB)
}


// Pre-process image: rotate 90° and scale to desired dimensions.
fun preProcessImageCropKeepOriginal(
    bytes: ByteArray,
    rotationDegrees: Float,
    targetAspectRatio: Float
): Bitmap? {
    // Decode the original bitmap.
    val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

    // Rotate the image.
    val matrix = Matrix().apply { postRotate(rotationDegrees) }
    val rotated = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)

    // Calculate crop dimensions to match the target aspect ratio.
    val rotatedWidth = rotated.width
    val rotatedHeight = rotated.height
    var cropWidth = rotatedWidth
    var cropHeight = (rotatedWidth / targetAspectRatio).toInt()
    if (cropHeight > rotatedHeight) {
        cropHeight = rotatedHeight
        cropWidth = (rotatedHeight * targetAspectRatio).toInt()
    }
    val xOffset = (rotatedWidth - cropWidth) / 2
    val yOffset = (rotatedHeight - cropHeight) / 2

    // Crop the rotated image.
    val cropped = Bitmap.createBitmap(rotated, xOffset, yOffset, cropWidth, cropHeight)

    // Return the cropped image (without scaling, keeping the original resolution of the crop).
    return cropped
}


// Convert a Bitmap to a JPEG byte array with adjustable quality.
fun bitmapToByteArray(bitmap: Bitmap, quality: Int = 100): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return stream.toByteArray()
}



// Function to export points as an XLSX file with embedded images.
fun exportExcelFile(
    context: Context,
    points: List<PointData>,
    settings: SheetSettings,
    timeFormatSetting: TimeFormatSetting   // Added parameter
): File {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Points")
    var colIndex = 0

    // Create header row.
    val headerRow = sheet.createRow(0)
    if (settings.showPoint) headerRow.createCell(colIndex++).setCellValue("Point")
    if (settings.showTime) headerRow.createCell(colIndex++).setCellValue("Time")
    if (settings.showTMU) headerRow.createCell(colIndex++).setCellValue("TMU")
    if (settings.showStartTime) headerRow.createCell(colIndex++).setCellValue("Start Time")
    if (settings.showComment) headerRow.createCell(colIndex++).setCellValue("Comment")
    if (settings.showImage) headerRow.createCell(colIndex++).setCellValue("Image")

    // Create the drawing patriarch to hold images.
    val drawing = sheet.createDrawingPatriarch()

    // Populate data rows.
    points.forEachIndexed { index, point ->
        val row = sheet.createRow(index + 1)
        var currentCol = 0
        if (settings.showPoint) {
            row.createCell(currentCol++).setCellValue((index + 1).toDouble())
        }
        if (settings.showTime) {
            val cell = row.createCell(currentCol++)
            val dataFormat = workbook.createDataFormat()
            val cellStyle = workbook.createCellStyle()

            if (timeFormatSetting.useShortFormat) {
                // Short format: display total seconds with two decimals.
                // Convert milliseconds to seconds.
                val secondsValue = point.elapsedTime.toDouble() / 1000
                cell.setCellValue(secondsValue)
                // Format as a plain number, e.g., 75.32
                cellStyle.dataFormat = dataFormat.getFormat("0.00")
            } else {
                // Long format: display as mm:ss.00.
                // For Excel time format, convert milliseconds into fraction of a day.
                val excelTime = point.elapsedTime.toDouble() / (1000 * 24 * 3600)
                cell.setCellValue(excelTime)
                // Format the cell as time – note that Excel interprets 1 as one full day.
                cellStyle.dataFormat = dataFormat.getFormat("mm:ss.00")
            }
            cell.cellStyle = cellStyle
        }
        if (settings.showTMU) {
            row.createCell(currentCol++).setCellValue((point.elapsedTime / 36).toDouble())
        }
        if (settings.showStartTime) {
            row.createCell(currentCol++).setCellValue(formatPointInTime(point.pointStartTime))
        }
        if (settings.showComment) {
            row.createCell(currentCol++).setCellValue(point.comment)
        }
        if (settings.showImage) {
            val localImagePath = point.imagePath
            if (!localImagePath.isNullOrEmpty()) {
                try {
                    val bytes = readImageBytes(context, localImagePath)
                    if (bytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null) {
                            // Use the original dimensions to calculate a uniform scale factor (1%)
                            val scaleFactor = 0.01f
                            val displayWidthPx = (bitmap.width * scaleFactor).toInt()
                            val displayHeightPx = (bitmap.height * scaleFactor).toInt()
                            val widthEMU = displayWidthPx * 9525
                            val heightEMU = displayHeightPx * 9525

                            // Convert the original bitmap to a JPEG byte array.
                            val imageBytes = bitmapToByteArray(bitmap, quality = 100)

                            // Create an anchor that places the image at the calculated fixed size.
                            val anchor = XSSFClientAnchor(
                                0, 0,
                                widthEMU, heightEMU,
                                currentCol, index + 1,
                                currentCol, index + 1
                            ).apply {
                                anchorType = ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE
                            }

                            val pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG)
                            drawing.createPicture(anchor, pictureIdx)
                        } else {
                            row.createCell(currentCol).setCellValue("Image decoding failed")
                        }
                    } else {
                        row.createCell(currentCol).setCellValue("Image not found")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    row.createCell(currentCol).setCellValue("Error loading image")
                }
            } else {
                row.createCell(currentCol).setCellValue("")
            }
            currentCol++
        }
    }

    // Create the file name using the same date-time format as the CSV file.
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }

    val timeStamp  = SimpleDateFormat("yyyy-MM-dd, HH:mm", Locale.getDefault()).format(Date())
    val fileName   = "exported_points_$timeStamp.xlsx"
    val file       = File(exportDir, fileName)

    FileOutputStream(file).use { workbook.write(it) }
    workbook.close()

    return file
}

fun readImageBytes(context: Context, imagePath: String): ByteArray? {
    return try {
        // Try reading as a direct file path first.
        val file = File(imagePath)
        if (file.exists()) {
            file.readBytes()
        } else {
            // Fallback: try using the ContentResolver for content URIs.
            val uri = imagePath.toUri()
            context.contentResolver.openInputStream(uri)?.readBytes()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


// --- Navigation Enum ---
enum class Screen {
    Stopwatch, FileBrowser, ViewFile, Settings
}
sealed class ActiveDialog {
    object None           : ActiveDialog()
    object SaveShare      : ActiveDialog()
    object Rename         : ActiveDialog()
    object ConfirmReset   : ActiveDialog()    // ← new
    data class Options(val file: File) : ActiveDialog()
    data class DeleteConfirmation(val file: File) : ActiveDialog()
}

class CameraActivity : ComponentActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: Button
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val REQUEST_CODE_CAMERA_PERMISSION = 1001

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CODE_CAMERA_PERMISSION
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        previewView  = findViewById(R.id.previewView)
        captureButton = findViewById(R.id.captureButton)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Check for camera permission and start the camera if granted.
        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        captureButton.setOnClickListener {
            takePhoto()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted; reinitialize the camera use cases.
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // For preview:
            val preview = CameraXPreview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build().also { previewUseCase ->
                    previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
                }

            // For image capture:
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()

            // Bind use cases to lifecycle (example):
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create a file in your app-specific images folder.
        val storageDir = File(getExternalFilesDir(null), "StopwatchMetrics")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        // Declare photoFile here so it’s in scope for the callback.
        val photoFile = File(storageDir, "JPEG_${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${exc.message}", exc)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Read the captured file's bytes into a variable named originalBytes.
                    val originalBytes = photoFile.readBytes()

                    // Process the image: crop while keeping original resolution.
                    val processedBitmap = preProcessImageCropKeepOriginal(
                        originalBytes,
                        rotationDegrees = 90f,
                        targetAspectRatio = 4f / 3f  // For example, a 4:3 aspect ratio.
                    )

                    if (processedBitmap != null) {
                        // Overwrite the original file with the cropped image.
                        FileOutputStream(photoFile).use { fos ->
                            processedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        }
                    } else {
                        Log.e("CameraX", "Image processing failed")
                    }

                    // (Optional) Update MediaStore so other apps see the new file.
                    MediaScannerConnection.scanFile(
                        this@CameraActivity,
                        arrayOf(photoFile.absolutePath),
                        null,
                        null
                    )
                    // Show the toast for image capture.
                    Toast.makeText(this@CameraActivity, "Image captured!", Toast.LENGTH_SHORT).show()

                    // Return the image path to the calling activity
                    val resultIntent = Intent().apply {
                        putExtra("imagePath", photoFile.absolutePath)
                    }
                    setResult(RESULT_OK, resultIntent)

                    finish()
                }
            }
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
// --- Composable Functions ---

@Composable
fun PointProgressIndicator(
    elapsedTime: Long,               // current elapsed time in ms
    points: List<PointData>,         // your list of recorded points
    modifier: Modifier = Modifier,   // ← external size will be applied here
    centerCircleRadius: Dp = 3.dp,
    pointerLengthFraction: Float = 0.8f
) {
    val colorScheme = MaterialTheme.colorScheme

    // fallback to 60 s if no points
    val maxPointTime = points.maxOfOrNull { it.elapsedTime } ?: 60000L
    val progress     = if (maxPointTime > 0) min(elapsedTime / maxPointTime.toFloat(), 1f) else 0f

    // Use only the passed‑in modifier — don’t override it with .size(100.dp)

    val colors = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        val strokeWidth = 8.dp.toPx()

        // background ring
        drawCircle(
            color = colors.surface,
            style = Stroke(width = strokeWidth)
        )

        drawArc(
            color  = colors.primary,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter  = false,
            style      = Stroke(width = strokeWidth)
        )

        // center dot
        drawCircle(
            color       = colorScheme.onBackground,
            center = center,
            radius = centerCircleRadius.toPx()
        )

        // rotating pointer
        if (pointerLengthFraction > 0f) {
            val angleDeg = -90f + 360f * (elapsedTime / maxPointTime.toFloat())
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val length   = (size.minDimension / 2) * pointerLengthFraction
            val end = Offset(
                x = center.x + length * cos(angleRad).toFloat(),
                y = center.y + length * sin(angleRad).toFloat()
            )
            drawLine(
                color       = colorScheme.onBackground,
                start       = center,
                end         = end,
                strokeWidth = 4.dp.toPx()
            )
        }

        // point ticks
        points.forEach { point ->
            val frac     = (point.elapsedTime / maxPointTime.toFloat()).coerceAtMost(1f)
            val rad      = Math.toRadians(frac * 360f - 90f.toDouble())
            val radius   = size.minDimension / 2
            val pinLen   = 8.dp.toPx()
            val outer    = Offset(
                x = center.x + radius * cos(rad).toFloat(),
                y = center.y + radius * sin(rad).toFloat()
            )
            val inner    = Offset(
                x = center.x + (radius - pinLen) * cos(rad).toFloat(),
                y = center.y + (radius - pinLen) * sin(rad).toFloat()
            )
            drawLine(
                color       = colorScheme.onBackground,
                start       = outer,
                end         = inner,
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}






@Composable
fun ImageCaptureDialog(
    onCapture: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Image") },
        text = { Text("Would you like to capture a new image for this record?") },
        confirmButton = {
            Button(
                onClick = onCapture,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Capture")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PointTable(
    points: List<PointData>,
    currentActivePoint: PointData?,
    timeFormatSetting: TimeFormatSetting,
    sheetSettings: SheetSettings,
    onCommentClick: (PointData) -> Unit,
    onImageClick: (PointData) -> Unit,
    onAddCommentForLive: () -> Unit,
    onCaptureImageForLive: () -> Unit
) {
    // decide if we actually show those columns
    val hasComments = points.any { it.comment.isNotBlank() }
    val hasImages   = points.any { !it.imagePath.isNullOrEmpty() }
    val showCommentColumn = sheetSettings.showComment &&
            (sheetSettings.showEmptyComment || hasComments)
    val showImageColumn = sheetSettings.showImage &&
            (sheetSettings.showEmptyImage   || hasImages)

    Column(modifier = Modifier.fillMaxWidth()) {
        // ─── header row ───
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            if (sheetSettings.showPoint)     Text("Point",      Modifier.weight(1f), textAlign = TextAlign.Center)
            if (sheetSettings.showTime)      Text("Time",       Modifier.weight(1f), textAlign = TextAlign.Center)
            if (sheetSettings.showTMU)       Text("TMU",        Modifier.weight(1f), textAlign = TextAlign.Center)
            if (sheetSettings.showStartTime) Text("Start Time", Modifier.weight(1f), textAlign = TextAlign.Center)
            if (showCommentColumn)           Text("Comment",    Modifier.weight(1f), textAlign = TextAlign.Center)
            if (showImageColumn)             Text("Image",      Modifier.weight(1f), textAlign = TextAlign.Center)
        }

        // ─── data rows ───
        LazyColumn {
            items(points.reversed()) { point ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    if (sheetSettings.showPoint) {
                        val idx = points.size - points.reversed().indexOf(point)
                        Text("#$idx", Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                    if (sheetSettings.showTime) {
                        Text(
                            formatTime(point.elapsedTime, timeFormatSetting),
                            Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                    if (sheetSettings.showTMU) {
                        Text(
                            "${(point.elapsedTime / 36).toInt()}",
                            Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                    if (sheetSettings.showStartTime) {
                        Text(
                            formatPointInTime(point.pointStartTime),
                            Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }

                    // ── COMMENT cell ──
                    if (showCommentColumn) {
                        Text(
                            text = if (point.comment.isNotBlank()) point.comment else "—",
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (point.pointStartTime == currentActivePoint?.pointStartTime) {
                                        onAddCommentForLive()
                                    } else {
                                        onCommentClick(point)
                                    }
                                },
                            textAlign = TextAlign.Center
                        )
                    }

                    // ── IMAGE cell ──
                    if (showImageColumn) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (point.pointStartTime == currentActivePoint?.pointStartTime) {
                                        onCaptureImageForLive()
                                    } else {
                                        onImageClick(point)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!point.imagePath.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(point.imagePath),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            } else {
                                Text("—", textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun GraphPreview(
    points: List<PointData>,
    modifier: Modifier = Modifier
) {
    // grab your color in a composable context:
    val lineColor = MaterialTheme.colorScheme.onBackground

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val numPoints = points.size

        if (numPoints > 1) {
            val maxTime = points.maxOfOrNull { it.elapsedTime } ?: 1L
            val xStep   = width / (numPoints - 1)

            val path = Path().apply {
                moveTo(0f, height - (points[0].elapsedTime.toFloat() / maxTime) * height)
                for (i in 1 until numPoints) {
                    val x = i * xStep
                    val y = height - (points[i].elapsedTime.toFloat() / maxTime) * height
                    lineTo(x, y)
                }
            }

            drawPath(
                path  = path,
                color = lineColor,                   // use the pre-captured color
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}


@Composable
fun FileOptionsDialog(
    file: File,
    onView: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("File Options") },
        text = { Text("What would you like to do with \"${file.name}\"?") },
        confirmButton = {
            // Combine all buttons in one Column to prevent overlap.
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onView,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete")
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        },
        // Remove the separate dismissButton.
        dismissButton = {}
    )
}

@Composable
fun DeleteConfirmationDialog(
    file: File,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Delete") },
        text = { Text("Are you sure you want to delete \"${file.name}\"?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CsvTable(csvContent: String, sheetSettings: SheetSettings) {
    // Split CSV content into lines.
    val allRows = csvContent.split("\n").filter { it.isNotBlank() }
    if (allRows.isEmpty()) {
        Text("No data available", color = MaterialTheme.colorScheme.onBackground)
        return
    }
    // Get header and data rows.
    val headerRow = allRows.first()
    val headerCells = headerRow.split(",").map { it.trim() }
    val dataRows = allRows.drop(1)

    // Determine enabled indices based on the full header.
    val enabledIndices = headerCells.mapIndexedNotNull { index, column ->
        when (column) {
            "Point" -> if (sheetSettings.showPoint) index else null
            "Time" -> if (sheetSettings.showTime) index else null
            "TMU" -> if (sheetSettings.showTMU) index else null
            "Start Time" -> if (sheetSettings.showStartTime) index else null
            "Comment" -> if (sheetSettings.showComment) index else null
            "Image" -> if (sheetSettings.showImage) index else null
            else -> index
        }
    }

    // Filter header based on enabled indices.
    val filteredHeaderCells = enabledIndices.map { headerCells[it] }

    // State for enlarged image preview.
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Display header row.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            filteredHeaderCells.forEach { cell ->
                Text(
                    text = cell,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        // Display data rows.
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(dataRows) { row ->
                val cells = row.split(",").map { it.trim() }
                val filteredCells = enabledIndices.map { cells.getOrElse(it) { "" } }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    filteredCells.forEachIndexed { index, cell ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .let {
                                    if (filteredHeaderCells[index] == "Image" && cell.isNotBlank()) {
                                        it.clickable { selectedImageUri = cell }
                                    } else it
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (filteredHeaderCells[index] == "Image" && cell.isNotBlank()) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = cell),
                                    contentDescription = "Image preview",
                                    modifier = Modifier.size(48.dp)
                                )
                            } else {
                                Text(
                                    text = cell,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedImageUri != null) {
        Dialog(onDismissRequest = { selectedImageUri = null }) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = selectedImageUri),
                    contentDescription = "Enlarged image",
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { selectedImageUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}


@Composable
fun SettingsScreen(
    sheetSettings: SheetSettings,
    timeFormatSetting: TimeFormatSetting,
    fastCommentsSettings: FastCommentsSettings,
    onSheetSettingsChange: (SheetSettings) -> Unit,
    onTimeFormatChange: (TimeFormatSetting) -> Unit,
    onFastCommentsChange: (FastCommentsSettings) -> Unit,
    onBack: () -> Unit
) {
    var showTMUInfoDialog by remember { mutableStateOf(false) }
    var showFastCommentsEditDialog by remember { mutableStateOf(false) }

    // capture a Context to use inside the SettingsRow lambda
    val context = LocalContext.current

    val useDarkMode by readDarkModeSetting(context)
        .collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Toggle columns in your sheet:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            SettingsRow(
                label = "Show Point",
                checked = sheetSettings.showPoint,
                onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showPoint = it)) }
            )
            SettingsRow(
                label = "Show Time",
                checked = sheetSettings.showTime,
                onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showTime = it)) }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                SettingsRow(
                    label = "Show TMU",
                    checked = sheetSettings.showTMU,
                    onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showTMU = it)) }
                )
                IconButton(
                    onClick = { showTMUInfoDialog = true },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-75).dp)     // tweak this offset as needed
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "What is TMU?"
                    )
                }
            }

            SettingsRow(
                label = "Show Start Time",
                checked = sheetSettings.showStartTime,
                onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showStartTime = it)) }
            )
            SettingsRow(
                label = "Show Comment",
                checked = sheetSettings.showComment,
                onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showComment = it)) }
            )
            SettingsRow(
                label = "Show Comment If Empty",
                checked = sheetSettings.showEmptyComment,
                onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showEmptyComment = it)) }
            )
            SettingsRow(
                label = "Show Image",
                checked = sheetSettings.showImage,
                onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showImage = it)) }
            )
            SettingsRow(
                label = "Show Image If Empty",
                checked = sheetSettings.showEmptyImage,
                onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showEmptyImage = it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(
                color = MaterialTheme.colorScheme.onBackground,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Other Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            SettingsRow(
                label = "Show Control Buttons",
                checked = sheetSettings.showInstructions,
                onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showInstructions = it)) }
            )

            SettingsRow(
                label = "Use Dark Theme",
                checked = useDarkMode,
                onCheckedChange = { checked ->
                    CoroutineScope(Dispatchers.IO).launch {
                        saveDarkModeSetting(context, checked)
                    }
                }
            )

            SettingsRow(
                label = "Show 2D Line Graph",
                checked = sheetSettings.showGraph,
                onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showGraph = it)) }
            )

            SettingsRow(
                label = "Use short time format (ss.00)",
                checked = timeFormatSetting.useShortFormat,
                onCheckedChange = { checked ->
                    // update UI state
                    onTimeFormatChange(TimeFormatSetting(useShortFormat = checked))
                    // persist it using the captured context
                    CoroutineScope(Dispatchers.IO).launch {
                        saveTimeFormatSetting(context, checked)
                    }
                }
            )

            SettingsRow(
                label = "Use Fast Comments Window",
                checked = fastCommentsSettings.enabled,
                onCheckedChange = { onFastCommentsChange(fastCommentsSettings.copy(enabled = it)) }
            )
            Button(
                onClick = { showFastCommentsEditDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Fast Comments")
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/elias_svensson_apps"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocalCafe,
                    contentDescription = "Coffee Icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Buy the developer of this app a coffee <3")
            }
        }
    }

    if (showTMUInfoDialog) {
        AlertDialog(
            onDismissRequest = { showTMUInfoDialog = false },
            title = { Text("TMU Information") },
            text = {
                Text(
                    """
                    TMU (Time Measuring Unit)
                    
                    1 TMU equals:
                    • 0.000010 hours
                    • 0.00060 minutes
                    • 0.036 seconds
                    
                    Conversely:
                    • 1 second ≈ 27.78 TMU
                    • 1 minute ≈ 1,667 TMU
                    • 1 hour = 100,000 TMU
                    """.trimIndent()
                )
            },
            confirmButton = {
                Button(onClick = { showTMUInfoDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showFastCommentsEditDialog) {
        FastCommentsEditDialog(
            initialFastComments = fastCommentsSettings,
            onConfirm = { newSettings ->
                onFastCommentsChange(newSettings)
                showFastCommentsEditDialog = false
            },
            onDismiss = { showFastCommentsEditDialog = false }
        )
    }
}



@Composable
fun FastCommentsEditDialog(
    initialFastComments: FastCommentsSettings,
    onConfirm: (FastCommentsSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var fastComments by remember { mutableStateOf(initialFastComments) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Fast Comments") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fastComments.comment1,
                    onValueChange = { fastComments = fastComments.copy(comment1 = it) },
                    label = { Text("Fast Comment 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fastComments.comment2,
                    onValueChange = { fastComments = fastComments.copy(comment2 = it) },
                    label = { Text("Fast Comment 2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fastComments.comment3,
                    onValueChange = { fastComments = fastComments.copy(comment3 = it) },
                    label = { Text("Fast Comment 3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fastComments.comment4,
                    onValueChange = { fastComments = fastComments.copy(comment4 = it) },
                    label = { Text("Fast Comment 4") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fastComments.comment5,
                    onValueChange = { fastComments = fastComments.copy(comment5 = it) },
                    label = { Text("Fast Comment 5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fastComments.comment6,
                    onValueChange = { fastComments = fastComments.copy(comment6 = it) },
                    label = { Text("Fast Comment 6") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(fastComments) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SettingsRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun FileViewerScreen(
    file: File,
    onBack: () -> Unit,
    sheetSettings: SheetSettings,
    onExportExcel: (File) -> Unit // New callback for export action
) {
    val isImage = file.extension.lowercase() in listOf("jpg", "jpeg", "png")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(file.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .clickable { onExportExcel(file) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Export as Excel"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Export as Excel",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding)
        ) {
            if (isImage) {
                Image(
                    painter = rememberAsyncImagePainter(file),
                    contentDescription = "Image",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val fileContent = try { file.readText() } catch (e: Exception) { "Error reading file: ${e.message}" }
                CsvTable(csvContent = fileContent, sheetSettings = sheetSettings)
            }
        }
    }
}

// --- Dialog for Save/Share Options ---
@Composable
fun SaveShareOptionsDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Action") },
        text = { Text("Do you want to save your recorded times or share them?") },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save File") }
                Button(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Share CSV") }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel") }
            }
        },
        // Remove the dismissButton parameter.
        dismissButton = {}
    )
}

@Composable
fun CommentDialogUnified(
    initialComment: String,
    fastCommentsSettings: FastCommentsSettings,
    isEditingOldPoint: Boolean, // new parameter to determine behavior
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var comment by remember { mutableStateOf(initialComment) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialComment.isEmpty()) "Add Comment" else "Edit Comment")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
                if (fastCommentsSettings.enabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fast Comments", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp
                    ) {
                        listOf(
                            fastCommentsSettings.comment1,
                            fastCommentsSettings.comment2,
                            fastCommentsSettings.comment3,
                            fastCommentsSettings.comment4,
                            fastCommentsSettings.comment5,
                            fastCommentsSettings.comment6
                        ).forEach { fastComment ->
                            Button(
                                onClick = {
                                    // Only show toast if it's not an edit on an old point.
                                    if (!isEditingOldPoint) {
                                        ToastHelper.showToast(context, "Fast comment '$fastComment' added", 1000L)
                                    }
                                    onConfirm(fastComment)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor   = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = fastComment, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                Button(onClick = { onConfirm("") }) {
                    Text("Clear")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onConfirm(comment) }) {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}





// Dialog to prompt for file renaming.
@Composable
fun RenameFileDialog(
    defaultName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var fileName by remember { mutableStateOf(defaultName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename File") },
        text = {
            Column {
                Text("Enter file name:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    singleLine = true,
                    label = { Text("File Name") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(fileName) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FileBrowserScreen(
    onBack: () -> Unit,
    contextFilesDir: File,
    onFileSelected: (File) -> Unit,
    onDeleteAllFiles: () -> Unit,
    showCsv: Boolean,
    showImages: Boolean,
    onShowCsvChange: (Boolean) -> Unit,
    onShowImagesChange: (Boolean) -> Unit
) {
    // All files in the folder as state
    var filesList by remember { mutableStateOf(contextFilesDir.listFiles()?.toList() ?: emptyList()) }

    // Filter dialog visibility
    var showFilterDialog by remember { mutableStateOf(false) }

    // Compute displayed files based on filter
    val displayedFiles by remember(filesList, showCsv, showImages) {
        derivedStateOf {
            filesList.filter { file ->
                (showCsv    && file.extension.equals("csv", ignoreCase = true)) ||
                        (showImages && listOf("jpg","jpeg","png").contains(file.extension.lowercase()))
            }
                .sortedByDescending { it.lastModified() }
        }
    }

    // Compute totals for summary (all files, not only displayed)
    val totalCsvCount by remember(filesList) { derivedStateOf { filesList.count { it.extension.equals("csv", ignoreCase = true) } } }
    val totalImageCount by remember(filesList) { derivedStateOf { filesList.count { listOf("jpg","jpeg","png").contains(it.extension.lowercase()) } } }
    val totalStorageAll by remember(filesList) { derivedStateOf { getTotalStorageUsedFromFiles(filesList) } }

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var fileDialogState by remember { mutableStateOf<ActiveDialog>(ActiveDialog.None) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Saved Files")
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter files",
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteAllDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete all files",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column(Modifier.fillMaxSize()) {
                // Summary row always reflects all files
                Row(Modifier.padding(8.dp)) {
                    Text(
                        text = "Files Found: $totalCsvCount CSV, $totalImageCount Image",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Storage Used: $totalStorageAll",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                LazyColumn(Modifier.fillMaxSize()) {
                    items(displayedFiles) { file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { fileDialogState = ActiveDialog.Options(file) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = file.name,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            if (showDeleteAllDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteAllDialog = false },
                    title = { Text("Delete All Files") },
                    text = { Text("Are you sure you want to delete all files?") },
                    confirmButton = {
                        Button(onClick = {
                            onDeleteAllFiles()
                            showDeleteAllDialog = false
                            Toast.makeText(context, "All files deleted", Toast.LENGTH_SHORT).show()
                            filesList = contextFilesDir.listFiles()?.toList() ?: emptyList()
                        }) { Text("Delete") }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDeleteAllDialog = false }) { Text("Cancel") }
                    }
                )
            }

            when (fileDialogState) {
                is ActiveDialog.Options -> {
                    val file = (fileDialogState as ActiveDialog.Options).file
                    FileOptionsDialog(
                        file = file,
                        onView = {
                            onFileSelected(file)
                            fileDialogState = ActiveDialog.None
                        },
                        onDelete = { fileDialogState = ActiveDialog.DeleteConfirmation(file) },
                        onDismiss = { fileDialogState = ActiveDialog.None }
                    )
                }
                is ActiveDialog.DeleteConfirmation -> {
                    val file = (fileDialogState as ActiveDialog.DeleteConfirmation).file
                    DeleteConfirmationDialog(
                        file = file,
                        onConfirm = {
                            deleteCsvFileAndAssociatedImages(file)
                            Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
                            filesList = contextFilesDir.listFiles()?.toList() ?: emptyList()
                            fileDialogState = ActiveDialog.None
                        },
                        onDismiss = { fileDialogState = ActiveDialog.None }
                    )
                }
                else -> {}
            }
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter files") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Show CSV files")
                        Spacer(Modifier.weight(1f))
                        Switch(checked = showCsv, onCheckedChange = onShowCsvChange)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Show images")
                        Spacer(Modifier.weight(1f))
                        Switch(checked = showImages, onCheckedChange = onShowImagesChange)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFilterDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}






// ---  MainScreen ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TouchVolumeButton(
    icon: ImageVector,
    label: String,
    tapInstruction: String = "",
    holdInstruction: String = "",
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconSize: Dp = 36.dp,
    textFontSize: TextUnit = 14.sp,
    instructionFontSize: TextUnit = 12.sp
) {
    // keep a single interactionSource if you need it
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = iconTint
        )
        Spacer(Modifier.height(4.dp))
        if (label.isNotBlank()) {
            Text(label, fontSize = textFontSize, color = iconTint)
        }
        if (tapInstruction.isNotBlank()) {
            Text("Tap: $tapInstruction", fontSize = instructionFontSize, color = iconTint)
        }
        if (holdInstruction.isNotBlank()) {
            Text("Hold: $holdInstruction", fontSize = instructionFontSize, color = iconTint)
        }
    }
}








@Composable
fun ThreePageSwipeContainer(
    modifier: Modifier = Modifier,
    threshold: Float = 50f,
    content: List<@Composable () -> Unit>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit
) {
    require(content.size == 3) { "Must pass exactly three pages" }
    var dragAccum by remember { mutableStateOf(0f) }

    Box(modifier
        .pointerInput(currentPage) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    val newPage = when {
                        dragAccum > threshold && currentPage > 0 -> currentPage - 1
                        dragAccum < -threshold && currentPage < 2 -> currentPage + 1
                        else -> currentPage
                    }
                    dragAccum = 0f
                    if (newPage != currentPage) onPageChanged(newPage)
                },
                onHorizontalDrag = { _, delta -> dragAccum += delta },
                onDragCancel = { dragAccum = 0f }
            )
        }
    ) {
        content[currentPage]()
    }
}


@Composable
fun MainScreen(
    elapsedTime: Long,
    isRunning: Boolean,
    points: List<PointData>,
    currentActivePoint: PointData?,
    timeFormatSetting: TimeFormatSetting,
    sheetSettings: SheetSettings,
    activeDialog: ActiveDialog,
    onActiveDialogChange: (ActiveDialog) -> Unit,
    onToggleStopwatch: () -> Unit,
    onNewPoint: () -> Unit,
    isDirty: Boolean,
    onResetRequest: () -> Unit,
    onPrepareSaveShare: () -> Unit,
    onFileBrowserClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShare: () -> Unit,
    onRenameConfirm: (String) -> Unit,
    onCaptureImage: () -> Unit,
    onAddComment: () -> Unit,
    onUndo: () -> Unit,
    fastCommentsSettings: FastCommentsSettings,
    onImageClick: (PointData) -> Unit,
    onUpdatePointImage: (PointData, String) -> Unit,
    onUpdatePointComment: (PointData, String) -> Unit,
    onToggleImageColumn: () -> Unit,
    onToggleCommentColumn: () -> Unit,
) {

    val context = LocalContext.current

    // 1) read the preference – initial = null means “nothing loaded yet”
    val showTipsPref: Boolean? by readShowTips(context)
        .collectAsState(initial = null)   // type is Boolean? because of the var above

// 2) local UI-state for the dialog itself
    var showTipsDialog by remember { mutableStateOf(false) }

// 3) open the dialog once the preference arrives and is `true`
    LaunchedEffect(showTipsPref) {
        if (showTipsPref == true) {       // only when it’s explicitly true
            showTipsDialog = true
        }
    }

    var dontShowAgain by remember { mutableStateOf(false) }   // ② local state

    var selectedPointForComment by remember { mutableStateOf<PointData?>(null) }
    var selectedPointForImage by remember { mutableStateOf<PointData?>(null) }
    var currentPage by remember { mutableStateOf(1) }    // start in middle

    val maxTime      = points.maxOfOrNull { it.elapsedTime } ?: 60000L
    val medianTime   = calculateMedian(points.map { it.elapsedTime })
    val stdDevTime   = calculateStdDev(points.map { it.elapsedTime })
    val averageTime  = points.map { it.elapsedTime }.average().toLong()

    // only rebuild when points or the elapsed‐time of the live point changes
    val displayPoints by remember(points, currentActivePoint?.pointStartTime, currentActivePoint?.elapsedTime, isRunning) {
        derivedStateOf {
            currentActivePoint?.let { live ->
                // if it's not already in `points`, show it any time elapsed >0 *or* we're paused
                if (points.none { it.pointStartTime == live.pointStartTime } &&
                    (live.elapsedTime > 0L || !isRunning)
                ) {
                    points + live
                } else points
            } ?: points
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stopwatch Metrics")
                        IconButton(onClick = { showTipsDialog = true }) {
                            Icon(Icons.Default.Info, "Tips", Modifier.size(30.dp))
                        }
                        IconButton(onClick = onFileBrowserClick) {
                            Icon(Icons.Filled.FolderOpen, "Saved", Modifier.size(30.dp))
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Filled.Settings, "Settings", Modifier.size(30.dp))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Undo + view‑indicator
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier.clickable { onUndo() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, "Undo")
                    Spacer(Modifier.width(8.dp))
                    Text("Undo last point", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.weight(1f))
                // dots
                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(3) { idx ->
                        val color = if (idx == currentPage)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surface
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(color, CircleShape)
                                .padding(4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Pager ──

            ThreePageSwipeContainer(
                modifier      = Modifier
                    .fillMaxSize()            // take all available vertical space
                    .padding(horizontal = 8.dp),
                threshold     = 50f,
                content       = listOf(

                    // ─── PAGE 0 ───
                    {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Spacer(Modifier.height(16.dp))

                            // Box to stack the PPI and the counter text
                            Box(
                                modifier = Modifier.size(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                PointProgressIndicator(
                                    elapsedTime           = elapsedTime,
                                    points                = displayPoints,
                                    centerCircleRadius    = 0.dp,
                                    pointerLengthFraction = 0f,
                                    modifier              = Modifier.matchParentSize()
                                )

                                val displayCount = displayPoints.size
                                val counterText = if (displayCount > 0) "#$displayCount" else ""

                                Text(
                                    text     = counterText,
                                    fontSize = 38.sp,
                                    color    = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            Spacer(Modifier.height(16.dp))
                            Text(
                                text    = formatTime(elapsedTime, timeFormatSetting),
                                fontSize = 28.sp,
                                color    = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(24.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TouchVolumeButton(
                                    icon            = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    label           = "",
                                    tapInstruction  = "Play/Pause",
                                    holdInstruction = "Save",
                                    onTap           = onToggleStopwatch,
                                    onLongPress     = onPrepareSaveShare,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    iconTint        = MaterialTheme.colorScheme.onBackground,
                                    iconSize        = 80.dp,
                                    textFontSize = 16.sp,
                                    instructionFontSize = 14.sp,
                                    modifier        = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                )
                                TouchVolumeButton(
                                    icon            = Icons.Filled.Timer,
                                    label           = "",
                                    tapInstruction  = "New Point",
                                    holdInstruction = "Reset",
                                    onTap           = onNewPoint,
                                    onLongPress     = onResetRequest,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    iconTint        = MaterialTheme.colorScheme.onBackground,
                                    iconSize        = 80.dp,
                                    textFontSize = 16.sp,
                                    instructionFontSize = 14.sp,
                                    modifier        = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                            }
                        }
                    },

                    // ─── PAGE 1: PPI + timer + play/reset row + camera/comment row + point‑table ───
                    {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(16.dp))
                            PointProgressIndicator(
                                elapsedTime           = elapsedTime,
                                points                = displayPoints,
                                centerCircleRadius    = 3.dp,
                                pointerLengthFraction = 0.8f,
                                modifier              = Modifier.size(120.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(formatTime(elapsedTime, timeFormatSetting),
                                fontSize = 28.sp,
                                color    = MaterialTheme.colorScheme.onBackground)
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                TouchVolumeButton(
                                    icon            = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    label           = "",
                                    tapInstruction  = "Play/Pause",
                                    holdInstruction = "Save",
                                    onTap           = onToggleStopwatch,
                                    onLongPress     = onPrepareSaveShare,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    iconTint        = MaterialTheme.colorScheme.onBackground,
                                    modifier        = Modifier.weight(1f).padding(8.dp)
                                )
                                TouchVolumeButton(
                                    icon            = Icons.Filled.Timer,
                                    label           = "",
                                    tapInstruction  = "New Point",
                                    holdInstruction = "Reset",
                                    onTap           = onNewPoint,
                                    onLongPress     = onResetRequest,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    iconTint        = MaterialTheme.colorScheme.onBackground,
                                    modifier        = Modifier.weight(1f).padding(8.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Toggle Comments
                                IconButton(
                                    onClick = {
                                        val newSettings = sheetSettings.copy(
                                            showComment      = !sheetSettings.showComment,
                                            showEmptyComment = true
                                        )
                                        CoroutineScope(Dispatchers.IO).launch {
                                            saveSheetSettings(context, newSettings)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)                          // make the touch target big enough
                                        .clip(CircleShape)                    // clip to circle
                                        .border(
                                            1.dp,
                                            if (sheetSettings.showComment) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Toggle Comments",
                                        tint = if (sheetSettings.showComment)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                // Toggle Images
                                IconButton(
                                    onClick = {
                                        val newSettings = sheetSettings.copy(
                                            showImage      = !sheetSettings.showImage,
                                            showEmptyImage = true
                                        )
                                        CoroutineScope(Dispatchers.IO).launch {
                                            saveSheetSettings(context, newSettings)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .border(
                                            1.dp,
                                            if (sheetSettings.showImage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Filled.CameraAlt,
                                        contentDescription = "Toggle Images",
                                        tint = if (sheetSettings.showImage)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }


                            Spacer(Modifier.height(16.dp))

                            PointTable(
                                points                = displayPoints,
                                currentActivePoint    = currentActivePoint,
                                timeFormatSetting     = timeFormatSetting,
                                sheetSettings         = sheetSettings,
                                onCommentClick        = { selectedPointForComment = it },
                                onImageClick          = { onImageClick(it) },
                                onAddCommentForLive   = onAddComment,
                                onCaptureImageForLive = onCaptureImage
                            )
                        }
                    },

                    // ─── PAGE 2: graph + timer + table ───────────────────────────────────────────
                    {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(top = 16.dp, bottom = 16.dp),   // only vertical padding here
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            GraphPreview(
                                points = displayPoints,
                                modifier = Modifier
                                    .fillMaxWidth()                           // now full width
                                    .padding(horizontal = 16.dp)              // horizontal inset only for the graph
                                    .height(120.dp)
                            )
                            Spacer(Modifier.height(12.dp))

                            Text(
                                formatTime(elapsedTime, timeFormatSetting),
                                fontSize = 28.sp,
                                color   = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(Modifier.height(12.dp))
                            Column(
                                Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.End          // right-align like before
                            ) {
                                Text("Max:     ${formatTime(maxTime,       timeFormatSetting)}",
                                    style = MaterialTheme.typography.bodyMedium)
                                Text("Median:  ${formatTime(medianTime.toLong(), timeFormatSetting)}",
                                    style = MaterialTheme.typography.bodyMedium)
                                Text("Average: ${formatTime(averageTime,  timeFormatSetting)}",
                                    style = MaterialTheme.typography.bodyMedium)
                                Text("Std Dev: ${formatTime(stdDevTime.toLong(), timeFormatSetting)}",
                                    style = MaterialTheme.typography.bodyMedium)
                            }


                            Spacer(Modifier.height(12.dp))
                            PointTable(
                                points                = displayPoints,
                                currentActivePoint    = currentActivePoint,
                                timeFormatSetting     = timeFormatSetting,
                                sheetSettings         = sheetSettings,
                                onCommentClick        = { selectedPointForComment = it },
                                onImageClick          = { onImageClick(it) },
                                onAddCommentForLive   = onAddComment,
                                onCaptureImageForLive = onCaptureImage,
                            )
                        }
                    }
                ),
                currentPage   = currentPage,
                onPageChanged = { currentPage = it }
            )
        }
    }

    // Dialogs:
    when (activeDialog) {
        is ActiveDialog.Rename -> {
            val defaultName = SimpleDateFormat("yyyy-MM-dd, HH:mm", Locale.getDefault()).format(Date())
            RenameFileDialog(
                defaultName = defaultName,
                onConfirm = { newName ->
                    onRenameConfirm(newName)
                    onActiveDialogChange(ActiveDialog.None)
                },
                onDismiss = { onActiveDialogChange(ActiveDialog.None) }
            )
        }
        is ActiveDialog.SaveShare -> {
            SaveShareOptionsDialog(
                onDismiss = { onActiveDialogChange(ActiveDialog.None) },
                onSave = { onActiveDialogChange(ActiveDialog.Rename) },
                onShare = {
                    onShare()
                    onActiveDialogChange(ActiveDialog.None)
                }
            )
        }
        is ActiveDialog.DeleteConfirmation -> {
            DeleteConfirmationDialog(
                file = activeDialog.file,
                onConfirm = { onActiveDialogChange(ActiveDialog.None) },
                onDismiss = { onActiveDialogChange(ActiveDialog.None) }
            )
        }
        ActiveDialog.None -> { }
        else -> { }
    }

    selectedPointForComment?.let { point ->
        CommentDialogUnified(
            initialComment = point.comment,
            fastCommentsSettings = fastCommentsSettings,
            isEditingOldPoint = true,
            onConfirm = { newComment ->
                onUpdatePointComment(point, newComment)
                selectedPointForComment = null
            },
            onDismiss = { selectedPointForComment = null }
        )
    }

    selectedPointForImage?.let {
        ImageCaptureDialog(
            onCapture = {
                onCaptureImage()
                selectedPointForImage = null
            },
            onDismiss = { selectedPointForImage = null }
        )
    }
    // inside MainScreen – or wherever you show the tips dialog

    if (showTipsDialog) {
        AlertDialog(
            onDismissRequest = { showTipsDialog = false },
            title = { Text("Start Guide") },

            /* ───────────────────────────────────── text ───────────────────────────────────── */
            text = {
                Column {

                    // plain-text bullets
                    Text("• You can use the volume buttons for Play/Pause and New-Point (tap and hold).")
                    Text("• Tweak the layout in Settings to match your preferences.")
                    Text("• Swipe left / right for the three different views.")
                    Text("• In Saved Files you can filter, view, delete or export to Excel.")
                    Text("• Tap the info icon any time to see this guide again.")

                    Spacer(Modifier.height(12.dp))

                    // “don’t show again” checkbox
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = dontShowAgain,
                            onCheckedChange = { dontShowAgain = it }
                        )
                        Text("Don't show again")
                    }
                }
            },

            /* ───────────────────────────── confirm button ───────────────────────────── */
            confirmButton = {
                Button(
                    onClick = {
                        if (dontShowAgain) {
                            // persist the preference so the dialog won’t appear next launch
                            CoroutineScope(Dispatchers.IO).launch {
                                saveShowTips(context, false)
                            }
                        }
                        showTipsDialog = false
                    }
                ) { Text("Got it!") }
            }
        )
    }
}






// --- MainActivity ---
class MainActivity : ComponentActivity() {



    private var isDirty by mutableStateOf(false)

    private var currentActivePoint: PointData? = null

    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>

    private val REQUEST_CODE_CAMERA_PERMISSION = 1001

    private var pendingImageUpdatePoint: PointData? = null

    private var fbShowCsv by mutableStateOf(true)
    private var fbShowImages by mutableStateOf(false)

    // Function to check if the CAMERA permission is granted.
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request the CAMERA permission.
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CODE_CAMERA_PERMISSION
        )
    }



    // Function to capture an image.
    private fun captureImage() {
        // Check if permission is granted before capturing.
        if (!checkCameraPermission()) {
            requestCameraPermission()
            return
        }
        // Create a temporary file for the image.
        val imageFile = createImageFile()
        val localUri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            imageFile
        )
        currentImageUri = localUri
        startActivity(Intent(this, CameraActivity::class.java))
    }

    // Override onRequestPermissionsResult to handle permission responses.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage()  // Permission granted, now capture image.
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // For viewing files.
    private var selectedFile: File? by mutableStateOf(null)

    // Rename file state.
    private var pendingCsvContent: String? = null

    // Stopwatch state.
    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> = _isRunning

    private var startTime: Long = 0L
    private var accumulatedTime: Long = 0L

    // Current Point start.
    private var currentPointStartTime: Long = 0L

    // Elapsed time state.
    private val _elapsedTime = mutableStateOf(0L)
    val elapsedTime: State<Long> = _elapsedTime

    // List of Points.
    private val _points = mutableStateListOf<PointData>()
    val points: List<PointData> get() = _points

    // Volume key tracking.
    private var volumeDownPressStart: Long = 0L
    private var volumeUpPressStart: Long = 0L

    private var activeDialog by mutableStateOf<ActiveDialog>(ActiveDialog.None)

    private var currentImageUri: Uri? = null

    private var sheetSettings by mutableStateOf(SheetSettings())

    // Navigation state.
    var currentScreen by mutableStateOf(Screen.Stopwatch)
        private set

    private fun saveCsvFile(fileName: String) {
        val baseName = if (fileName.endsWith(".csv")) fileName.substringBeforeLast(".csv") else fileName
        var finalName = "$baseName.csv"

        // Use your app-specific folder:
        val csvDir = File(getExternalFilesDir(null), "StopwatchMetrics")
        if (!csvDir.exists()) {
            csvDir.mkdirs()
        }

        var file = File(csvDir, finalName)
        var counter = 2
        while (file.exists()) {
            finalName = "$baseName ($counter).csv"
            file = File(csvDir, finalName)
            counter++
        }

        val csvContent = generateCSV(points, sheetSettings, currentTimeFormatSetting)
        Log.d("DEBUG", "CSV Content:\n$csvContent")  // Log CSV content for debug if needed.
        file.outputStream().use {
            it.write(csvContent.toByteArray())
        }
        Toast.makeText(this, "CSV file saved as $finalName", Toast.LENGTH_SHORT).show()
    }

    private fun undoLastPoint() {
        if (_points.isNotEmpty()) {
            // Remove the latest recorded point
            val removed = _points.removeAt(_points.lastIndex)
            // Add its elapsed time back to the accumulated time
            accumulatedTime += removed.elapsedTime
            // Update the UI: if the stopwatch is running, update the active point; if not, update the displayed time
            if (_isRunning.value) {
                currentActivePoint?.elapsedTime = (currentActivePoint?.elapsedTime ?: 0L) + removed.elapsedTime
            } else {
                _elapsedTime.value = accumulatedTime
            }
            ToastHelper.showToast(this, "Last point undone", 1000L)
        } else {
            Toast.makeText(this, "No point to undo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Context.shareExcelFile(excel: File) {

        // 1. Build the content‑URI with your FileProvider authority
        val authority = "${packageName}.fileprovider"      // ← uses runtime package name
        val uri       = FileProvider.getUriForFile(this, authority, excel)

        // 2. Craft the SEND intent
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_SUBJECT, "Exported Points")
            putExtra(Intent.EXTRA_STREAM,  uri)

            // Required on API 24+ so the receiving app can actually read the URI
            clipData = ClipData.newUri(contentResolver, excel.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 3. Grant one‑time read permission to every app that can handle the intent
        packageManager.queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .forEach { res ->
                grantUriPermission(res.activityInfo.packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

        // 4. Fire the chooser
        startActivity(Intent.createChooser(sendIntent, "Share Excel file"))
    }

    private var currentTimeFormatSetting by mutableStateOf(TimeFormatSetting(useShortFormat = false))

    private fun clearOldExportsCache() {
        // use the *same* root you used when exporting
        val root = externalCacheDir ?: cacheDir          // external if it exists, else internal
        root?.resolve("exports")
            ?.listFiles()
            ?.forEach { it.delete() }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force a refresh of the time format setting (hacky workaround)
        lifecycleScope.launch {
            // Wait for the initial value from DataStore to be available
            val storedFormat = readTimeFormatSetting(this@MainActivity).first()
            // Set the global variable to this stored value
            currentTimeFormatSetting = TimeFormatSetting(useShortFormat = storedFormat)
            // Force a refresh by toggling the setting twice
            currentTimeFormatSetting = TimeFormatSetting(useShortFormat = !storedFormat)
            delay(50) // wait briefly so the change is registered
            currentTimeFormatSetting = TimeFormatSetting(useShortFormat = storedFormat)
            Log.d("DEBUG", "Forced currentTimeFormatSetting refresh on startup: ${currentTimeFormatSetting.useShortFormat}")
        }

        clearOldExportsCache()

        val activityInstance = this

        cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imagePath = result.data?.getStringExtra("imagePath")
                if (!imagePath.isNullOrEmpty()) {
                    if (activityInstance.pendingImageUpdatePoint != null) {
                        val pointToUpdate = activityInstance.pendingImageUpdatePoint!!
                        val index = _points.indexOf(pointToUpdate)
                        if (index != -1) {
                            _points[index] = pointToUpdate.copy(imagePath = "file://$imagePath")
                        }
                        activityInstance.pendingImageUpdatePoint = null
                    } else if (currentActivePoint != null) {
                        currentActivePoint!!.imagePath = "file://$imagePath"
                    } else if (_points.isNotEmpty()) {
                        _points[_points.lastIndex] = _points.last().copy(imagePath = "file://$imagePath")
                    }
                }
            }
        }

        setContent {

                // Get the context for DataStore access.
                val context = LocalContext.current

                val useDarkMode by readDarkModeSetting(context)
                    .collectAsState(initial = true)

                MyApplicationTheme(useDarkTheme = useDarkMode) {
                // Reference to the current Activity instance.
                val activityInstance = this

                // at the very top of onCreate()   (inside setContent { … })
                val showTipsOnStart by readShowTips(context).collectAsState(initial = true)
                var showTipsDialog by remember { mutableStateOf(showTipsOnStart) }
                var dontShowAgain   by remember { mutableStateOf(false) }

                // Local UI state.
                var showCommentDialog by remember { mutableStateOf(false) }
                var showImageUpdateDialog by remember { mutableStateOf(false) }

                // Define your capture image action.
                val captureImage: () -> Unit = {
                    val intent = Intent(activityInstance, CameraActivity::class.java)
                    cameraResultLauncher.launch(intent)
                }

                // DataStore settings.
                // Read the Boolean from DataStore that indicates if we should use the short format.
                val useShortFormat by readTimeFormatSetting(context).collectAsState(initial = false)
                // Create a TimeFormatSetting instance from the DataStore Boolean.
                val timeFormatSetting = TimeFormatSetting(useShortFormat = useShortFormat)
                Log.d("DEBUG", "Using TimeFormatSetting: ${timeFormatSetting.useShortFormat}")

                // Read Sheet settings and Fast Comments settings.
                val sheetSettingsFlow = readSheetSettings(context)
                val currentSheetSettings by sheetSettingsFlow.collectAsState(initial = SheetSettings())
                val fastCommentsFlow = readFastCommentsSettings(context)
                val currentFastComments by fastCommentsFlow.collectAsState(initial = FastCommentsSettings())

                // Ensure CSV folder exists.
                val csvDir = File(context.getExternalFilesDir(null), "StopwatchMetrics")
                if (!csvDir.exists()) {
                    csvDir.mkdirs()
                }

                // Regenerate CSV content whenever points, sheet settings, or the time format setting change.
                val csvContent by remember(points, currentSheetSettings, timeFormatSetting) {
                    derivedStateOf { generateCSV(points, currentSheetSettings, timeFormatSetting) }
                }
                Log.d("CSVContent", csvContent)

                // Main UI (switching between screens).
                Box(modifier = Modifier.fillMaxSize()) {

                    BackHandler {
                        when (currentScreen) {
                            Screen.Settings     -> currentScreen = Screen.Stopwatch
                            Screen.FileBrowser  -> currentScreen = Screen.Stopwatch
                            Screen.ViewFile     -> currentScreen = Screen.FileBrowser
                            // If we are already on the main stopwatch screen, let the
                            // system finish the activity (default back behaviour):
                            Screen.Stopwatch    -> finish()
                        }
                    }

                    when (currentScreen) {
                        Screen.Stopwatch -> {
                            MainScreen(
                                elapsedTime            = elapsedTime.value,
                                isRunning              = isRunning.value,
                                points = if (isRunning.value && currentActivePoint != null)
                                    points + currentActivePoint!!
                                else
                                    points,
                                currentActivePoint     = currentActivePoint,
                                timeFormatSetting      = timeFormatSetting,
                                sheetSettings          = currentSheetSettings,
                                activeDialog           = activeDialog,
                                isDirty = activityInstance.isDirty,

                                onActiveDialogChange   = { activeDialog = it },
                                onToggleStopwatch      = {
                                    toggleStopwatch()
                                    activityInstance.isDirty = true
                                },
                                onNewPoint             = {
                                    newPoint()
                                    activityInstance.isDirty = true
                                },
                                onResetRequest         = {
                                    if (activityInstance.isDirty)
                                        activeDialog = ActiveDialog.ConfirmReset
                                    else
                                        resetStopwatch()
                                },
                                onFileBrowserClick     = { currentScreen = Screen.FileBrowser },
                                onSettingsClick        = { currentScreen = Screen.Settings },
                                onPrepareSaveShare     = {
                                    pendingCsvContent = generateCSV(points, sheetSettings, currentTimeFormatSetting)
                                    activeDialog = ActiveDialog.SaveShare
                                },
                                onShare                = { sendEmail() },
                                onRenameConfirm        = { newName ->
                                    saveCsvFile(newName)
                                    isDirty = false
                                    activeDialog = ActiveDialog.None
                                },
                                onCaptureImage         = { captureImage() },
                                onAddComment           = { showCommentDialog = true },
                                onUndo                 = {
                                    undoLastPoint()
                                    activityInstance.isDirty = true
                                },

                                fastCommentsSettings   = currentFastComments,

                                onToggleImageColumn = {
                                    val new = sheetSettings.copy(showImage = !sheetSettings.showImage)
                                    sheetSettings = new
                                    CoroutineScope(Dispatchers.IO).launch {
                                        saveSheetSettings(context, new)
                                    }
                                },
                                onToggleCommentColumn = {
                                    val new = sheetSettings.copy(showComment = !sheetSettings.showComment)
                                    sheetSettings = new
                                    CoroutineScope(Dispatchers.IO).launch {
                                        saveSheetSettings(context, new)
                                    }
                                },


                                onImageClick           = { point ->
                                    pendingImageUpdatePoint = point
                                    showImageUpdateDialog = true
                                },
                                onUpdatePointImage     = { point, path ->
                                    val idx = _points.indexOf(point)
                                    if (idx != -1) {
                                        _points[idx] = point.copy(imagePath = path)
                                        isDirty = true
                                    }
                                },
                                onUpdatePointComment   = { point, comment ->
                                    val idx = _points.indexOf(point)
                                    if (idx != -1) {
                                        _points[idx] = point.copy(comment = comment)
                                        isDirty = true
                                    }
                                }
                            )
                        }
                        Screen.Settings -> {
                            SettingsScreen(
                                sheetSettings = currentSheetSettings,
                                timeFormatSetting = timeFormatSetting,
                                fastCommentsSettings = currentFastComments,
                                onSheetSettingsChange = { newSettings ->
                                    sheetSettings = newSettings
                                    // Save updated settings.
                                    CoroutineScope(Dispatchers.IO).launch {
                                        saveSheetSettings(context, newSettings)
                                    }
                                },
                                onTimeFormatChange = { newTimeFormat ->
                                    currentTimeFormatSetting = newTimeFormat  // update global state
                                    CoroutineScope(Dispatchers.IO).launch {
                                        saveTimeFormatSetting(context, newTimeFormat.useShortFormat)
                                    }
                                },
                                onFastCommentsChange = { newFastSettings ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        saveFastCommentsSettings(context, newFastSettings)
                                    }
                                },
                                onBack = { currentScreen = Screen.Stopwatch }
                            )
                        }
                        Screen.FileBrowser -> {
                            FileBrowserScreen(
                                onBack = { currentScreen = Screen.Stopwatch },
                                contextFilesDir = csvDir,
                                onFileSelected = { file ->
                                    selectedFile = file
                                    currentScreen = Screen.ViewFile
                                },
                                onDeleteAllFiles   = { deleteAllFiles() },
                                showCsv            = fbShowCsv,
                                showImages         = fbShowImages,
                                onShowCsvChange    = { fbShowCsv    = it },
                                onShowImagesChange = { fbShowImages = it }
                            )
                        }
                        Screen.ViewFile -> {
                            selectedFile?.let { file ->
                                FileViewerScreen(
                                    file = file,
                                    onBack = {
                                        currentScreen = Screen.FileBrowser
                                        selectedFile = null
                                    },
                                    sheetSettings = currentSheetSettings,
                                    onExportExcel = { exportExcelFileAndShare() }
                                )
                            }
                        }
                    } // End when

                    // Additional dialogs.
                    if (showImageUpdateDialog) {
                        ImageCaptureDialog(
                            onCapture = {
                                captureImage()
                                showImageUpdateDialog = false
                            },
                            onDismiss = {
                                showImageUpdateDialog = false
                                pendingImageUpdatePoint = null
                            }
                        )
                    }
                    if (showCommentDialog) {
                        CommentDialogUnified(
                            initialComment = "",
                            fastCommentsSettings = currentFastComments,
                            isEditingOldPoint = false,
                            onConfirm = { newComment ->
                                if (currentActivePoint != null) {
                                    currentActivePoint!!.comment = if (currentActivePoint!!.comment.isNotBlank())
                                        currentActivePoint!!.comment + ", " + newComment
                                    else newComment
                                } else if (_points.isNotEmpty()) {
                                    _points.last().comment = if (_points.last().comment.isNotBlank())
                                        _points.last().comment + ", " + newComment
                                    else newComment
                                }
                                showCommentDialog = false
                            },
                            onDismiss = { showCommentDialog = false }
                        )
                    }
                    // Confirm Reset dialog
                    if (activeDialog is ActiveDialog.ConfirmReset) {
                        AlertDialog(
                            onDismissRequest = { activeDialog = ActiveDialog.None },
                            title   = { Text("Confirm Reset") },
                            text    = { Text("Are you sure you want to reset? Unsaved data will be lost.") },
                            confirmButton = {
                                Button(onClick = {
                                    resetStopwatch()
                                    isDirty = false
                                    activeDialog = ActiveDialog.None
                                }) { Text("Reset") }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { activeDialog = ActiveDialog.None }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }


    }

    fun deleteAllFiles() {
        // Delete files in the StopwatchMetrics folder (CSV and JPEG files)
        val imagesAndCsvDir = File(getExternalFilesDir(null), "StopwatchMetrics")
        imagesAndCsvDir.listFiles()?.forEach { file ->
            if (file.exists()) {
                file.delete()
            }
        }
        // Delete exported Excel files in the main files directory
        val exportDir = getExternalFilesDir(null)
        exportDir?.listFiles()?.forEach { file ->
            // Assuming exported Excel files are .xlsx (or .csv if that's the case)
            if (file.exists() && file.isFile && (file.name.lowercase().endsWith(".xlsx") || file.name.lowercase().endsWith(".csv"))) {
                // Optionally, add further filtering if you want to leave CSV files from the StopwatchMetrics folder untouched.
                file.delete()
            }
        }
    }

    // Move these override functions OUTSIDE onCreate:

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (event?.repeatCount == 0) {
                    volumeUpPressStart = System.currentTimeMillis()
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event?.repeatCount == 0) {
                    volumeDownPressStart = System.currentTimeMillis()
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                val pressDuration = System.currentTimeMillis() - volumeUpPressStart
                if (pressDuration >= 500) {
                    pendingCsvContent = generateCSV(points, sheetSettings, currentTimeFormatSetting)
                    Log.d("CSVContent", pendingCsvContent ?: "null")
                    activeDialog = ActiveDialog.SaveShare
                } else {
                    toggleStopwatch()
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val pressDuration = System.currentTimeMillis() - volumeDownPressStart

                if (pressDuration >= 500) {      // ── long-press ──
                    if (isDirty) {
                        activeDialog = ActiveDialog.ConfirmReset   // ask first
                    } else {
                        resetStopwatch()                           // nothing to lose
                    }
                } else {                           // ── short-press ──
                    newPoint()                     // add a point
                    isDirty = true                 // mark sheet as changed
                }
                return true
            }

        }
        return super.onKeyUp(keyCode, event)
    }






    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // Get the public Pictures directory.
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        // Create a dedicated subfolder for your app's images.
        val appImagesDir = File(picturesDir, "StopwatchMetrics")
        if (!appImagesDir.exists()) {
            appImagesDir.mkdirs()
        }
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", appImagesDir)
    }

    private fun toggleStopwatch() {
        if (!_isRunning.value) {
            _isRunning.value = true
            if (accumulatedTime == 0L) {
                currentPointStartTime = System.currentTimeMillis()
                currentActivePoint = PointData(elapsedTime = 0L, pointStartTime = currentPointStartTime)
            }
            startTime = System.currentTimeMillis()
            updateElapsedTime()
        } else {
            _isRunning.value = false
            accumulatedTime += System.currentTimeMillis() - startTime
        }
    }

    private fun newPoint() {
        // If no time has elapsed yet, treat this as starting a new cycle without recording a zero point.
        if (_elapsedTime.value == 0L) {
            if (!_isRunning.value) {
                toggleStopwatch() // Start the timer fresh
            }
            return
        }

        // Otherwise, when there is a nonzero elapsed time:
        // Record the current active point.
        currentActivePoint?.let { _points.add(it) }

        // Reset timing variables for a new measurement cycle.
        startTime = System.currentTimeMillis()
        accumulatedTime = 0L
        _elapsedTime.value = 0L

        // Create a new active point starting from 0.
        currentActivePoint = PointData(
            elapsedTime = 0L,
            pointStartTime = startTime
        )

        // If paused, start the stopwatch for the new cycle.
        if (!_isRunning.value) {
            toggleStopwatch()
        }
    }



    private fun newCycleNoClear() {
        // Reset timing variables for a new cycle.
        startTime = System.currentTimeMillis()
        accumulatedTime = 0L
        _elapsedTime.value = 0L

        // Create a new active point starting at the new start time.
        currentActivePoint = PointData(
            elapsedTime = 0L,
            pointStartTime = startTime
        )

        // If the stopwatch is paused, start it.
        if (!_isRunning.value) {
            toggleStopwatch()
        }
    }

    private fun resetStopwatch() {
        _isRunning.value = false
        accumulatedTime = 0L
        _elapsedTime.value = 0L
        _points.clear()
        currentActivePoint = null
    }

    private fun updateElapsedTime() {
        lifecycleScope.launch {
            while (_isRunning.value) {
                val now = System.currentTimeMillis()
                _elapsedTime.value = accumulatedTime + (now - startTime)
                currentActivePoint?.elapsedTime = _elapsedTime.value
                delay(100)
            }
        }
    }

    private fun exportExcelFileAndShare() {
        val exportedFile = exportExcelFile(
            context             = this,
            points              = points,
            settings            = sheetSettings,
            timeFormatSetting   = currentTimeFormatSetting
        )

        // optional: keep or drop the Toast
        Toast.makeText(
            this,
            "Excel file generated … sharing now",
            Toast.LENGTH_SHORT
        ).show()

        // NEW helper (extension on Context)
        shareExcelFile(exportedFile)
    }
    private fun sendEmail() {
        val csvContent = generateCSV(points, sheetSettings, currentTimeFormatSetting)
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Recorded Time Measurements")
            putExtra(Intent.EXTRA_TEXT, csvContent)
        }
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }
}
