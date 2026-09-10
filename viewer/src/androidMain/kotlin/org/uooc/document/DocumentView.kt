package org.uooc.document

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.compose.ui.unit.Density
import com.github.jing332.filepicker.base.FileImpl
import com.tencent.tbs.reader.ITbsReader
import com.tencent.tbs.reader.TbsFileInterfaceImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "DocumentPreviewer"

class DocumentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    private lateinit var currentState: DocumentPreviewer.TMResult

    @Suppress("UNUSED_PARAMETER")
    suspend fun setDocument(
        scope: CoroutineScope,
        file: FileImpl,
        density: Density,
        currentState: DocumentPreviewer.TMResult,
        callback: (Boolean, String) -> Unit,
    ) {
        this.currentState = currentState
        val completer = CompletableDeferred<Pair<Boolean, String>>()

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val tbsReaderTemp = File(FileUtils.getDir(context), "TbsReaderTemp")
                    if (!tbsReaderTemp.exists() && !tbsReaderTemp.mkdirs()) {
                        Log.e(TAG, "Failed to create TBS temp directory: $tbsReaderTemp")
                        completeOnce(completer, false, "TbsReaderTemp缓存文件创建失败")
                        return@withContext
                    }

                    if (this@DocumentView.currentState != DocumentPreviewer.TMResult.SUCCESS) {
                        val initCode = DocumentPreviewer.currentInitCode
                        completeOnce(
                            completer,
                            false,
                            DocumentPreviewer.describeInitResult(initCode),
                        )
                        return@withContext
                    }

                    val fileExt = FileUtils.getFileType(file.toString())
                    Log.d(TAG, "Opening document: ext=$fileExt")

                    withContext(Dispatchers.Main) {
                        if (!TbsFileInterfaceImpl.canOpenFileExt(fileExt)) {
                            Log.e(TAG, "TBS cannot open extension: $fileExt")
                            completeOnce(completer, false, "文件格式不支持或者打开失败: $fileExt")
                            return@withContext
                        }

                        val localBundle = Bundle().apply {
                            putString("filePath", file.absolutePath.toString())
                            putString("tempPath", tbsReaderTemp.absolutePath)
                            putString("fileExt", fileExt)
                            // These values are pixels already. Converting px -> dp -> numeric px was incorrect.
                            putInt("set_content_view_width", measuredWidth.coerceAtLeast(1))
                            putInt("set_content_view_height", measuredHeight.coerceAtLeast(200))
                        }

                        this@DocumentView.post {
                            try {
                                val ret = TbsFileInterfaceImpl.getInstance().openFileReader(
                                    context,
                                    localBundle,
                                    { code, args, msg ->
                                        Log.d(TAG, "TBS open callback code=$code, message=$msg")
                                        when (code) {
                                            ITbsReader.OPEN_FILEREADER_STATUS_UI_CALLBACK -> {
                                                if (args is Bundle) {
                                                    val id = args.getInt("typeId", 0)
                                                    if (ITbsReader.TBS_READER_TYPE_STATUS_UI_SHUTDOWN == id) {
                                                        completeOnce(
                                                            completer,
                                                            false,
                                                            "文件阅读器已关闭${msg?.takeIf { it.isNotBlank() }?.let { ": $it" } ?: ""}",
                                                        )
                                                    }
                                                } else {
                                                    completeOnce(
                                                        completer,
                                                        false,
                                                        "文件阅读器状态异常${msg?.takeIf { it.isNotBlank() }?.let { ": $it" } ?: ""}",
                                                    )
                                                }
                                            }

                                            ITbsReader.NOTIFY_CANDISPLAY -> {
                                                completeOnce(completer, true, "")
                                            }
                                        }
                                    },
                                    this@DocumentView,
                                )

                                if (ret != 0) {
                                    completeOnce(
                                        completer,
                                        false,
                                        describeOpenReaderFailure(ret),
                                    )
                                }
                            } catch (t: Throwable) {
                                Log.e(TAG, "TBS openFileReader threw", t)
                                completeOnce(
                                    completer,
                                    false,
                                    "TbsFile 打开文件异常: ${t.message ?: t::class.simpleName}",
                                )
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Document preparation failed", t)
                completeOnce(
                    completer,
                    false,
                    "文档预览初始化异常: ${t.message ?: t::class.simpleName}",
                )
            }
        }

        val (result, message) = completer.await()
        callback(result, message)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    TbsFileInterfaceImpl.getInstance().onSizeChanged(width, height)
                }
            },
        )
    }

    fun dispose() {
        try {
            TbsFileInterfaceImpl.getInstance().closeFileReader()
        } catch (t: Throwable) {
            Log.w(TAG, "TBS closeFileReader failed", t)
        } finally {
            removeAllViews()
        }
    }

    private fun completeOnce(
        completer: CompletableDeferred<Pair<Boolean, String>>,
        success: Boolean,
        message: String,
    ) {
        if (!completer.isCompleted) {
            completer.complete(success to message)
        }
    }

    private fun describeOpenReaderFailure(code: Int): String {
        val reason =
            when (code) {
                -1 -> "参数错误"
                -2 -> "Reader 尚未加载"
                -3 -> "鉴权失败"
                -4 -> "Engine 正在加载"
                -5 -> "阅读器 View 初始化失败"
                -6 -> "文件格式不支持"
                -7 -> "Reader 入口正在异步加载"
                -8 -> "TBS Core 正在下载或尚未就绪"
                else -> "未知错误"
            }
        return "TbsFile 打开文件失败 (code=$code, reason=$reason)"
    }
}
