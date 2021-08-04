/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2021 THL A29 Limited, a Tencent company.  All rights reserved.
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

package com.tencent.bkrepo.repository.service.node

import com.tencent.bkrepo.common.artifact.api.ArtifactInfo
import com.tencent.bkrepo.repository.pojo.node.FileExtensionStatInfo
import com.tencent.bkrepo.repository.pojo.node.NodeSizeInfo

/**
 * 节点重命名接口
 */
interface NodeStatsOperation {

    /**
     * 计算文件或者文件夹大小
     */
    fun computeSize(artifact: ArtifactInfo): NodeSizeInfo

    /**
     * 查询文件节点数量
     */
    fun countFileNode(artifact: ArtifactInfo): Long

    /**
     * 计算文件大小分布
     */
    fun computeSizeDistribution(projectId: String, range: List<Long>, repoName: String?): Map<String, Long>

    /**
     * 查询文件的后缀名
     */
    fun getFileExtensions(projectId: String, repoName: String?): List<String>

    /**
     * 根据文件后缀名统计文件数量，大小
     */
    fun statFileExtension(projectId: String, extension: String, repoName: String?): FileExtensionStatInfo
}
