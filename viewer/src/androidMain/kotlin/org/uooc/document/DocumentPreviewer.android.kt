package org.uooc.document

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import coil3.PlatformContext
import com.github.jing332.filepicker.base.FileImpl
import com.tencent.tbs.reader.TbsFileInterfaceImpl
import kotlinx.coroutines.launch


internal actual fun DocumentPreviewer.setupLicense(license:String, applicationContext: PlatformContext) {
    TbsFileInterfaceImpl.setLicenseKey(license)
    TbsFileInterfaceImpl.fileEnginePreCheck(applicationContext)
    val isInit = TbsFileInterfaceImpl.initEngine(applicationContext)
}


@Composable
internal actual fun DocumentPreviewer.documentView(document: FileImpl) {
    val file = remember {
        mutableStateOf(document)
    }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val loadState = remember { mutableStateOf(false to "Loading document...") }

        val documentView = remember {
            mutableStateOf<DocumentView?>(null)
        }
        AndroidView(
            factory = { context ->
                DocumentView(context).apply {
                    documentView.value = this
                }
            },
            update = {

            },
            modifier = Modifier.fillMaxSize()
        )

        DisposableEffect(documentView.value) {
            scope.launch {
                documentView.value?.setDocument(scope, file.value, density) { success, message ->
                    loadState.value = success to message
                }
            }
            onDispose {
                documentView.value?.dispose()
            }
        }

        if (loadState.value.first) {
            return@Box
        }
        Text(
            text = loadState.value.second,
            modifier = Modifier.align(Alignment.Center)
        )

    }
}