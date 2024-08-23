package org.uooc.document

import androidx.compose.runtime.Composable
import com.github.jing332.filepicker.base.FileImpl

object DocumentPreviewer {
    var currentState = TMResult.UNKNOWN
        internal set
    @Composable
    fun previewDocument(document: FileImpl,callback: (Boolean, String) -> Unit) {
        println("Previewing document at ${document.getAbsolutePath()}")
        documentView(document,callback)
    }

    fun setup(license: String, applicationContext: coil3.PlatformContext) {
        println("Setting up document previewer with license $license")
        setupLicense(license, applicationContext)
    }


    /**
     * 初始化接口错误码
     * intEngine 接口错误码为方法返回值。
     * initEngineAsync 接口错误码为回调 actionType == ITbsReader.OPEN_FILEREADER_ASYNC_LOAD_READER_ENTRY_CALLBACK 时 args 的值。
     * 错误码
     * 说明
     * 102
     * 未设置 licenseKey。
     * 202
     * 请检查调用接口是否正确，应调用 setLicenseKey 接口而不是 setLicense 接口。
     * 103 、305
     * 1. 请检查设备网络是否连通。
     * 2. 尝试切换网络。
     * 212、322
     * 调用量包次数用完。
     * 4001
     * licenseKey 不存在，请检查设置的 licenseKey 是否正确。
     * 4002
     * 客户端包名和 licenseKey 不匹配。
     *
     *
     *
     */
    enum class TMResult(val code: Int = 0, val message: String = "") {
        SUCCESS(0, "Success"),
        MISMATCH(4002, "License key mismatch"),
        UNSET(102, "Unset license key"),
        CHECK(202, "Check if the interface is called correctly, should call setLicenseKey instead of setLicense"),
        NETWORK_MAYBE1(103, "Check if the device network is connected, try switching networks"),
        NETWORK_MAYBE2(305, "Check if the device network is connected, try switching networks"),
        QUOTA1(212, "The number of calls is used up"),
        QUOTA2(322, "The number of calls is used up"),
        NOT_EXIST(4001, "License key does not exist, please check if the set license key is correct"),
        PACKAGE(4002, "The client package name does not match the license key"),
        UNKNOWN(-1, "Unknown error");

        companion object {
            fun fromCode(code: Int): TMResult {
                return values().find { it.code == code } ?: UNKNOWN
            }
        }
    }
}


@Composable
internal expect fun DocumentPreviewer.documentView(
    document: FileImpl,
    callback: (Boolean, String) -> Unit
)

internal expect fun DocumentPreviewer.setupLicense(
    license: String,
    applicationContext: coil3.PlatformContext
)