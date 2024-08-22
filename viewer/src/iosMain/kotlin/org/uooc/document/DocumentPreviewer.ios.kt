package org.uooc.document

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import coil3.PlatformContext
import com.github.jing332.filepicker.base.FileImpl
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.QuickLook.QLPreviewController
import platform.QuickLook.QLPreviewControllerDataSourceProtocol
import platform.QuickLook.QLPreviewControllerDelegateProtocol
import platform.QuickLook.QLPreviewItemProtocol
import platform.UIKit.UIApplication
import platform.darwin.NSInteger
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun DocumentPreviewer.documentView(document: FileImpl) {
    val window = UIApplication.sharedApplication.keyWindow ?: return
    val controller = window.rootViewController ?: return
    val file = remember {
        mutableStateOf(document)
    }
    val quickLookController = remember { QLPreviewController() }
    DisposableEffect(Unit) {
        quickLookController.dataSource =
            object : NSObject(), QLPreviewControllerDataSourceProtocol {
                override fun numberOfPreviewItemsInPreviewController(controller: QLPreviewController): NSInteger {
                    return 1
                }

                override fun previewController(
                    controller: QLPreviewController,
                    previewItemAtIndex: NSInteger
                ): QLPreviewItemProtocol {
                    return object : NSObject(), QLPreviewItemProtocol {
                        override fun previewItemURL(): NSURL {
                            return NSURL(fileURLWithPath = file.value.getAbsolutePath())
                        }
                    }
                }
            }
        quickLookController.delegate = object : NSObject(), QLPreviewControllerDelegateProtocol {
            override fun previewControllerDidDismiss(controller: QLPreviewController) {
                println("Document preview dismissed")
            }
        }
        quickLookController.refreshCurrentPreviewItem()
        quickLookController.presentViewController(controller, true, null)
        onDispose {
        }
    }
}

internal actual fun DocumentPreviewer.setupLicense(
    license: String,
    applicationContext: PlatformContextAlias
) {
}