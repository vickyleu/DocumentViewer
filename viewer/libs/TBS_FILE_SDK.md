# Tencent TbsFile SDK binary

This module expects the official Tencent Browsing Service document SDK binary:

- Version: `V1.0.8.6000124`
- Release date: `2026-08-06`
- Variant: comprehensive document formats, `64-bit + 32-bit`
- Expected file path: `viewer/libs/TbsFileSdk.aar`
- Official SDK page: `https://cloud.tencent.com/document/product/1645/83899`
- Official binary URL at the time of this update: `https://tbs.imtt.qq.com/sdk/release/TbsFileSdk_base_universal_release_1.0.8.6000124.20260806101826.aar`

The AAR is intentionally replaced and verified on a local build machine because the repository connector used for source maintenance does not upload binary repository contents.

Before publishing `viewer`, verify the downloaded AAR came from the official Tencent host, record its SHA-256, run a clean Android build, and confirm a smoke-test document can be opened.
