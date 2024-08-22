package org.uooc.document

import androidx.compose.runtime.Composable
import com.github.jing332.filepicker.base.FileImpl

object DocumentPreviewer {
    @Composable
    fun previewDocument(document: FileImpl) {
        println("Previewing document at ${document.getAbsolutePath()}")
        documentView(document)
    }
}

@Composable
internal expect fun DocumentPreviewer.documentView(document: FileImpl)