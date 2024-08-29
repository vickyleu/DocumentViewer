package org.uooc.document

import android.content.Context
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
        Column(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    DocumentView(context).apply {
                        documentView.value = this
                    }
                },
                update = {

                },
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight()

            )
        }

        DisposableEffect(documentView.value) {
            scope.launch {
                documentView.value?.setDocument(scope, file.value, density,this@documentView.currentState) { success, message ->
                    loadState.value = success to message
                }
            }
            onDispose {
                documentView.value?.dispose()
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