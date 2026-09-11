# Tencent TbsFile SDK binary

This module expects the official Tencent Browsing Service document SDK binary:

- Version: `V1.0.8.6000124`
- Release date: `2026-08-06`
- Variant: comprehensive document formats, `64-bit + 32-bit`
- Expected file path: `viewer/src/androidMain/libs/TbsFileSdk.aar`
- Official SDK page: `https://cloud.tencent.com/document/product/1645/83899`
- Official binary URL at the time of this update: `https://tbs.imtt.qq.com/sdk/release/TbsFileSdk_base_universal_release_1.0.8.6000124.20260806101826.aar`

The repository currently contains an AAR at that path, but its binary version has not been verified/replaced through the source connector. Replace it on the local build machine with the official `V1.0.8.6000124` binary before validation and publication.

Before publishing `viewer`, verify the downloaded AAR came from the official Tencent host, record its SHA-256, run a clean Android build, and confirm a smoke-test document can be opened.
