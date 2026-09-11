package org.uooc.document

import androidx.compose.runtime.Composable
import com.github.jing332.filepicker.base.FileImpl

object DocumentPreviewer {
    var currentState = TMResult.UNKNOWN
        internal set

    /** Raw TBS initEngine return code. Kept separately so unknown SDK codes are not lost. */
    var currentInitCode: Int = TMResult.UNKNOWN.code
        internal set

    @Composable
    fun previewDocument(document: FileImpl, callback: (Boolean, String) -> Unit) {
        println("Previewing document at ${document.getAbsolutePath()}")
        documentView(document, callback)
    }

    fun setup(license: String, applicationContext: coil3.PlatformContext) {
        // Never print the license key. setupLicense() logs only non-sensitive diagnostics.
        setupLicense(license, applicationContext)
    }

    /** TBS File Engine initialization result codes. */
    enum class TMResult(val code: Int = 0, val message: String = "") {
        SUCCESS(0, "Success"),
        UNSET(102, "License key is not set"),
        NETWORK_MAYBE1(103, "Network unavailable; check connectivity or try another network"),
        MISMATCH(209, "License key mismatch"),
        CHECK(202, "Invalid license API usage; setLicenseKey must be used"),
        QUOTA1(212, "TBS File quota is exhausted or unavailable to this app"),
        NETWORK_MAYBE2(305, "Network unavailable; check connectivity or try another network"),
        QUOTA2(322, "TBS File quota is exhausted or unavailable to this app"),
        NOT_EXIST(4001, "License key does not exist"),
        PACKAGE(4002, "Application package name does not match the license key"),
        UNKNOWN(-1, "Unknown TBS File initialization error");

        companion object {
            fun fromCode(code: Int): TMResult = values().firstOrNull { it.code == code } ?: UNKNOWN
        }
    }

    internal fun describeInitResult(code: Int): String {
        val result = TMResult.fromCode(code)
        return if (result == TMResult.SUCCESS) {
            "TbsFile Engine initialized"
        } else if (result == TMResult.UNKNOWN) {
            "TbsFile Engine initialization failed (code=$code, reason=${result.message})"
        } else {
            "TbsFile Engine initialization failed (code=$code, reason=${result.message})"
        }
    }
}

@Composable
internal expect fun DocumentPreviewer.documentView(
    document: FileImpl,
    callback: (Boolean, String) -> Unit,
)

internal expect fun DocumentPreviewer.setupLicense(
    license: String,
    applicationContext: coil3.PlatformContext,
)
