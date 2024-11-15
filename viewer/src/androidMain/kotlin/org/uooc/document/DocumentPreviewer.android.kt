package org.uooc.document

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import coil3.Uri
import com.github.jing332.filepicker.base.FileImpl
import com.tencent.tbs.reader.TbsFileInterfaceImpl
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch


internal actual fun DocumentPreviewer.setupLicense(
    license: String,
    applicationContext: coil3.PlatformContext
) {
    val ctx = applicationContext.applicationContext as Context
    TbsFileInterfaceImpl.setLicenseKey(license)
    TbsFileInterfaceImpl.fileEnginePreCheck(ctx)
    //初始化Engine
    val isInit = if(TbsFileInterfaceImpl.isEngineLoaded().not()){
        TbsFileInterfaceImpl.initEngine(ctx)
    }else {
        DocumentPreviewer.TMResult.SUCCESS.code
    }
//    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//        if(!Settings.System.canWrite(ctx)){
//            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
//            intent.setData(android.net.Uri.parse("package:" + ctx.packageName))
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            ctx.startActivity(intent)
//        }
//    }
    this.currentState = DocumentPreviewer.TMResult.fromCode(isInit)
    println("TbsFileInterfaceImpl.initEngine: ${this.currentState.message}")
}


@Composable
internal actual fun DocumentPreviewer.documentView(
    document: FileImpl,
    callback: (Boolean, String) -> Unit
) {
    val file = remember {
        mutableStateOf(document)
    }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val loadState = remember { mutableStateOf(false to "Loading document...") }

        val documentView = remember {
            mutableStateOf<DocumentView?>(null)
        }
        with(LocalDensity.current){
            Column(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        DocumentView(context).apply {
                            minimumWidth = this@BoxWithConstraints.maxWidth.roundToPx()
                            minimumHeight = this@BoxWithConstraints.maxHeight.roundToPx()
                            documentView.value = this
                        }
                    },
                    update = {

                    },
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight()

                )
            }
        }

        LaunchedEffect(documentView.value) {
            if(documentView.value==null){
                return@LaunchedEffect
            }
            scope.launch {
                documentView.value?.setDocument(scope, file.value, density,this@documentView.currentState) { success, message ->
                    loadState.value = success to message
                }
            }
        }
        DisposableEffect(documentView.value) {
            if(documentView.value==null){
                return@DisposableEffect onDispose {  }
            }
            onDispose {
                documentView.value?.dispose()
                documentView.value = null
            }
        }

        if (loadState.value.first.not()) {
            Text(
                text = loadState.value.second.let {
                    if(it.contains("未设置 licenseKey")){
                        "tbs未充值,请联系管理员"
                    }else{
                        it
                    }
                },
                modifier = Modifier.align(Alignment.Center)
            )
        }
        LaunchedEffect(Unit) {
            snapshotFlow { loadState.value }
                .drop(1)
                .distinctUntilChanged()
                .collect{
                    callback(it.first,it.second)
                }
        }

    }
}