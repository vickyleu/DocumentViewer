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
                if(this@DocumentView.currentState.code!=0){
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
                        localBundle.putBoolean("file_reader_stream_mode", true)//设置为文件流打开模式
                        localBundle.putInt(
                            "set_content_view_width",
                            with(density) { width.toFloat().dp.value.roundToInt() })
                        // TODO  高度目前还不确定需不需要,等腾讯回复
                        /**
                         * System.out               I  文件格式：pdf
                         * DocumentPreviewer        D  文件是否支持true  文件路径：/storage/emulated/0/Android/data/uooconline.com.education/files/Documents/file_cache/temp_5474223ad190a6233f72b7f0281b96c3/舒程测试成人高考腾讯电子签模板.pdf /data/user/0/uooconline.com.education/files/file_preview/TbsReaderTemp pdf
                         * ReaderEngine             D  createTbsReader success
                         * DocumentPreviewer        E  文件打开回调 7000  Bundle[{typeId=0, typeDes=fileReaderOpened}]  null
                         * beacon-thread-3          W  type=1400 audit(0.0:491547): avc: denied { read } for name="type" dev="sysfs" ino=34791 scontext=u:r:untrusted_app:s0:c203,c257,c512,c768 tcontext=u:object_r:sysfs:s0 tclass=file permissive=0
                         * TbsReaderCore            D  OpenFile result = 0
                         * AutofillManager          V  requestHideFillUi(null): anchor = null
                         * System.out               I  文件格式：pdf
                         * System.out               I  [socket]:check permission begin!
                         * System                   W  ClassLoader referenced unknown path: system/framework/mediatek-cta.jar
                         * System.out               I  [socket] e:java.lang.ClassNotFoundException: com.mediatek.cta.CtaUtils
                         * DocumentPreviewer        E  文件打开回调 5030  Bundle[{name=PDFReader, version=11.6.1.1}]  null
                         * e.com.education          W  type=1400 audit(0.0:491550): avc: granted { execute } for path="/data/data/uooconline.com.education/app_tbs/home/default/components/file/6000020/libmttpdfcore.so" dev="mmcblk0p46" ino=3558324 scontext=u:r:untrusted_app:s0:c203,c257,c512,c768 tcontext=u:object_r:app_data_file:s0:c203,c257,c512,c768 tclass=file
                         * DocumentPreviewer        E  文件打开回调 5071  null  null
                         * DocumentPreviewer        E  文件打开回调 5031  0  null
                         * DocumentPreviewer        D  文件是否支持true  文件路径：/storage/emulated/0/Android/data/uooconline.com.education/files/Documents/file_cache/temp_5474223ad190a6233f72b7f0281b96c3/舒程测试成人高考腾讯电子签模板.pdf /data/user/0/uooconline.com.education/files/file_preview/TbsReaderTemp pdf
                         * ReaderEngine             D  createTbsReader success
                         * DocumentPreviewer        E  文件打开回调 7000  Bundle[{typeId=0, typeDes=fileReaderOpened}]  null
                         * TbsReaderCore            D  OpenFile result = 0
                         * DocumentPreviewer        E  文件打开回调 5030  Bundle[{name=PDFReader, version=11.6.1.1}]  null
                         * DocumentPreviewer        E  文件打开回调 5071  null  null
                         * DocumentPreviewer        E  文件打开回调 5031  0  null
                         * pdfiumJni                D  initLibraryIfNeed do init
                         * pdfiumJni                D  initLibraryIfNeed sLibraryReferenceCount=1
                         * pdfiumJni                D  destroyLibraryIfNeed do destroy
                         * pdfiumJni                D  destroyLibraryIfNeed sLibraryReferenceCount=0
                         * pdfiumJni                D  initLibraryIfNeed do init
                         * pdfiumJni                D  initLibraryIfNeed sLibraryReferenceCount=1
                         * DocumentPreviewer        E  文件打开回调 5037  Bundle[{bflag=false}]  null
                         * DocumentPreviewer        E  文件打开回调 19  0  null
                         * DocumentPreviewer        E  文件打开回调 5048  Bundle[{cur_page=1, page_count=4}]  null
                         * System.out               I  [okhttp]:check permission begin!
                         * System                   W  ClassLoader referenced unknown path: system/framework/mediatek-cta.jar
                         * System.out               I  [okhttp] e:java.lang.ClassNotFoundException: com.mediatek.cta.CtaUtils
                         * System.out               I  [socket]:check permission begin!
                         * DocumentPreviewer        E  文件打开回调 5033  null  null
                         * DocumentPreviewer        E  文件打开回调 5024  null  Bundle[{}]
                         * DocumentPreviewer        E  文件打开回调 12  null  null
                         * System                   W  ClassLoader referenced unknown path: system/framework/mediatek-cta.jar
                         * NOTIFY_CANDISPLAY        E  文件即将显示
                         */
                        localBundle.putInt(
                            "set_content_view_height",
                            with(density) { height.toFloat().dp.value.roundToInt().coerceAtLeast(200) })
                        this@DocumentView.post {
                            val ret = TbsFileInterfaceImpl.getInstance().openFileReader(
                                context, localBundle,
                                { code, args, msg ->
                                    Log.e(TAG, "文件打开回调 $code  $args  $msg")
                                    when (code) {
                                        ITbsReader.OPEN_FILEREADER_STATUS_UI_CALLBACK->{
                                            if (args is Bundle) {
                                                val id = args.getInt("typeId", 0)
                                                val typeDes = args.getString("typeDes", "fileReaderOpened")
                                                if (ITbsReader.TBS_READER_TYPE_STATUS_UI_OPENED == id) {
                                                    //加密文档弹框取消需关闭activity
//                                                Navigation.findNavController(getView()).popBackStack()
                                                    if(completer.isCompleted.not()){
                                                        completer.complete(false to "文件打开失败:${msg}")
                                                    }
                                                } else if (ITbsReader.TBS_READER_TYPE_STATUS_UI_SHUTDOWN == id) {
                                                    //加密文档弹框取消需关闭activity
//                                                Navigation.findNavController(getView()).popBackStack()
                                                    if(completer.isCompleted.not()){
                                                        completer.complete(false to "文件打开失败:${msg}")
                                                    }
                                                }
                                            }else{
                                                if(completer.isCompleted.not()){
                                                    completer.complete(false to "文件打开失败:${msg}")
                                                }
                                            }
                                        }
                                        ITbsReader.NOTIFY_CANDISPLAY -> {
                                            //文件即将显示
                                            Log.wtf("NOTIFY_CANDISPLAY", "文件即将显示")
                                            completer.complete(true to "")
                                        }
                                    }
                                }, this@DocumentView
                            )
                            if (ret == 0) {
                            } else {
                                if(ret==-8){
                                    completer.complete(true to "")
                                }else{
                                    completer.complete(false to "error:$ret")
                                }
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