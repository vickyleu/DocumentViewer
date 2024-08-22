package org.uooc.document

import androidx.compose.runtime.Composable
import coil3.PlatformContext
import com.github.jing332.filepicker.base.FileImpl

object DocumentPreviewer {
    @Composable
    fun previewDocument(document: FileImpl) {
        println("Previewing document at ${document.getAbsolutePath()}")
        documentView(document)
    }

    fun setup(license: String, applicationContext: PlatformContext) {
        println("Setting up document previewer with license $license")
        setup(license, applicationContext)
    }
}

@Composable
internal expect fun DocumentPreviewer.documentView(document: FileImpl)

internal expect fun DocumentPreviewer.setupLicense(license:String, applicationContext: PlatformContext)

