package org.uooc.document

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import com.github.jing332.filepicker.base.FileImpl
import kotlinx.coroutines.launch


@Composable
internal actual fun DocumentPreviewer.documentView(document: FileImpl) {
    val file = remember {
        mutableStateOf(document)
    }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val loadState = remember { mutableStateOf(false to "Loading document...") }
        AndroidView(
            factory = { context ->
                DocumentView(context)
            },
            update = {
                scope.launch {
                    it.setDocument(scope, file.value, density) { success, message ->
                        loadState.value = success to message
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        if (loadState.value.first) {
            return@Box
        }
        Text(
            text = loadState.value.second,
            modifier = Modifier.align(Alignment.Center)
        )

    }
}