package org.uooc.document

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.jing332.filepicker.base.FileImpl
import com.tencent.tbs.reader.ITbsReader
import com.tencent.tbs.reader.TbsFileInterfaceImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

private const val TAG = "DocumentPreviewer"

class DocumentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private lateinit var currentState: DocumentPreviewer.TMResult
    private var currentStateCode: Int = DocumentPreviewer.TMResult.UNKNOWN.code

    suspend fun setDocument(
        scope: CoroutineScope,
        file: FileImpl,
        density: Density,
        currentState: DocumentPreviewer.TMResult,
        currentStateCode: Int,
        callback: (Boolean, String) -> Unit
    ) {
        this.currentState = currentState
        this.currentStateCode = currentStateCode
        val completer = CompletableDeferred<Pair<Boolean, String>>()
        scope.launch {
            withContext(Dispatchers.IO) {
                // 增加下面一句解决没有 TbsReaderTemp 文件夹存在导致加载文件失败
                val bsReaderTemp =
                    FileUtils.getDir(context).toString() + File.separator + "TbsReaderTemp"
                val bsReaderTempFile = File(bsReaderTemp)
                if (!bsReaderTempFile.exists()) {
                    val mkdir: Boolean = bsReaderTempFile.mkdir()
                    if (!mkdir) {
                        Log.e(TAG, "创建$bsReaderTemp 失败")
                        completer.complete(false to "TbsReaderTemp缓存文件创建失败")
                        return@withContext
                    }
                }

                if (this@DocumentView.currentStateCode != DocumentPreviewer.TMResult.SUCCESS.code) {
                    val diagnostic = DocumentPreviewer.TMResult.diagnosticMessage(
                        this@DocumentView.currentStateCode,
                        this@DocumentView.currentState
                    )
                    Log.e(TAG, "$diagnostic, package=${context.packageName}")
                    completer.complete(false to diagnostic)
                    return@withContext
                }

                // 文件格式
                val fileExt = FileUtils.getFileType(file.toString())
                Log.d(TAG, "文件格式：$fileExt")

                withContext(Dispatchers.Main) {
                    val bool = TbsFileInterfaceImpl.canOpenFileExt(fileExt)
                    Log.d(TAG, "文件是否支持$bool  文件路径：$file $bsReaderTemp $fileExt")
                    if (bool) {
                        // 加载文件
                        val localBundle = Bundle()
                        localBundle.putString("filePath", file.absolutePath.toString())
                        localBundle.putString("tempPath", bsReaderTemp)
                        localBundle.putString("fileExt", fileExt)

                        localBundle.putInt(
                            "set_content_view_width",
                            with(density) { measuredWidth.toFloat().dp.value.roundToInt() }
                        )
//                        localBundle.putBoolean("file_reader_stream_mode", false)//设置为文件流打开模式
                        localBundle.putInt(
                            "set_content_view_height",
                            with(density) {
                                measuredHeight.toFloat().dp.value.roundToInt().coerceAtLeast(200)
                            }
                        )
                        this@DocumentView.post {
                            val ret = TbsFileInterfaceImpl.getInstance().openFileReader(
                                context, localBundle,
                                { code, args, msg ->
                                    Log.e(TAG, "文件打开回调 code=$code args=$args msg=$msg")
                                    when (code) {
                                        ITbsReader.OPEN_FILEREADER_STATUS_UI_CALLBACK -> {
                                            if (args is Bundle) {
                                                val id = args.getInt("typeId", 0)
                                                if (ITbsReader.TBS_READER_TYPE_STATUS_UI_SHUTDOWN == id) {
                                                    if (!completer.isCompleted) {
                                                        completer.complete(false to "文件打开失败(code=$code): $msg")
                                                    }
                                                }
                                            } else if (!completer.isCompleted) {
                                                completer.complete(false to "文件打开失败(code=$code): $msg")
                                            }
                                        }

                                        ITbsReader.NOTIFY_CANDISPLAY -> {
                                            Log.d(TAG, "文件即将显示")
                                            if (!completer.isCompleted) {
                                                completer.complete(true to "")
                                            }
                                        }

                                        else -> Unit
                                    }
                                }, this@DocumentView
                            )
                            if (ret != 0 && !completer.isCompleted) {
                                val message = "openFileReader失败(code=$ret)"
                                Log.e(TAG, "$message, file=$file")
                                completer.complete(false to message)
                            }
                        }
                    } else {
                        Log.e(TAG, "文件打开失败！文件格式暂不支持")
                        completer.complete(false to "文件格式不支持或者打开失败")
                    }
                }
            }
        }
        val (rlt, msg) = completer.await()
        callback.invoke(rlt, msg)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        this.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                this@DocumentView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val w: Int = this@DocumentView.width
                val h: Int = this@DocumentView.height
                TbsFileInterfaceImpl.getInstance().onSizeChanged(w, h)
            }
        })
    }

    fun dispose() {
        try {
            this.removeAllViews()
            val instance = TbsFileInterfaceImpl.getInstance()
            instance.closeFileReader()
        } catch (ignore: Exception) {
        }
    }
}
