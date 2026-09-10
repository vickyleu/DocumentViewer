package org.uooc.document

import android.content.Context
import android.util.Log
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

private const val TAG = "DocumentPreviewer"

internal actual fun DocumentPreviewer.setupLicense(
    license: String,
    applicationContext: coil3.PlatformContext,
) {
    val ctx = applicationContext.applicationContext as Context
    val initCode =
        try {
            if (license.isBlank()) {
                DocumentPreviewer.TMResult.UNSET.code
            } else {
                TbsFileInterfaceImpl.setLicenseKey(license)
                TbsFileInterfaceImpl.fileEnginePreCheck(ctx)
                if (TbsFileInterfaceImpl.isEngineLoaded()) {
                    DocumentPreviewer.TMResult.SUCCESS.code
                } else {
                    TbsFileInterfaceImpl.initEngine(ctx)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "TbsFile initEngine threw an exception for package=${ctx.packageName}", t)
            DocumentPreviewer.TMResult.UNKNOWN.code
        }

    this.currentInitCode = initCode
    this.currentState = DocumentPreviewer.TMResult.fromCode(initCode)
    Log.i(
        TAG,
        "TbsFile initEngine package=${ctx.packageName}, code=$initCode, " +
            "state=${this.currentState.name}, engineLoaded=${runCatching { TbsFileInterfaceImpl.isEngineLoaded() }.getOrDefault(false)}",
    )
}

@Composable
internal actual fun DocumentPreviewer.documentView(
    document: FileImpl,
    callback: (Boolean, String) -> Unit,
) {
    val file = remember { mutableStateOf(document) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val loadState = remember { mutableStateOf(false to "Loading document...") }
        val documentView = remember { mutableStateOf<DocumentView?>(null) }

        with(LocalDensity.current) {
            Column(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        DocumentView(context).apply {
                            minimumWidth = this@BoxWithConstraints.maxWidth.roundToPx()
                            minimumHeight = this@BoxWithConstraints.maxHeight.roundToPx()
                            documentView.value = this
                        }
                    },
                    update = {},
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                )
            }
        }

        LaunchedEffect(documentView.value) {
            val view = documentView.value ?: return@LaunchedEffect
            scope.launch {
                view.setDocument(scope, file.value, density, this@documentView.currentState) { success, message ->
                    loadState.value = success to message
                }
            }
        }

        DisposableEffect(documentView.value) {
            if (documentView.value == null) {
                return@DisposableEffect onDispose {}
            }
            onDispose {
                documentView.value?.dispose()
                documentView.value = null
            }
        }

        if (!loadState.value.first) {
            // Do not translate a missing/mismatched license into "not recharged"; those are different failures.
            Text(
                text = loadState.value.second,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        LaunchedEffect(Unit) {
            snapshotFlow { loadState.value }
                .drop(1)
                .distinctUntilChanged()
                .collect {
                    callback(it.first, it.second)
                }
        }
    }
}
