@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.stopwatchmetrics

//import androidx.compose.ui.tooling.preview.Preview

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.example.stopwatchmetrics.ui.theme.MyApplicationTheme
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import androidx.camera.core.Preview as CameraXPreview


// --- Helper Functions & Data Classes ---

@SuppressLint("DefaultLocale")
fun formatTime(timeMs: Long, timeFormatSetting: TimeFormatSetting = TimeFormatSetting()): String {
    val totalSeconds   = timeMs / 1000         // whole seconds
    val hundredths     = (timeMs % 1000) / 10  // 0-99

    return if (timeFormatSetting.useShortFormat) {
        // SHORT = always seconds.hundredths
        String.format("%d.%02d", totalSeconds, hundredths)
    } else {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        if (minutes == 0L)
            String.format("%d.%02d", seconds, hundredths) // 57.74
        else
            String.format("%d:%02d.%02d", minutes, seconds, hundredths) // 1:24.05
    }
}

fun formatEventInTime(timeMs: Long): String {
    val date = Date(timeMs)
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
}



/** Tracks which step index we’re currently on, for a loaded PresetCycle. */
data class ActiveCycle(
    val preset: PresetCycle,
    var currentIndex: Int = 0
)

data class EventData(
    var elapsedTime: Long,
    val eventStartTime: Long,
    var comment: String = "",
    var imagePath: String? = null,
    val cycleNumber   : Int = 1
)


fun generateCSV(
    events: List<EventData>,
    settings: SheetSettings,
    timeFormatSetting: TimeFormatSetting
): String {

    /* ── 1 . add the header ────────────────────────────────────────── */
    val allHeaders = listOf(
        "Event",
        "Time",
        "TMU",
        "Start Time",
        "Comment",
        "Image",
        "Cycle"
    )

    /* ── 2 . respect the toggle in Settings ────────────────────────── */
    val enabledHeaders = allHeaders.filter { header ->
        when (header) {
            "Event"       -> settings.showEvent
            "Time"        -> settings.showTime
            "TMU"         -> settings.showTMU
            "Start Time"  -> settings.showStartTime
            "Comment"     -> settings.showComment
            "Image"       -> settings.showImage
            "Cycle"       -> settings.showCycle        // ← NEW
            else          -> true
        }
    }
    val headerLine = enabledHeaders.joinToString(",")

    /* ── 3 . build each row ────────────────────────────────────────── */
    val rows = events.mapIndexed { index, event ->
        val formattedTime = formatTime(event.elapsedTime, timeFormatSetting)

        val allCells = listOf(

            "#${index + 1}",                       // Event
            formattedTime,                         // Time
            "${(event.elapsedTime / 36).toInt()}", // TMU
            formatEventInTime(event.eventStartTime), // Start Time
            event.comment,                         // Comment
            event.imagePath ?: "",                 // Image
            "${event.cycleNumber}",                 // NEW  (index 0 == “Cycle”)
        )

        /* keep only the enabled columns */
        allHeaders.mapIndexedNotNull { i, header ->
            if (header in enabledHeaders) allCells[i] else null
        }.joinToString(",")
    }

    return buildString {
        appendLine(headerLine)
        append(rows.joinToString("\n"))
    }
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



// Function to export events as an XLSX file with embedded images.
fun exportExcelFile(
    context: Context,
    events: List<EventData>,
    settings: SheetSettings,
    timeFormatSetting: TimeFormatSetting,   // Added parameter
    presetName: String? = null               // <- NEW
): File {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Events")
    var colIndex = 0

    // Create header row.
    val headerRow = sheet.createRow(0)

    if (settings.showEvent) headerRow.createCell(colIndex++).setCellValue("Event")
    if (settings.showTime) headerRow.createCell(colIndex++).setCellValue("Time")
    if (settings.showTMU) headerRow.createCell(colIndex++).setCellValue("TMU")
    if (settings.showStartTime) headerRow.createCell(colIndex++).setCellValue("Start Time")
    if (settings.showComment) headerRow.createCell(colIndex++).setCellValue("Comment")
    if (settings.showImage) headerRow.createCell(colIndex++).setCellValue("Image")
    if (settings.showCycle) headerRow.createCell(colIndex++).setCellValue("Cycle")


    // Create the drawing patriarch to hold images.
    val drawing = sheet.createDrawingPatriarch()

    // Populate data rows.
    events.forEachIndexed { index, event ->
        val row = sheet.createRow(index + 1)
        var currentCol = 0
        if (settings.showEvent) {
            row.createCell(currentCol++).setCellValue((index + 1).toDouble())
        }
        if (settings.showTime) {
            val cell = row.createCell(currentCol++)
            val dataFormat = workbook.createDataFormat()
            val cellStyle = workbook.createCellStyle()

            if (timeFormatSetting.useShortFormat) {
                // Short format: display total seconds with two decimals.
                // Convert milliseconds to seconds.
                val secondsValue = event.elapsedTime.toDouble() / 1000
                cell.setCellValue(secondsValue)
                // Format as a plain number, e.g., 75.32
                cellStyle.dataFormat = dataFormat.getFormat("0.00")
            } else {
                // Long format: display as mm:ss.00.
                // For Excel time format, convert milliseconds into fraction of a day.
                val excelTime = event.elapsedTime.toDouble() / (1000 * 24 * 3600)
                cell.setCellValue(excelTime)
                // Format the cell as time – note that Excel interprets 1 as one full day.
                cellStyle.dataFormat = dataFormat.getFormat("mm:ss.00")
            }
            cell.cellStyle = cellStyle
        }
        if (settings.showTMU) {
            row.createCell(currentCol++).setCellValue((event.elapsedTime / 36).toDouble())
        }
        if (settings.showStartTime) {
            row.createCell(currentCol++).setCellValue(formatEventInTime(event.eventStartTime))
        }
        if (settings.showComment) {
            row.createCell(currentCol++).setCellValue(event.comment)
        }
        if (settings.showImage) {
            val localImagePath = event.imagePath
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
        if (settings.showCycle) {
            row.createCell(currentCol++).setCellValue(event.cycleNumber.toDouble())
        }
    }

    // Create the file name using the same date-time format as the CSV file.
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }

    val timeStamp  = SimpleDateFormat("yyyy-MM-dd, HH:mm", Locale.getDefault()).format(Date())

    val safePreset = presetName
        ?.takeIf { it.isNotBlank() }
        ?.replace(Regex("""[\\/:*?"<>|]"""), "_")   // scrub illegal filename chars
        ?.let   { " - $it" }                       // " - Elevator Up"
        ?: ""                                      // nothing if null/blank

    val fileName   = "exported_events_${timeStamp}$safePreset.xlsx"
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
                    //Toast.makeText(this@CameraActivity, "Image captured!", Toast.LENGTH_SHORT).show()

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
fun EventProgressIndicator(
    elapsedTime: Long,               // current elapsed time in ms
    events: List<EventData>,         // your list of recorded events
    modifier: Modifier = Modifier,   // ← external size will be applied here
    centerCircleRadius: Dp = 3.dp,
    eventerLengthFraction: Float = 0.8f
) {
    val colorScheme = MaterialTheme.colorScheme

    // fallback to 60 s if no events
    val maxEventTime = events.maxOfOrNull { it.elapsedTime } ?: 60000L
    val progress     = if (maxEventTime > 0) min(elapsedTime / maxEventTime.toFloat(), 1f) else 0f

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

        // rotating eventer
        if (eventerLengthFraction > 0f) {
            val angleDeg = -90f + 360f * (elapsedTime / maxEventTime.toFloat())
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val length   = (size.minDimension / 2) * eventerLengthFraction
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

        // event ticks
        events.forEach { event ->
            val frac     = (event.elapsedTime / maxEventTime.toFloat()).coerceAtMost(1f)
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

@OptIn(ExperimentalTextApi::class)
@Composable
fun EventTable(
    events: List<EventData>,
    currentActiveEvent: EventData?,
    timeFormatSetting: TimeFormatSetting,
    sheetSettings: SheetSettings,
    hasPresetLoaded: Boolean,
    onCommentClick: (EventData) -> Unit,
    onImageClick: (EventData) -> Unit,
    onAddCommentForLive: () -> Unit,
    onCaptureImageForLive: () -> Unit
) {
    /* ───── decide visible columns ───── */
    val columns = buildList {
        if (sheetSettings.showEvent   ) add("Event")
        if (sheetSettings.showTime    ) add("Time")
        if (sheetSettings.showTMU     ) add("TMU")
        if (sheetSettings.showStartTime) add("Start Time")
        if (sheetSettings.showComment ) add("Comment")
        if (sheetSettings.showImage   ) add("Image")
        if (sheetSettings.showCycle && hasPresetLoaded) add("Cycle")
    }

    val density      = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    /* ───── PASS ① : measure natural width of every column (header + data) ───── */
    val naturalPx: List<Int> = remember(
        events,                // already there
        sheetSettings,         // already there
        hasPresetLoaded        //  ← NEW
    ) {
        val px = IntArray(columns.size) { 0 }

        fun update(colIdx: Int, text: String) {
            if (px[colIdx] >= 600) return
            val w = textMeasurer
                .measure(AnnotatedString(text))
                .size.width
            px[colIdx] = max(px[colIdx], w)
        }

        // headers
        columns.forEachIndexed { i, h -> update(i, h) }

        // rows
        events.forEachIndexed { rowIdx, event ->
            var col = 0
            if (sheetSettings.showEvent   ) update(col++, "#${rowIdx + 1}")
            if (sheetSettings.showTime    ) update(col++, formatTime(event.elapsedTime, timeFormatSetting))
            if (sheetSettings.showTMU     ) update(col++, "${(event.elapsedTime / 36).toInt()}")
            if (sheetSettings.showStartTime) update(col++, formatEventInTime(event.eventStartTime))
            if (sheetSettings.showComment ) update(col++, event.comment.ifBlank { "—" })
            if (sheetSettings.showImage   ) update(col++, if (event.imagePath.isNullOrBlank()) "—" else "img")
            if (sheetSettings.showCycle && hasPresetLoaded) update(col++, "${event.cycleNumber}")
        }

        px.toList()
    }

    /* convert to dp once */
    val naturalDp = remember(naturalPx) {
        naturalPx.map { with(density) { it.toDp() } }
    }

    Column(Modifier.fillMaxWidth()) {

        /* ───── header row ───── */
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Start      // ← leave space distribution to weights
        ) {
            columns.forEachIndexed { i, title ->
                Text(
                    text = title,
                    modifier = Modifier
                        .widthIn(min = naturalDp[i])        // never shrink below natural width
                        .weight(naturalDp[i].value, fill = true),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }


        Divider()

        /* ───── data rows ───── */
        LazyColumn {
            itemsIndexed(events.reversed()) { realIdx, event ->

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    var col = 0     // tracks visible-column index

                    @Composable
                    fun cell(text: String, onClick: (() -> Unit)? = null) {
                        val base = Modifier
                            .widthIn(min = naturalDp[col])              // never shrink below natural size
                            .weight(naturalDp[col].value, fill = true)  // but share the surplus space

                        val mod  = onClick?.let { base.clickable(onClick = it) } ?: base

                        Text(text, modifier = mod, textAlign = TextAlign.Center)
                        col++
                    }

                    if (sheetSettings.showEvent)
                        cell("#${events.size - realIdx}")
                    if (sheetSettings.showTime)
                        cell(formatTime(event.elapsedTime, timeFormatSetting))
                    if (sheetSettings.showTMU)
                        cell("${(event.elapsedTime / 36).toInt()}")
                    if (sheetSettings.showStartTime)
                        cell(formatEventInTime(event.eventStartTime))
                    if (sheetSettings.showComment)
                        cell(
                            if (event.comment.isNotBlank()) event.comment else "—",
                            onClick = {
                                if (event.eventStartTime == currentActiveEvent?.eventStartTime)
                                    onAddCommentForLive()
                                else
                                    onCommentClick(event)
                            }
                        )
                    if (sheetSettings.showImage) {
                        val imgMod = Modifier
                            .widthIn(min = naturalDp[col])
                            .weight(naturalDp[col].value, fill = true)   // ★ keep Image cells in the same grid
                            .padding(horizontal = 4.dp)
                            .clickable {
                                if (event.eventStartTime == currentActiveEvent?.eventStartTime)
                                    onCaptureImageForLive()
                                else
                                    onImageClick(event)
                            }

                        Box(imgMod, contentAlignment = Alignment.Center) {
                            if (!event.imagePath.isNullOrBlank())
                                Image(
                                    painter = rememberAsyncImagePainter(event.imagePath),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            else
                                Text("—")
                        }
                        col++
                    }
                    if (sheetSettings.showCycle && hasPresetLoaded)
                        cell("${event.cycleNumber}")
                }
            }
        }
    }
}



@Composable
fun GraphPreview(
    events: List<EventData>,
    modifier: Modifier = Modifier
) {
    // grab your color in a composable context:
    val lineColor = MaterialTheme.colorScheme.onBackground

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val numEvents = events.size

        if (numEvents > 1) {
            val maxTime = events.maxOfOrNull { it.elapsedTime } ?: 1L
            val xStep   = width / (numEvents - 1)

            val path = Path().apply {
                moveTo(0f, height - (events[0].elapsedTime.toFloat() / maxTime) * height)
                for (i in 1 until numEvents) {
                    val x = i * xStep
                    val y = height - (events[i].elapsedTime.toFloat() / maxTime) * height
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
fun PresetExportPickerDialog(
    all: List<PresetCycle>,
    onDismiss: () -> Unit,
    onExport: (List<PresetCycle>) -> Unit
) {
    /* ---- state ---- */
    val exportAll = remember { mutableStateOf(true) }

    // a State<List<String>> that automatically re‑composes on add/remove
    val selected = remember {
        mutableStateListOf<String>().apply { addAll(all.map { it.name }) }
    }

    /* ---- helpers ---- */
    fun toggleItem(name: String) {
        if (selected.contains(name)) selected.remove(name) else selected.add(name)
        exportAll.value = selected.size == all.size        // keep master switch in sync
    }

    /* ---- UI ---- */
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Presets") },

        text = {
            Column(Modifier.heightIn(max = 300.dp)) {

                /* ── master checkbox ── */
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = exportAll.value,
                        onCheckedChange = { checked ->
                            exportAll.value = checked
                            selected.apply {
                                clear()
                                if (checked) addAll(all.map { it.name })
                            }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Export all presets")
                }

                Spacer(Modifier.height(8.dp))

                /* ── list ── */
                LazyColumn {
                    items(all) { p ->
                        val isChecked = selected.contains(p.name)

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { toggleItem(p.name) }
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { chk ->
                                    // avoid double‑toggle when the row itself is clicked
                                    if (chk != isChecked) toggleItem(p.name)
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(p.name)
                        }
                    }
                }
            }
        },

        /* ── buttons ── */
        confirmButton = {
            Button(
                enabled = selected.isNotEmpty(),
                onClick = {
                    onExport(all.filter { selected.contains(it.name) })
                    onDismiss()
                }
            ) { Text("Export") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}



@Composable
fun PresetCycleEditDialog(
    original: PresetCycle,
    existingNames: List<String>,
    onConfirm: (PresetCycle) -> Unit,
    onDismiss: () -> Unit
) {
    var presetName by remember { mutableStateOf(original.name) }
    val steps      = remember {
        mutableStateListOf<String>().apply { addAll(original.steps) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Preset") },

        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp, max = 500.dp)
            ) {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("Preset Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = presetName.isBlank() ||
                            (presetName != original.name &&
                                    existingNames.contains(presetName)),
                    supportingText = {
                        when {
                            presetName.isBlank() ->
                                Text("Name cannot be empty", color = Color.Red)
                            presetName != original.name &&
                                    existingNames.contains(presetName) ->
                                Text("A preset with that name already exists", color = Color.Red)
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))
                Text("Steps:", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    items(steps.size) { idx ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = steps[idx],
                                onValueChange = { steps[idx] = it },
                                label = { Text("Step ${idx + 1}") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                if (steps.size > 1) steps.removeAt(idx)
                                else steps[idx] = ""
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove step")
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { steps.add("") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Step")
                }
            }
        },

        confirmButton = {
            Button(
                enabled = presetName.trim().isNotBlank() &&
                        (presetName == original.name ||
                                !existingNames.contains(presetName.trim())) &&
                        steps.all { it.trim().isNotBlank() },
                onClick = {
                    val cleanedSteps = steps.map { it.trim() }
                    onConfirm(
                        PresetCycle(
                            name  = presetName.trim(),
                            steps = cleanedSteps
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun NewCycleTile(
    activeCycle : ActiveCycle?,          // null → tile is disabled/greyed-out
    cycleNumber : Int,                   // ← NEW
    onNewCycle  : () -> Unit,
    modifier    : Modifier = Modifier,
    ringSize    : Dp = 36.dp
) {
    /* progress maths (unchanged) */
    val total    = activeCycle?.preset?.steps?.size ?: 0
    val index    = activeCycle?.currentIndex ?: 0
    val progress by animateFloatAsState(
        if (total == 0) 0f else index / total.toFloat(),
        label = "cycle-progress"
    )
    val done     = total > 0 && index >= total

    /* colours (unchanged) */
    val colors     = MaterialTheme.colorScheme
    val container  = if (done) colors.onBackground else colors.background
    val contentCol = if (done) colors.background     else colors.onBackground

    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(12.dp))
            .background(container)
            .clickable(enabled = activeCycle != null) { onNewCycle() }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* ── ring + centred cycle number ───────────────────────── */
        Box(Modifier.size(ringSize), contentAlignment = Alignment.Center) {

            Canvas(Modifier.matchParentSize()) {
                val stroke = 5.dp.toPx()

                drawArc(
                    color      = contentCol.copy(alpha = .25f),       // full ring
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter  = false,
                    style      = Stroke(stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color      = contentCol,                          // progress segment
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter  = false,
                    style      = Stroke(stroke, cap = StrokeCap.Round)
                )
            }

            /* the new **cycle number** */
            Text(
                text   = "$cycleNumber",
                style  = MaterialTheme.typography.labelLarge,
                color  = contentCol
            )
        }

        Spacer(Modifier.height(6.dp))

        Text("Tap: New Cycle ($index/$total)",    //  ← moved progress here
                        color = contentCol, style = MaterialTheme.typography.labelSmall)
        val presetName = activeCycle?.preset?.name ?: "—"
        Text("Preset: $presetName",   color = contentCol, style = MaterialTheme.typography.labelSmall)
        val nextStep   = activeCycle?.preset?.steps?.getOrNull(index) ?: "—"
                Text("Next: $nextStep",                       //  ← progress removed from here
            color = contentCol,
            style = MaterialTheme.typography.labelSmall)
    }
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
            "Event"       -> if (sheetSettings.showEvent)       index else null
            "Time"        -> if (sheetSettings.showTime)        index else null
            "TMU"         -> if (sheetSettings.showTMU)         index else null
            "Start Time"  -> if (sheetSettings.showStartTime)   index else null
            "Comment"     -> if (sheetSettings.showComment)     index else null
            "Image"       -> if (sheetSettings.showImage)       index else null
            "Cycle"       -> if (sheetSettings.showCycle)       index else null   // ← add this
            else          -> index        // keep any unknown column
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
            Text("Toggle columns in your Event Table:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            SettingsRow(
                label = "Show Event",
                checked = sheetSettings.showEvent,
                onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showEvent = it)) }
            )
            SettingsRow(
                label = "Show Time",
                checked = sheetSettings.showTime,
                onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showTime = it)) }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp)
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
                label = "Show Image",
                checked = sheetSettings.showImage,
                onCheckedChange = { onSheetSettingsChange(sheetSettings.copy(showImage = it)) }
            )
            SettingsRow(
                label = "Show Cycle if preset is loaded",
                checked = sheetSettings.showCycle,
                onCheckedChange = { checked ->
                    val new = sheetSettings.copy(showCycle = checked)
                    onSheetSettingsChange(new)
                    CoroutineScope(Dispatchers.IO).launch { saveSheetSettings(context, new) }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(
                color = MaterialTheme.colorScheme.onBackground,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Other Settings:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                label = "Show times as seconds only (ss.00)",
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

            Spacer(Modifier.height(8.dp))

            SettingsRow(
                label   = "Enable Fast Comments",
                checked = fastCommentsSettings.enabled,
                onCheckedChange = { checked ->
                    val updated = fastCommentsSettings.copy(enabled = checked)
                    onFastCommentsChange(updated)                  // update UI
                    CoroutineScope(Dispatchers.IO).launch {
                        saveFastCommentsSettings(context, updated) // persist
                    }
                }
            )

            // EDIT FAST COMMENTS
            OutlinedButton(
                onClick = { showFastCommentsEditDialog = true },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor   = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text("Edit Fast Comments")
            }

            Spacer(Modifier.height(32.dp))


// BUY A COFFEE
            OutlinedButton(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://buymeacoffee.com/elias_svensson_apps")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor   = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Icon(
                    Icons.Default.LocalCafe,
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
    // 1) Back your UI with a MutableStateList
    val comments = remember {
        mutableStateListOf<String>().apply {
            addAll(initialFastComments.comments)
        }
    }

    // 2) Create a LazyListState to control scrolling
    val listState = rememberLazyListState()

    // 3) Whenever comments.size changes, scroll to the bottom
    LaunchedEffect(comments.size) {
        if (comments.isNotEmpty()) {
            listState.animateScrollToItem(comments.lastIndex)
        }
    }

    AlertDialog(
        // 4) Make the dialog wider / taller
        modifier = Modifier
            .fillMaxWidth(1f)
            .heightIn(min = 400.dp, max = 600.dp),

        onDismissRequest = onDismiss,
        title = { Text("Edit Fast Comments") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // give the column enough height so the list scrolls
                    .heightIn(min = 400.dp, max = 600.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(comments) { commentText ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { new ->
                                    val idx = comments.indexOf(commentText)
                                    if (idx >= 0) comments[idx] = new
                                },
                                placeholder = {
                                    Text("Comment ${comments.indexOf(commentText) + 1}")
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            )
                            IconButton(onClick = {
                                if (comments.size > 1) comments.remove(commentText)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove comment")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { comments += "" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(Modifier.width(4.dp))
                    Text("Add comment")
                }
            }
        },

        confirmButton = {
            Button(onClick = {
                val updated = initialFastComments.copy(comments = comments.toList())
                onConfirm(updated)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
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
    isEditingOldEvent: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    onEditFastComments: () -> Unit
) {
    var comment by remember { mutableStateOf(initialComment) }
    val context = LocalContext.current

    AlertDialog(
        modifier = Modifier
            .fillMaxWidth(1f)
            .heightIn(min = 400.dp, max = 600.dp),
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fast Comments",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onEditFastComments,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Edit fast comments"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp
                    ) {
                        // ← use the dynamic list
                        fastCommentsSettings.comments.forEach { fastComment ->
                            Button(
                                onClick = {
                                    onConfirm(fastComment)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor   = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(fastComment)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                Button(onClick = { onConfirm("") }) { Text("Clear") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onConfirm(comment) }) { Text("Save") }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PresetActionDialog(
    cycle: PresetCycle,
    onLoad  : () -> Unit,
    onEdit  : () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(cycle.name) },
        text  = { Text("What would you like to do with this preset?") },

        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onLoad();  onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Load") }

                Button(
                    onClick = { onEdit();  /* keep dialog open */ },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Edit") }

                OutlinedButton(
                    onClick  = { onDelete() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete") }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel") }
            }
        },
        dismissButton = {}     // nothing down here – everything’s in confirmButton
    )
}


@Composable
fun PresetCycleListDialog(
    allCycles   : List<PresetCycle>,
    activeCycle : ActiveCycle?,
    onDismiss   : () -> Unit,
    onLoad      : (PresetCycle) -> Unit,
    onEdit      : (PresetCycle) -> Unit,
    onDelete    : (PresetCycle) -> Unit,
    onCreateNew : () -> Unit,
    onUnload    : () -> Unit,
    onImport    : () -> Unit,
    onExport    : () -> Unit,
) {
    /* ── local dialog state ─────────────────────────────────────────── */
    var confirmDeleteOf by remember { mutableStateOf<PresetCycle?>(null) }
    var actionForCycle by remember { mutableStateOf<PresetCycle?>(null) }

    /* ── tiny helpers for the 3‑button rows ─────────────────────────── */


    @Composable
    fun BottomBtn(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) = OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,   // ≈12 sp
            maxLines = 1,
            overflow = TextOverflow.Ellipsis                   // never wraps
        )
    }

    /* ───────────────────────────────────────────────────────────────── */

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Preset Cycles") },

        /* ──────────────── MAIN BODY ──────────────── */
        text = {
            Box(                                            // outer frame caps size
                Modifier
                    .widthIn(max = 420.dp)
                    .heightIn(max = 520.dp)
            ) {
                Column(                                     // inner column = list + 2 buttons
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 440.dp)
                ) {

                    /* ─── scrolling list ─── */
                    Box(Modifier.weight(1f)) {
                        if (allCycles.isEmpty()) {
                            Text("No presets saved yet.")
                        } else {
                            LazyColumn {
                                items(allCycles) { cycle ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { actionForCycle = cycle }   // <── only this
                                            .padding(vertical = 12.dp, horizontal = 8.dp)
                                    ) {
                                        Text(cycle.name, style = MaterialTheme.typography.bodyLarge)
                                    }
                                    Divider()
                                }
                            }
                        }
                    }

                    /* ─── always‑visible buttons under the list ─── */

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick  = { onCreateNew(); onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add New Preset")
                    }

                    if (activeCycle != null) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick  = { onUnload(); onDismiss() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Unload Preset") }
                    }
                }
            }
        },

        /* ──────────────── BOTTOM ROW ──────────────── */
        confirmButton = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BottomBtn("Import",  onClick = onImport,  Modifier.weight(1f))
                BottomBtn("Export",  onClick = onExport,  Modifier.weight(1f))
                BottomBtn("Cancel",  onClick = onDismiss, Modifier.weight(1f))
            }
        },
        dismissButton = {}
    )

    /* inside PresetCycleListDialog, *after* the AlertDialog block */

    actionForCycle?.let { chosen ->
        var confirmDelete by remember { mutableStateOf(false) }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title   = { Text("Delete Preset") },
                text    = { Text("Are you sure you want to delete “${chosen.name}”?") },
                confirmButton = {
                    Button(onClick = {
                        onDelete(chosen)
                        confirmDelete = false
                        actionForCycle = null          // close both dialogs
                    }) { Text("Delete") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { confirmDelete = false }) {
                        Text("Cancel")
                    }
                }
            )
        } else {
            PresetActionDialog(
                cycle     = chosen,
                onLoad    = { onLoad(chosen) },
                onEdit    = { onEdit(chosen) },
                onDelete  = { confirmDelete = true },
                onDismiss = { actionForCycle = null }
            )
        }
    }


    /* ───────── confirmation pop‑up for deletion ───────── */
    confirmDeleteOf?.let { doomed ->
        AlertDialog(
            onDismissRequest = { confirmDeleteOf = null },
            title   = { Text("Delete Preset") },
            text    = { Text("Are you sure you want to delete \"${doomed.name}\"?") },
            confirmButton = {
                Button(onClick = {
                    onDelete(doomed)
                    confirmDeleteOf = null
                }) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDeleteOf = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

//NBSP
// 

@Composable
fun NewPresetDialog(
    existingNames: List<String>,
    onDismiss: () -> Unit,
    onSave: (PresetCycle) -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    val steps       = remember { mutableStateListOf<String>() }
    if (steps.isEmpty()) steps.add("")          // start with one field

    AlertDialog(
        // give the whole sheet a generous max-height
        modifier = Modifier
            .fillMaxWidth(1f)
            .heightIn(min = 300.dp, max = 600.dp),

        onDismissRequest = onDismiss,
        title = { Text("Create New Preset") },

        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp, max = 600.dp)
            ) {
                /* ── name ───────────────────────────────────────── */
                OutlinedTextField(
                    value         = presetName,
                    onValueChange = { presetName = it },
                    label         = { Text("Preset Name") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Text("Steps:", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))

                /* ── list now scrolls ──────────────────────────── */
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)               // <—— key line
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(steps.size) { index ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = steps[index],
                                onValueChange = { steps[index] = it },
                                singleLine = true,
                                label = { Text("Step ${index + 1}") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                if (steps.size > 1) steps.removeAt(index)
                                else               steps[0] = ""
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove step")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick  = { steps.add("") },   // now you can add as many as you like
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Step")
                }
            }
        },

        confirmButton = {
            Button(
                enabled = presetName.trim().isNotBlank()
                        && steps.all { it.trim().isNotBlank() }
                        && presetName !in existingNames,
                onClick = {
                    onSave(
                        PresetCycle(
                            name  = presetName.trim(),
                            steps = steps.map { it.trim() }
                        )
                    )
                }
            ) { Text("Save Preset") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
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
    events: List<EventData>,
    currentActiveEvent: EventData?,
    timeFormatSetting: TimeFormatSetting,
    sheetSettings: SheetSettings,
    activeDialog: ActiveDialog,
    onActiveDialogChange: (ActiveDialog) -> Unit,
    onToggleStopwatch: () -> Unit,
    onNewEvent: () -> Unit,
    isDirty: Boolean,
    onResetRequest: () -> Unit,
    onPrepareSaveShare: () -> Unit,
    onFileBrowserClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShare: () -> Unit,
    onRenameConfirm: (String) -> Unit,
    onCaptureImage: () -> Unit,
    onAddComment: () -> Unit,
    onEditFastComments: () -> Unit,
    onUndo: () -> Unit,
    fastCommentsSettings: FastCommentsSettings,
    onImageClick: (EventData) -> Unit,
    onUpdateEventImage: (EventData, String) -> Unit,
    onUpdateEventComment: (EventData, String) -> Unit,
    onToggleImageColumn: () -> Unit,
    onToggleCommentColumn: () -> Unit,
    activeCycle: ActiveCycle?,
    onActiveCycleChange: (ActiveCycle?) -> Unit,
    onLoadCycle: (PresetCycle) -> Unit,
    onSaveAllCycles: (List<PresetCycle>) -> Unit,
    allPresetCycles: List<PresetCycle>,
    onClearPreset: () -> Unit,
    onConfigurePresets: () -> Unit,
    showPresetDialog : Boolean,
    onShowPresetDialogChange: (Boolean) -> Unit,
    cycleNumber: Int,
    onCycleIncrement: () -> Unit,

    ) {

    val context = LocalContext.current

    val scope   = rememberCoroutineScope()

    var showTipsDialog by remember { mutableStateOf(false) }     // UI flag
    val showTipsPref  by readShowTips(context)
        .collectAsState(initial = false)                    // persisted flag

    /* first app launch => DataStore emits `true` once, we open the dialog */
    LaunchedEffect(showTipsPref) {
        if (showTipsPref) showTipsDialog = true
    }

    var selectedEventForComment by remember { mutableStateOf<EventData?>(null) }
    var selectedEventForImage by remember { mutableStateOf<EventData?>(null) }
    var currentPage by remember { mutableStateOf(1) }    // start in middle

    val maxTime      = events.maxOfOrNull { it.elapsedTime } ?: 60000L
    val medianTime   = calculateMedian(events.map { it.elapsedTime })
    val stdDevTime   = calculateStdDev(events.map { it.elapsedTime })
    val averageTime  = events.map { it.elapsedTime }.average().toLong()
    val minTime   = events.minOfOrNull { it.elapsedTime } ?: 0L
    val rangeTime = maxTime - minTime         // max − min, always ≥ 0


    val displayEvents by remember(events, currentActiveEvent?.eventStartTime, currentActiveEvent?.elapsedTime, isRunning) {
        derivedStateOf {
            currentActiveEvent?.let { live ->
                if (events.none { it.eventStartTime == live.eventStartTime } &&
                    (live.elapsedTime > 0L || !isRunning)
                ) {
                    events + live
                } else events
            } ?: events
        }
    }

    // only rebuild when events or the elapsed‐time of the live event changes
    val allEvents = buildList<EventData> {
        addAll(events)
        currentActiveEvent?.takeIf { live ->
            // only include the live event if it’s not already in events
            events.none { it.eventStartTime == live.eventStartTime } &&
                    // and either some time has elapsed or we’re paused
                    (live.elapsedTime > 0L || !isRunning)
        }?.let { add(it) }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment   = Alignment.CenterVertically
                    ) {
                        Text("Stopwatch Metrics")

                        IconButton(onClick = { showTipsDialog = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Tips")
                        }
                        IconButton(onClick = onFileBrowserClick) {
                            Icon(Icons.Filled.FolderOpen, contentDescription = "Saved")
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
                    Text("Undo last event", style = MaterialTheme.typography.bodyMedium)
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
                                EventProgressIndicator(
                                    elapsedTime           = elapsedTime,
                                    events                = displayEvents,
                                    centerCircleRadius    = 0.dp,
                                    eventerLengthFraction = 0f,
                                    modifier              = Modifier.matchParentSize()
                                )

                                val displayCount = displayEvents.size
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
                                    tapInstruction  = "New Event",
                                    holdInstruction = "Reset",
                                    onTap           = onNewEvent,
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

                    // ─── PAGE 1: PPI + timer + play/reset row + camera/comment row + event‑table ───

                    {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(Modifier.height(16.dp))

                                /* ───────────── TOP ROW ───────────── */
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    if (activeCycle != null) {
                                        /* ── TILE present: two equal columns ─────────── */

                                        // ①  New‑Cycle tile
                                        Box(Modifier.weight(1f)) {
                                            NewCycleTile(
                                                activeCycle  = activeCycle,
                                                cycleNumber  = cycleNumber,    // ← pass in
                                                onNewCycle   = {
                                                    onActiveCycleChange(activeCycle.copy(currentIndex = 0))
                                                    onCycleIncrement()         // bump number in Activity
                                                },
                                                modifier     = Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 8.dp)
                                            )
                                        }

                                        // ②  Dial gets the other half
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(Modifier.size(120.dp)) {
                                                EventProgressIndicator(
                                                    elapsedTime           = elapsedTime,
                                                    events                = displayEvents,
                                                    centerCircleRadius    = 3.dp,
                                                    eventerLengthFraction = 0.8f,
                                                    modifier              = Modifier.matchParentSize()
                                                )
                                            }
                                        }

                                    } else {
                                        /* ── NO tile: keep dial centred with two spacers ───────── */

                                        Spacer(Modifier.weight(1f))          // balance left
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(Modifier.size(120.dp)) {
                                                EventProgressIndicator(
                                                    elapsedTime           = elapsedTime,
                                                    events                = displayEvents,
                                                    centerCircleRadius    = 3.dp,
                                                    eventerLengthFraction = 0.8f,
                                                    modifier              = Modifier.matchParentSize()
                                                )
                                            }
                                        }
                                        Spacer(Modifier.weight(1f))          // balance right
                                    }
                                }







                                Spacer(Modifier.height(12.dp))

                                // ── PLAY/PAUSE  |  NEW EVENT buttons under PPI ──
                                Row(
                                    Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // ─── Play / Pause ───
                                    TouchVolumeButton(
                                        icon            = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        label           = "",
                                        tapInstruction  = "Play/Pause",
                                        holdInstruction = "Save",
                                        onTap           = onToggleStopwatch,
                                        onLongPress     = onPrepareSaveShare,
                                        backgroundColor = MaterialTheme.colorScheme.surface,
                                        iconTint        = MaterialTheme.colorScheme.onBackground,
                                        modifier        = Modifier
                                            .weight(1f)
                                            .padding(8.dp)
                                    )

                                    // ─── New Event ───
                                    TouchVolumeButton(
                                        icon            = Icons.Filled.Timer,
                                        label           = "",
                                        tapInstruction  = "New Event",
                                        holdInstruction = "Reset",
                                        onTap           = onNewEvent,
                                        onLongPress     = onResetRequest,
                                        backgroundColor = MaterialTheme.colorScheme.surface,
                                        iconTint        = MaterialTheme.colorScheme.onBackground,
                                        modifier        = Modifier
                                            .weight(1f)
                                            .padding(8.dp)
                                    )


                                }


                                Spacer(Modifier.height(12.dp))

                                // ── COMMENT / IMAGE / PRESET buttons ──
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    /* ----- Comment toggle ----- */
                                    IconButton(
                                        onClick = {
                                            val new = sheetSettings.copy(showComment = !sheetSettings.showComment)
                                            CoroutineScope(Dispatchers.IO).launch { saveSheetSettings(context, new) }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .border(
                                                1.dp,
                                                if (sheetSettings.showComment)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.outline,
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

                                    /* ----- Image toggle ----- */
                                    IconButton(
                                        onClick = {
                                            val new = sheetSettings.copy(showImage = !sheetSettings.showImage)
                                            CoroutineScope(Dispatchers.IO).launch { saveSheetSettings(context, new) }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .border(
                                                1.dp,
                                                if (sheetSettings.showImage)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.outline,
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

                                    /* ----- Preset‑cycles button ----- */
                                    IconButton(
                                        onClick = { onShowPresetDialogChange(true) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .border(
                                                1.dp,
                                                if (activeCycle != null)
                                                    MaterialTheme.colorScheme.primary        // highlight when a preset is loaded
                                                else
                                                    MaterialTheme.colorScheme.outline,
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Preset Cycles",
                                            tint = if (activeCycle != null)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }


                                Spacer(Modifier.height(12.dp))

                                // ── EVENT TABLE ──
                                EventTable(
                                    events = allEvents,
                                    currentActiveEvent = currentActiveEvent,
                                    timeFormatSetting = timeFormatSetting,
                                    sheetSettings = sheetSettings,
                                    hasPresetLoaded      = activeCycle != null,
                                    onCommentClick = { selectedEventForComment = it },
                                    onImageClick = { onImageClick(it) },
                                    onAddCommentForLive = onAddComment,
                                    onCaptureImageForLive = onCaptureImage,
                                )
                            }
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
                                events = displayEvents,
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
                            /* ── stats under the graph ───────────────────────────────────────── */

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)        // keep same inset as the graph
                            ) {
                                /* LEFT column: Max / Min / Range */
                                Column(Modifier.weight(1f)) {
                                    Text("Max:    ${formatTime(maxTime,    timeFormatSetting)}",
                                        style = MaterialTheme.typography.bodyMedium)
                                    Text("Min:    ${formatTime(minTime,    timeFormatSetting)}",
                                        style = MaterialTheme.typography.bodyMedium)
                                    Text("Range:  ${formatTime(rangeTime,  timeFormatSetting)}",
                                        style = MaterialTheme.typography.bodyMedium)
                                }

                                /* RIGHT column: Median / Average / Std Dev – right-aligned */
                                Column(
                                    Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text("Median:  ${formatTime(medianTime.toLong(), timeFormatSetting)}",
                                        style = MaterialTheme.typography.bodyMedium)
                                    Text("Average: ${formatTime(averageTime,         timeFormatSetting)}",
                                        style = MaterialTheme.typography.bodyMedium)
                                    Text("Std Dev: ${formatTime(stdDevTime.toLong(), timeFormatSetting)}",
                                        style = MaterialTheme.typography.bodyMedium)
                                }
                            }


                            Spacer(Modifier.height(12.dp))
                            EventTable(
                                events = allEvents,
                                currentActiveEvent    = currentActiveEvent,
                                timeFormatSetting     = timeFormatSetting,
                                sheetSettings         = sheetSettings,
                                hasPresetLoaded      = activeCycle != null,
                                onCommentClick        = { selectedEventForComment = it },
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
            // 1. time-stamp + optional preset name
            val timeStamp  = SimpleDateFormat("yyyy-MM-dd, HH:mm", Locale.getDefault()).format(Date())
            val presetPart = activeCycle?.preset?.name?.takeIf { it.isNotBlank() }?.let { " - $it" } ?: ""
            val defaultName = timeStamp + presetPart

            RenameFileDialog(
                defaultName = defaultName,

                // ✅ delegate the work to the Activity
                onConfirm = { newName ->
                    onRenameConfirm(newName)                     // <- calls saveCsvFile + clears isDirty
                    onActiveDialogChange(ActiveDialog.None)      // <- closes the dialog
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

    selectedEventForComment?.let { event ->
        CommentDialogUnified(
            initialComment = event.comment,
            fastCommentsSettings = fastCommentsSettings,
            isEditingOldEvent = true,
            onConfirm = { newComment ->
                onUpdateEventComment(event, newComment)
                selectedEventForComment = null
            },
            onDismiss = { selectedEventForComment = null },
            onEditFastComments   = onEditFastComments
        )
    }

    selectedEventForImage?.let {
        ImageCaptureDialog(
            onCapture = {
                onCaptureImage()
                selectedEventForImage = null
            },
            onDismiss = { selectedEventForImage = null }
        )
    }
    // inside MainScreen – or wherever you show the tips dialog

    if (showTipsDialog) {
        AlertDialog(
            onDismissRequest = { showTipsDialog = false },
            title = { Text("Quick guide") },
            text  = {
                Column {
                    Text("• Tap the info icon any time to see this guide again.")
                    Text("• Use the volume buttons to Play/Pause or add a New-Event (tap vs. hold).")
                    Text("• Swipe left / right for the three main views.")
                    Text("• Tweak the layout in Settings to match your workflow.")
                    Text("• Saved Files lets you filter, view, delete or export to Excel.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTipsDialog = false          // close dialog
                        scope.launch {                  // <-- run suspend code here
                            saveShowTips(context, false)
                        }
                    }
                ) { Text("Got it") }
            }
        )
    }
}






// --- MainActivity ---
class MainActivity : ComponentActivity() {




    // ── NEW: Hold all saved presets in memory (you can later persist via DataStore) ──
    private var allPresetCycles by mutableStateOf(
        listOf(
            PresetCycle(name = "Elevator Up", steps = listOf("In", "Up", "Out", "Down")),
            // (You can add more defaults here if you like)
        )
    )
    private var activeCycle      by mutableStateOf<ActiveCycle?>(null)
    private var currentCycleNumber by mutableStateOf(1)


    private fun defaultCycles(): List<PresetCycle> = listOf(
        // Compact demo
        PresetCycle(
            name  = "Elevator Up",
            steps = listOf("In", "Up", "Out", "Down")
        ),

        // Practical 10-step example
        PresetCycle(
            name  = "Assembly Line",
            steps = listOf(
                "Infeed",
                "Pick Part",
                "Align Part",
                "Screw / Fasten",
                "Torque Check",
                "Insert Label",
                "Visual Inspect",
                "Place on Conveyor",
                "Press Start Button",
                "Wait For Next"
            )
        ),

        // Sandbox for experimenting

    )



    private var isDirty by mutableStateOf(false)

    private var currentActiveEvent by mutableStateOf<EventData?>(null)

    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>

    private val REQUEST_CODE_CAMERA_PERMISSION = 1001

    private var pendingImageUpdateEvent: EventData? = null

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

    private fun saveAllPresetCycles(newList: List<PresetCycle>) {
        allPresetCycles = newList

        // Persist on a background thread:
        lifecycleScope.launch {
            saveAllPresetCycles(this@MainActivity, newList)
        }
    }

    /*  ──  PRESET  IMPORT / EXPORT  ─────────────── */
    private lateinit var exportPresetsLauncher: ActivityResultLauncher<Intent>
    private lateinit var importPresetsLauncher: ActivityResultLauncher<Array<String>>



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



    /** Shows a dialog after importing presets. `null` = no dialog */
    private var importSummary by mutableStateOf<ImportSummary?>(null)

    data class ImportSummary(
        val added: Int,
        val updated: Int,
        val skipped: Int
    )

    // For viewing files.
    private var selectedFile: File? by mutableStateOf(null)

    // Rename file state.
    private var pendingCsvContent: String? = null

    // Stopwatch state.
    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> = _isRunning

    private var startTime: Long = 0L
    private var accumulatedTime: Long = 0L

    // Current Event start.
    private var currentEventStartTime: Long = 0L

    // Elapsed time state.
    private val _elapsedTime = mutableStateOf(0L)
    val elapsedTime: State<Long> = _elapsedTime

    // List of Events.
    private val _events = mutableStateListOf<EventData>()
    val events: List<EventData> get() = _events

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

        val csvContent = generateCSV(events, sheetSettings, currentTimeFormatSetting)
        Log.d("DEBUG", "CSV Content:\n$csvContent")  // Log CSV content for debug if needed.
        file.outputStream().use {
            it.write(csvContent.toByteArray())
        }
        Toast.makeText(this, "CSV file saved as $finalName", Toast.LENGTH_SHORT).show()
    }

    private fun handleImportedPresets(incoming: List<PresetCycle>) {
        lifecycleScope.launch {
            var merged = allPresetCycles.toMutableList()
            var updated = 0
            var added   = 0
            for (preset in incoming) {
                val clashIdx = merged.indexOfFirst { it.name == preset.name }
                if (clashIdx == -1) {
                    merged += preset
                    added++
                } else {
                    // prompt the user synchronously on the main thread
                    val choice = suspendCancellableCoroutine<String> { cont ->
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("‘${preset.name}’ already exists")
                            .setItems(arrayOf("Keep old", "Overwrite", "Keep both")) { _, which ->
                                cont.resume(
                                    when (which) {
                                        0 -> "keep-old"
                                        1 -> "overwrite"
                                        else -> "keep-both"
                                    }, null)
                            }
                            .setCancelable(false)
                            .show()
                    }
                    when (choice) {
                        "overwrite" -> { merged[clashIdx] = preset; updated++ }
                        "keep-both" -> {
                            var suffix = 2
                            var newName: String
                            do {
                                newName = "${preset.name} ($suffix)"
                                suffix++
                            } while (merged.any { it.name == newName })
                            merged += preset.copy(name = newName)
                            added++
                        }
                    } // keep‑old → nothing
                }
            }

            // persist + update UI
            saveAllPresetCycles(this@MainActivity, merged)
            allPresetCycles = merged

            withContext(Dispatchers.Main) {
                importSummary = ImportSummary(
                    added   = added,
                    updated = updated,
                    skipped = incoming.size - added - updated
                )
            }
        }
    }

    private fun exportSelectedPresets(selected: List<PresetCycle>) {
        clearOldExportsCache()   // reuse your existing cache cleaner

        val file = File(cacheDir, "preset_cycles_${System.currentTimeMillis()}.presetcycles.json")
        file.writeBytes(selected.toJsonBytes())

        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(contentResolver, file.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        exportPresetsLauncher.launch(Intent.createChooser(send, "Share presets"))
    }



    private fun undoLastEvent() {
        if (_events.isNotEmpty()) {
            // Remove the latest recorded event
            val removed = _events.removeAt(_events.lastIndex)
            // Add its elapsed time back to the accumulated time
            accumulatedTime += removed.elapsedTime
            // Update the UI: if the stopwatch is running, update the active event; if not, update the displayed time
            if (_isRunning.value) {
                currentActiveEvent?.elapsedTime = (currentActiveEvent?.elapsedTime ?: 0L) + removed.elapsedTime
            } else {
                _elapsedTime.value = accumulatedTime
            }
            ToastHelper.showToast(this, "Last event undone", 1000L)
        } else {
            Toast.makeText(this, "No event to undo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Context.shareExcelFile(excel: File) {

        // 1. Build the content‑URI with your FileProvider authority
        val authority = "${packageName}.fileprovider"      // ← uses runtime package name
        val uri       = FileProvider.getUriForFile(this, authority, excel)

        // 2. Craft the SEND intent
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_SUBJECT, "Exported Events")
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


            readAllPresetCycles(this@MainActivity).collect { savedList ->
                if (savedList.isEmpty()) {
                    // First‐run (or user cleared everything) -> seed built‐in defaults
                    val builtIns = defaultCycles()
                    allPresetCycles = builtIns

                    // Persist them so next time DataStore is not empty
                    saveAllPresetCycles(this@MainActivity, builtIns)
                } else {
                    // We already have at least one preset stored -> use it
                    allPresetCycles = savedList
                }
            }
        }

        clearOldExportsCache()

        /* ----------  export launcher (share sheet)  ---------- */
        exportPresetsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { /* no‑op */ }

        /* ----------  import launcher (open document) ---------- */
        importPresetsLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri ?: return@registerForActivityResult   // ⬅️  old: return@OpenDocument

                val bytes = contentResolver.openInputStream(uri)?.readBytes()
                if (bytes == null || bytes.size > 1_000_000) {
                    ToastHelper.showToast(this, "File too large or unreadable")
                    return@registerForActivityResult       // ⬅️  old: return@OpenDocument
                }

                val incoming = decodePresetCycles(bytes)
                if (incoming == null) {
                    ToastHelper.showToast(this, "File is not a valid preset list")
                    return@registerForActivityResult       // ⬅️  old: return@OpenDocument
                }

                handleImportedPresets(incoming)
            }


        val activityInstance = this

        // Step A: define your built-in defaults
        val defaultCycles = listOf(
            PresetCycle("Elevator Up", listOf("In", "Up", "Out", "Down")),
            PresetCycle("Process", listOf("In", "Load", "Process", "Unload", "Out"))
        )

        // Step B: immediately read “allPresetCycles” from DataStore (or wherever you persist),
        // and if nothing’s there, seed it with defaults.
        lifecycleScope.launch {
            // Suppose you had a Flow<List<PresetCycle>> called `readPresetCycles(...)`.
            // For simplicity, let’s just check if your in-memory `allPresetCycles` is still empty:
            if (allPresetCycles.isEmpty()) {
                // First-run (or no saved presets): populate with defaults
                allPresetCycles = defaultCycles
                //  `allPresetCycles` into DataStore here so that next time it’s restored
            } else {
                // You already have saved cycles (merging defaults if you wish):
                val saved = allPresetCycles
                // If you want to ensure defaults are always present—merge them if missing:
                val merged = defaultCycles
                    .filter { builtIn -> saved.none { it.name == builtIn.name } }
                    .plus(saved)
                allPresetCycles = merged
            }
        }

        cameraResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val imagePath = result.data?.getStringExtra("imagePath") ?: return@registerForActivityResult

                    if (pendingImageUpdateEvent != null) {
                        // old event case
                        val live = pendingImageUpdateEvent!!
                        val idx  = _events.indexOf(live)
                        if (idx != -1) {
                            _events[idx] = live.copy(imagePath = "file://$imagePath")
                        }
                        pendingImageUpdateEvent = null

                    } else if (currentActiveEvent != null) {
                        // paused live event case
                        currentActiveEvent = currentActiveEvent!!.copy(imagePath = "file://$imagePath")

                    } else if (_events.isNotEmpty()) {
                        // fallback: last historical event
                        val last = _events.last()
                        _events[_events.lastIndex] = last.copy(imagePath = "file://$imagePath")
                    }
                }
            }


        setContent {

                // Get the context for DataStore access.
                val context = LocalContext.current

                val coroutineScope   = rememberCoroutineScope()   // ← add this

                val useDarkMode by readDarkModeSetting(context)
                    .collectAsState(initial = true)


            MyApplicationTheme(useDarkTheme = useDarkMode) {

                // ── 1) read the colour INSIDE a composable  ─────────────────────────
                val surfaceColor = MaterialTheme.colorScheme.onSurface          // ✅ composable read

                // ── 2) apply it to the system bar in a side‑effect  ────────────────
                val view = LocalView.current
                SideEffect {
                    WindowCompat.getInsetsController(window, view)
                        ?.isAppearanceLightStatusBars = !useDarkMode          // light‑/dark icons

                    window.statusBarColor = surfaceColor.toArgb()             // ← use captured colour
                }

                // Reference to the current Activity instance.
                val activityInstance = this

                // at the very top of onCreate()   (inside setContent { … })


                // Local UI state.
                var showCommentDialog by remember { mutableStateOf(false) }
                var showImageUpdateDialog by remember { mutableStateOf(false) }
                var showFastCommentsEditDialog by remember { mutableStateOf(false) }

                var showPresetDialog           by remember { mutableStateOf(false) }
                var showNewPresetDialog by remember { mutableStateOf(false) }
                var showExportPicker           by remember { mutableStateOf(false) }
                var cyclePendingDelete by remember { mutableStateOf<PresetCycle?>(null) }
                var editPresetTarget by remember { mutableStateOf<PresetCycle?>(null) }

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

                LaunchedEffect(Unit) {
                        readAllPresetCycles(context).collect { list ->
                            allPresetCycles = list          // ← this is the mutableStateOf property
                        }
                    }

                // Ensure CSV folder exists.
                val csvDir = File(context.getExternalFilesDir(null), "StopwatchMetrics")
                if (!csvDir.exists()) {
                    csvDir.mkdirs()
                }

                // Regenerate CSV content whenever events, sheet settings, or the time format setting change.
                val csvContent by remember(events, currentSheetSettings, timeFormatSetting) {
                    derivedStateOf { generateCSV(events, currentSheetSettings, timeFormatSetting) }
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
                                elapsedTime = elapsedTime.value,
                                isRunning = isRunning.value,
                                events = if (isRunning.value && currentActiveEvent != null)
                                    events + currentActiveEvent!!
                                else
                                    events,
                                currentActiveEvent = currentActiveEvent,
                                timeFormatSetting = timeFormatSetting,
                                sheetSettings = currentSheetSettings,
                                activeDialog = activeDialog,
                                isDirty = isDirty,

                                onActiveDialogChange = { activeDialog = it },
                                onToggleStopwatch = { toggleStopwatch(); isDirty = true },
                                onNewEvent = { newEvent(); isDirty = true },
                                onResetRequest = {
                                    if (isDirty) activeDialog = ActiveDialog.ConfirmReset
                                    else resetStopwatch()
                                },
                                onFileBrowserClick = { currentScreen = Screen.FileBrowser },
                                onSettingsClick = { currentScreen = Screen.Settings },
                                onPrepareSaveShare = {
                                    pendingCsvContent = generateCSV(events, sheetSettings, currentTimeFormatSetting)
                                    activeDialog = ActiveDialog.SaveShare
                                },
                                onShare = { sendEmail() },
                                onRenameConfirm = { newName -> saveCsvFile(newName); isDirty = false; activeDialog = ActiveDialog.None },
                                onCaptureImage = { captureImage() },
                                onAddComment = { showCommentDialog = true },
                                onEditFastComments = { showCommentDialog = false; showFastCommentsEditDialog = true },
                                onUndo = { undoLastEvent(); isDirty = true },

                                fastCommentsSettings = currentFastComments,

                                onToggleImageColumn = {
                                    val new = sheetSettings.copy(showImage = !sheetSettings.showImage)
                                    sheetSettings = new
                                    CoroutineScope(Dispatchers.IO).launch { saveSheetSettings(context, new) }
                                },
                                onToggleCommentColumn = {
                                    val new = sheetSettings.copy(showComment = !sheetSettings.showComment)
                                    sheetSettings = new
                                    CoroutineScope(Dispatchers.IO).launch { saveSheetSettings(context, new) }
                                },

                                onImageClick = { event ->
                                    pendingImageUpdateEvent = event
                                    showImageUpdateDialog = true
                                },
                                onUpdateEventComment = { event, newComment ->
                                    val idx = _events.indexOf(event)
                                    if (idx != -1) {
                                        _events[idx] = event.copy(comment = newComment)
                                    } else if (currentActiveEvent?.eventStartTime == event.eventStartTime) {
                                        currentActiveEvent = event.copy(comment = newComment)
                                    }
                                    isDirty = true
                                },
                                onUpdateEventImage = { event, newPath ->
                                    val idx = _events.indexOf(event)
                                    if (idx != -1) {
                                        _events[idx] = event.copy(imagePath = newPath)
                                    } else if (currentActiveEvent?.eventStartTime == event.eventStartTime) {
                                        currentActiveEvent = event.copy(imagePath = newPath)
                                    }
                                    isDirty = true
                                },

                                activeCycle = activeCycle,
                                onActiveCycleChange   = { activeCycle = it },

                                onLoadCycle = { chosen ->
                                    activeCycle = ActiveCycle(chosen, 0)   // index starts at 0
                                    // no changes to currentActiveEvent here
                                },
                                onSaveAllCycles = { updatedList ->
                                    // Write back the updated list of presets
                                    CoroutineScope(Dispatchers.IO).launch {
                                        saveAllPresetCycles(context, updatedList)
                                    }
                                },
                                allPresetCycles = allPresetCycles,

                                onClearPreset = {
                                    activeCycle = null
                                },
                                onConfigurePresets = { showPresetDialog = true },
                                showPresetDialog = showPresetDialog,
                                onShowPresetDialogChange = { showPresetDialog = it },

                                cycleNumber       = currentCycleNumber,
                                onCycleIncrement         = { currentCycleNumber++ }


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
                                // ① Just kick off the camera
                                captureImage()
                                // ② Close the dialog
                                showImageUpdateDialog = false
                            },
                            onDismiss = {
                                showImageUpdateDialog = false
                                pendingImageUpdateEvent = null
                            }
                        )
                    }
                    if (showCommentDialog) {
                        CommentDialogUnified(
                            initialComment       = currentActiveEvent?.comment.orEmpty(),
                            fastCommentsSettings = currentFastComments,
                            isEditingOldEvent    = false,
                            onConfirm = { newComment ->
                                currentActiveEvent?.let { live ->
                                    val idx = _events.indexOf(live)
                                    val updatedComment = newComment

                                    if (idx != -1) {
                                        // historical event (unlikely here)
                                        _events[idx] = live.copy(comment = updatedComment)
                                    } else {
                                        // paused “live” event
                                        currentActiveEvent = live.copy(comment = updatedComment)
                                    }
                                }
                                showCommentDialog = false
                            },
                            onDismiss = { showCommentDialog = false },
                            onEditFastComments = {
                                // hide comment dialog, show the editor
                                showCommentDialog = false
                                showFastCommentsEditDialog = true
                            }
                        )
                    }

                    if (showPresetDialog) {
                        PresetCycleListDialog(
                            allCycles    = allPresetCycles,
                            activeCycle  = activeCycle,
                            onDismiss    = { showPresetDialog = false },
                            onLoad = { chosen ->
                                activeCycle = ActiveCycle(preset = chosen, currentIndex = 0)

                                showPresetDialog = false         // close the list
                            },
                            onEdit = { cycle ->
                                editPresetTarget  = cycle      // open editor for this preset
                                showPresetDialog  = false
                            },

                            onCreateNew  = { showNewPresetDialog = true; showPresetDialog = false },
                            onUnload     = { activeCycle = null; showPresetDialog = false },   // renamed
                            onDelete = { cycle ->
                                val newList = allPresetCycles - cycle
                                coroutineScope.launch(Dispatchers.IO) {
                                    saveAllPresetCycles(context, newList)
                                }
                                allPresetCycles = newList
                            },


                            /* NEW callbacks */
                            onImport    = {
                                /* launches the ActivityResult you registered in onCreate() */
                                importPresetsLauncher.launch(arrayOf("application/json"))
                                showPresetDialog = false        // close the list
                            },
                            onExport    = {
                                showExportPicker = true         // open picker dialog
                                showPresetDialog = false
                            }
                        )
                    }

                    cyclePendingDelete?.let { cycle ->
                        AlertDialog(
                            onDismissRequest = { cyclePendingDelete = null },
                            title = { Text("Delete preset?") },
                            text  = { Text("Are you sure you want to delete “${cycle.name}”?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val newList = allPresetCycles - cycle        // remove from UI list
                                        coroutineScope.launch(Dispatchers.IO) {      // persist off‑thread
                                            saveAllPresetCycles(context, newList)
                                        }
                                        allPresetCycles     = newList                // update UI
                                        cyclePendingDelete  = null                   // close dialog
                                    }
                                ) { Text("Delete") }
                            },
                            dismissButton = {
                                OutlinedButton(
                                    onClick = { cyclePendingDelete = null }
                                ) { Text("Cancel") }
                            }
                        )
                    }

                    editPresetTarget?.let { target ->
                        PresetCycleEditDialog(
                            original      = target,
                            existingNames = allPresetCycles.map { it.name },
                            onConfirm     = { updated ->
                                // replace old entry, keep list order
                                val newList = allPresetCycles.map { if (it.name == target.name) updated else it }
                                // persist off the main thread
                                coroutineScope.launch(Dispatchers.IO) {
                                    saveAllPresetCycles(context, newList)
                                }
                                allPresetCycles   = newList   // UI refresh
                                editPresetTarget  = null      // close editor
                            },
                            onDismiss = { editPresetTarget = null }
                        )
                    }

                    /* showExportPicker handled exactly as in my previous snippet */
                    if (showExportPicker) {
                        PresetExportPickerDialog(
                            all      = allPresetCycles,
                            onDismiss= { showExportPicker = false },
                            onExport = { list ->
                                exportSelectedPresets(list)
                                showExportPicker = false
                            }
                        )
                    }

                    importSummary?.let { s ->
                        AlertDialog(
                            onDismissRequest = { importSummary = null },
                            title = { Text("Preset import complete") },
                            text  = {
                                Text(
                                    "${s.added} imported, " +
                                            "${s.updated} overwritten, " +
                                            "${s.skipped} skipped."
                                )
                            },
                            confirmButton = {
                                Button(onClick = { importSummary = null }) {
                                    Text("OK")
                                }
                            }
                        )
                    }

                    if (showNewPresetDialog) {
                        NewPresetDialog(
                            existingNames = allPresetCycles.map { it.name },
                            onDismiss = { showNewPresetDialog = false },
                            onSave = { newPreset ->
                                val merged = allPresetCycles
                                    .filterNot { it.name == newPreset.name } + newPreset

                                // Persist off the main thread
                                coroutineScope.launch(Dispatchers.IO) {
                                    saveAllPresetCycles(context, merged)   // suspend fun inside coroutine
                                }

                                activeCycle       = ActiveCycle(newPreset, 0)
                                showNewPresetDialog = false
                            }
                        )
                    }
                    if (showFastCommentsEditDialog) {
                        FastCommentsEditDialog(
                            initialFastComments = currentFastComments,
                            onConfirm = { newSettings ->
                                // persist and close
                                CoroutineScope(Dispatchers.IO).launch {
                                    saveFastCommentsSettings(context, newSettings)
                                }
                                showFastCommentsEditDialog = false
                            },
                            onDismiss = { showFastCommentsEditDialog = false }
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
                    pendingCsvContent = generateCSV(events, sheetSettings, currentTimeFormatSetting)
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
                    newEvent()                     // add a event
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
                currentEventStartTime = System.currentTimeMillis()
                currentActiveEvent = EventData(
                    elapsedTime    = 0L,
                    eventStartTime = currentEventStartTime,
                    cycleNumber    = currentCycleNumber
                )

            }
            startTime = System.currentTimeMillis()
            updateElapsedTime()
        } else {
            _isRunning.value = false
            accumulatedTime += System.currentTimeMillis() - startTime
        }
    }

    private fun newEvent() {

        // 0 ── make sure the watch is running
        if (!_isRunning.value) toggleStopwatch()

        val now = System.currentTimeMillis()

        // 1 ── if we have no live event yet, create one immediately
        if (currentActiveEvent == null) {
            currentEventStartTime = now
            currentActiveEvent = EventData(
                elapsedTime    = 0L,
                eventStartTime = currentEventStartTime,
                cycleNumber    = currentCycleNumber
            )
        }

        // 2 ── very first tap → stamp 1st preset step and bail out
        if (_elapsedTime.value == 0L) {
            activeCycle?.let { cycle ->
                cycle.preset.steps.getOrNull(cycle.currentIndex)?.let { step ->
                    currentActiveEvent?.comment = step
                    activeCycle = cycle.copy(currentIndex = cycle.currentIndex + 1)
                }
            }
            return
        }

        // 3 ── commit the previous live event **only if it had real duration**
        currentActiveEvent?.let { live ->
            if (live.elapsedTime > 50L) {        // <── guard-rail phantom event "0.00"
                _events.add(live)
            }
        }

        // 4 ── start a fresh live event
        startTime         = now
        accumulatedTime   = 0L
        _elapsedTime.value= 0L
        currentEventStartTime = now
        currentActiveEvent = EventData(
            elapsedTime    = 0L,
            eventStartTime = currentEventStartTime,
            cycleNumber    = currentCycleNumber
        )

        // 5 ── stamp the next preset step, if any
        activeCycle?.let { cycle ->
            cycle.preset.steps.getOrNull(cycle.currentIndex)?.let { step ->
                currentActiveEvent = currentActiveEvent!!.copy(comment = step)
                activeCycle = cycle.copy(currentIndex = cycle.currentIndex + 1)
            }
        }
    }


    private fun resetStopwatch() {
        _isRunning.value = false
        accumulatedTime = 0L
        _elapsedTime.value = 0L
        _events.clear()
        currentActiveEvent = null
        currentCycleNumber = 1                 // start counting cycles from 1 again
        activeCycle = activeCycle?.copy(currentIndex = 0)   // rewind preset (if one is loaded)
    }

    private fun updateElapsedTime() {
        lifecycleScope.launch {
            while (_isRunning.value) {
                val now = System.currentTimeMillis()
                _elapsedTime.value = accumulatedTime + (now - startTime)
                currentActiveEvent?.elapsedTime = _elapsedTime.value
                delay(100)
            }
        }
    }

    private fun exportExcelFileAndShare() {
        val exportedFile = exportExcelFile(
            context             = this,
            events              = events,
            settings            = sheetSettings,
            timeFormatSetting   = currentTimeFormatSetting,
            presetName        = activeCycle?.preset?.name
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
        val csvContent = generateCSV(events, sheetSettings, currentTimeFormatSetting)
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Recorded Time Measurements")
            putExtra(Intent.EXTRA_TEXT, csvContent)
        }
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }
}
