package org.uooc.document

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.github.jing332.filepicker.base.FileImpl
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.QuickLook.QLPreviewController
import platform.QuickLook.QLPreviewControllerDataSourceProtocol
import platform.QuickLook.QLPreviewControllerDelegateProtocol
import platform.QuickLook.QLPreviewItemProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.darwin.NSInteger
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun DocumentPreviewer.documentView(
    document: FileImpl,
    callback: (Boolean, String) -> Unit
) {
    val window = UIApplication.sharedApplication.keyWindow ?: return
    val controller = window.rootViewController ?: return
    val file = remember {
        mutableStateOf(document)
    }
    val scope = rememberCoroutineScope()
    println("Previewing document at ${file.value.getAbsolutePath()} !!!!!! iOS")
    val quickLookController = remember(file.value) { QLPreviewControllerImpl(file = file.value) }
    DisposableEffect(Unit) {
        println("Presenting document preview presentViewController")
        scope.launch {
            withContext(Dispatchers.Main) {
                quickLookController.openDocument(controller) { success, message ->
                    callback(success, message)
                }
            }
        }
        onDispose {
        }
    }
}

internal class QLPreviewControllerImpl(file: FileImpl) {
    private val quickLookController = QLPreviewController()
    private val dataSource = object : NSObject(), QLPreviewControllerDataSourceProtocol {
        override fun numberOfPreviewItemsInPreviewController(controller: QLPreviewController): NSInteger {
            return 1
        }

        override fun previewController(
            controller: QLPreviewController,
            previewItemAtIndex: NSInteger
        ): QLPreviewItemProtocol {
            return object : NSObject(), QLPreviewItemProtocol {
                override fun previewItemURL(): NSURL {
                    println("Previewing document previewItemURL at ${file.getAbsolutePath()} !!!!!! iOS")
                    return NSURL(fileURLWithPath = file.getAbsolutePath())
                }
            }
        }
    }
    private val delegate = object : NSObject(), QLPreviewControllerDelegateProtocol {
        override fun previewControllerDidDismiss(controller: QLPreviewController) {
            println("Document preview dismissed")
            callback(false, "")
        }
    }

    init {
        quickLookController.dataSource = dataSource
        quickLookController.delegate = delegate
    }

    private var callback: (Boolean, String) -> Unit = { _, _ -> }
    fun openDocument(controller: UIViewController, callback: (Boolean, String) -> Unit) {
        quickLookController.refreshCurrentPreviewItem()
        this.callback = callback
        controller.presentViewController(quickLookController, true, null)
    }
}

internal actual fun DocumentPreviewer.setupLicense(
    license: String,
    applicationContext: coil3.PlatformContext
) {
}