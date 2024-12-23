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

private val TAG = "DocumentPreviewer"

class DocumentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private lateinit var currentState: DocumentPreviewer.TMResult
    suspend fun setDocument(
        scope: CoroutineScope,
        file: FileImpl,
        density: Density,
        currentState: DocumentPreviewer.TMResult,
        callback: (Boolean, String) -> Unit
    ) {
        this.currentState = currentState
        val completer = CompletableDeferred<Pair<Boolean, String>>()
        scope.launch {
            withContext(Dispatchers.IO) {
                //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
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
                if (this@DocumentView.currentState.code != 0) {
                    completer.complete(false to "TbsFile Engine初始化失败")
                    return@withContext
                }
                //文件格式
                val fileExt = FileUtils.getFileType(file.toString())
                println("文件格式：$fileExt")


                withContext(Dispatchers.Main) {
                    val bool = TbsFileInterfaceImpl.canOpenFileExt(fileExt)
                    Log.d(TAG, "文件是否支持$bool  文件路径：$file $bsReaderTemp $fileExt")
                    if (bool) {
                        //加载文件
                        val localBundle = Bundle()
                        localBundle.putString("filePath", file.absolutePath.toString())
                        localBundle.putString("tempPath", bsReaderTemp)
                        localBundle.putString("fileExt", fileExt)

                        localBundle.putInt(
                            "set_content_view_width",
                            with(density) { measuredWidth.toFloat().dp.value.roundToInt() })
//                        localBundle.putBoolean("file_reader_stream_mode", false)//设置为文件流打开模式
                        localBundle.putInt(
                            "set_content_view_height",
                            with(density) {
                                measuredHeight.toFloat().dp.value.roundToInt().coerceAtLeast(200)
                            })
                        this@DocumentView.post {
                            val ret = TbsFileInterfaceImpl.getInstance().openFileReader(
                                context, localBundle,
                                { code, args, msg ->
                                    Log.e(TAG, "文件打开回调 $code  $args  $msg")
                                    when (code) {
                                        ITbsReader.OPEN_FILEREADER_STATUS_UI_CALLBACK -> {
                                            if (args is Bundle) {
                                                val id = args.getInt("typeId", 0)
                                                val typeDes =
                                                    args.getString("typeDes", "fileReaderOpened")
                                                if (ITbsReader.TBS_READER_TYPE_STATUS_UI_OPENED == id) {
                                                    //加密文档弹框取消需关闭activity
//                                                Navigation.findNavController(getView()).popBackStack()

                                                } else if (ITbsReader.TBS_READER_TYPE_STATUS_UI_SHUTDOWN == id) {
                                                    //加密文档弹框取消需关闭activity
//                                                Navigation.findNavController(getView()).popBackStack()
                                                    if (completer.isCompleted.not()) {
                                                        completer.complete(false to "文件打开2失败:${msg}")
                                                    }
                                                }
                                            } else {
                                                if (completer.isCompleted.not()) {
                                                    completer.complete(false to "文件打开3失败:${msg}")
                                                }
                                            }
                                        }

                                        ITbsReader.NOTIFY_CANDISPLAY -> {
                                            //文件即将显示
                                            Log.wtf("NOTIFY_CANDISPLAY", "文件即将显示")
                                            completer.complete(true to "")
                                        }

                                        else -> Unit
                                    }
                                }, this@DocumentView
                            )
                            if (ret == 0) {
                            } else {
                                completer.complete(false to "error:$ret")
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
        this.getViewTreeObserver().addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                this@DocumentView.getViewTreeObserver().removeOnGlobalLayoutListener(this)
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