/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2020 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tencent.bkrepo.preview.config

import com.tencent.bkrepo.common.artifact.repository.context.ArtifactDownloadContext
import com.tencent.bkrepo.common.artifact.repository.context.ArtifactUploadContext
import com.tencent.bkrepo.common.artifact.repository.local.LocalRepository
import com.tencent.bkrepo.common.artifact.resolve.response.ArtifactResource
import com.tencent.bkrepo.preview.constant.PREVIEW_NODE_DETAIL
import com.tencent.bkrepo.preview.constant.PreviewMessageCode
import com.tencent.bkrepo.preview.exception.PreviewNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 公共local仓库
 */
@Component
class PreviewLocalRepository : LocalRepository() {

    companion object {
        private val logger = LoggerFactory.getLogger(PreviewLocalRepository::class.java)
    }

    override fun onUpload(context: ArtifactUploadContext) {
        val nodeDetail = storageManager.storeArtifactFile(
            buildNodeCreateRequest(context),
            context.getArtifactFile(),
            context.storageCredentials
        )
        context.putAttribute(PREVIEW_NODE_DETAIL, nodeDetail)
    }

    override fun onDownload(context: ArtifactDownloadContext): ArtifactResource? {
        val resource = super.onDownload(context)
            ?: throw PreviewNotFoundException(
                code = PreviewMessageCode.PREVIEW_FILE_NOT_FOUND,
                "${context.artifactInfo.projectId}|${context.artifactInfo.repoName}" +
                        "|${context.artifactInfo.getArtifactFullPath()}"
            )
        return resource
    }

}