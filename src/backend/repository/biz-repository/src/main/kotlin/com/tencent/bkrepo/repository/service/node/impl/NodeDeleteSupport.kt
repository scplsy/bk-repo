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
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.bkrepo.repository.service.node.impl

import com.tencent.bkrepo.common.api.exception.ErrorCodeException
import com.tencent.bkrepo.common.api.message.CommonMessageCode
import com.tencent.bkrepo.common.api.util.HumanReadable
import com.tencent.bkrepo.common.artifact.path.PathUtils
import com.tencent.bkrepo.common.service.util.SpringContextUtils.Companion.publishEvent
import com.tencent.bkrepo.repository.dao.NodeDao
import com.tencent.bkrepo.repository.model.TNode
import com.tencent.bkrepo.repository.pojo.node.NodeDeleteResult
import com.tencent.bkrepo.repository.pojo.node.NodeListOption
import com.tencent.bkrepo.repository.pojo.node.service.NodeDeleteRequest
import com.tencent.bkrepo.repository.pojo.node.service.NodesDeleteRequest
import com.tencent.bkrepo.repository.service.node.NodeDeleteOperation
import com.tencent.bkrepo.repository.service.repo.QuotaService
import com.tencent.bkrepo.repository.util.NodeEventFactory.buildDeletedEvent
import com.tencent.bkrepo.repository.util.NodeQueryHelper
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.where
import java.time.LocalDateTime

/**
 * 节点删除接口实现
 */
open class NodeDeleteSupport(
    private val nodeBaseService: NodeBaseService
) : NodeDeleteOperation {

    val nodeDao: NodeDao = nodeBaseService.nodeDao
    val quotaService: QuotaService = nodeBaseService.quotaService

    override fun deleteNode(deleteRequest: NodeDeleteRequest): NodeDeleteResult {
        with(deleteRequest) {
            // 不允许直接删除根目录
            if (PathUtils.isRoot(fullPath)) {
                throw ErrorCodeException(CommonMessageCode.METHOD_NOT_ALLOWED, "Can't delete root node.")
            }
            return deleteByPath(projectId, repoName, fullPath, operator)
        }
    }

    override fun deleteNodes(nodesDeleteRequest: NodesDeleteRequest): NodeDeleteResult {
        with(nodesDeleteRequest) {
            if (fullPaths.isEmpty()) {
                throw ErrorCodeException(CommonMessageCode.PARAMETER_EMPTY, "fullPaths is empty.")
            }
            fullPaths.forEach {
                // 不允许直接删除根目录
                if (PathUtils.isRoot(it)) {
                    throw ErrorCodeException(CommonMessageCode.METHOD_NOT_ALLOWED, "Can't delete root node.")
                }
            }
            return deleteByPaths(projectId, repoName, fullPaths, operator)
        }
    }

    override fun countDeleteNodes(nodesDeleteRequest: NodesDeleteRequest): Long {
        with(nodesDeleteRequest) {
            if (fullPaths.isEmpty()) {
                return 0L
            }
            val orOperation = mutableListOf<Criteria>()
            val normalizedFullPaths = fullPaths.map { PathUtils.normalizeFullPath(it) }
            orOperation.add(where(TNode::fullPath).inValues(normalizedFullPaths))
            normalizedFullPaths.forEach {
                val normalizedPath = PathUtils.toPath(it)
                val escapedPath = PathUtils.escapeRegex(normalizedPath)
                orOperation.add(where(TNode::fullPath).regex("^$escapedPath"))
            }
            val criteria = where(TNode::projectId).isEqualTo(projectId)
                .and(TNode::repoName).isEqualTo(repoName)
                .and(TNode::deleted).isEqualTo(null)
                .and(TNode::folder).isEqualTo(isFolder)
                .orOperator(*orOperation.toTypedArray())
            return nodeDao.count(Query(criteria))
        }
    }

    override fun deleteByPath(
        projectId: String,
        repoName: String,
        fullPath: String,
        operator: String
    ): NodeDeleteResult {
        var deletedSize = 0L
        var deletedNum = 0L
        val normalizedFullPath = PathUtils.normalizeFullPath(fullPath)
        val normalizedPath = PathUtils.toPath(normalizedFullPath)
        val escapedPath = PathUtils.escapeRegex(normalizedPath)
        val criteria = where(TNode::projectId).isEqualTo(projectId)
            .and(TNode::repoName).isEqualTo(repoName)
            .and(TNode::deleted).isEqualTo(null)
            .orOperator(
                where(TNode::fullPath).regex("^$escapedPath"),
                where(TNode::fullPath).isEqualTo(normalizedFullPath)
            )
        val query = Query(criteria)
        val deleteTime = LocalDateTime.now()
        try {
            val updateResult = nodeDao.updateMulti(query, NodeQueryHelper.nodeDeleteUpdate(operator, deleteTime))
            deletedNum = updateResult.modifiedCount
            deletedSize = nodeBaseService.aggregateComputeSize(criteria.and(TNode::deleted).isEqualTo(deleteTime))
            quotaService.decreaseUsedVolume(projectId, repoName, deletedSize)
            // 更新父目录的修改信息
            val parentFullPath = PathUtils.toFullPath(PathUtils.resolveParent(fullPath))
            nodeBaseService.updateModifiedInfo(projectId, repoName, parentFullPath, operator, deleteTime)
            publishEvent(buildDeletedEvent(projectId, repoName, fullPath, operator))
        } catch (exception: DuplicateKeyException) {
            logger.warn("Delete node[/$projectId/$repoName$fullPath] by [$operator] error: [${exception.message}]")
        }
        logger.info(
            "Delete node[/$projectId/$repoName$fullPath] by [$operator] success." +
                "$deletedNum nodes have been deleted. The size is ${HumanReadable.size(deletedSize)}"
        )
        return NodeDeleteResult(deletedNum, deletedSize, deleteTime)
    }

    override fun deleteByPaths(
        projectId: String,
        repoName: String,
        fullPaths: List<String>,
        operator: String
    ): NodeDeleteResult {
        var deletedSize = 0L
        var deletedNum = 0L
        val normalizedFullPaths = fullPaths.map { PathUtils.normalizeFullPath(it) }
        val orOperation = mutableListOf(
            where(TNode::fullPath).inValues(normalizedFullPaths)
        )
        normalizedFullPaths.forEach {
            val normalizedPath = PathUtils.toPath(it)
            val escapedPath = PathUtils.escapeRegex(normalizedPath)
            orOperation.add(where(TNode::fullPath).regex("^$escapedPath"))
        }
        val criteria = where(TNode::projectId).isEqualTo(projectId)
            .and(TNode::repoName).isEqualTo(repoName)
            .and(TNode::deleted).isEqualTo(null)
            .orOperator(*orOperation.toTypedArray())
        val query = Query(criteria)
        val deleteTime = LocalDateTime.now()
        try {
            val updateResult = nodeDao.updateMulti(query, NodeQueryHelper.nodeDeleteUpdate(operator, deleteTime))
            deletedNum = updateResult.modifiedCount
            deletedSize = nodeBaseService.aggregateComputeSize(criteria.and(TNode::deleted).isEqualTo(deleteTime))
            quotaService.decreaseUsedVolume(projectId, repoName, deletedSize)
            publishEvent(buildDeletedEvent(projectId, repoName, fullPaths, operator))
        } catch (exception: DuplicateKeyException) {
            logger.warn("Delete node[/$projectId/$repoName$fullPaths] by [$operator] error: [${exception.message}]")
        }
        logger.info(
            "Delete node[/$projectId/$repoName$fullPaths] by [$operator] success." +
                "$deletedNum nodes have been deleted. The size is ${HumanReadable.size(deletedSize)}"
        )
        return NodeDeleteResult(deletedNum, deletedSize, deleteTime)
    }

    override fun deleteBeforeDate(
        projectId: String,
        repoName: String,
        date: LocalDateTime,
        operator: String
    ): NodeDeleteResult {
        var deletedSize = 0L
        var deletedNum = 0L
        val option = NodeListOption(includeFolder = false, deep = true)
        val criteria = NodeQueryHelper.nodeListCriteria(projectId, repoName, PathUtils.ROOT, option)
            .and(TNode::createdDate).lt(date)
        val query = Query(criteria)
        val deleteTime = LocalDateTime.now()
        try {
            val parentFullPaths = nodeDao.find(query)
                .map { PathUtils.toFullPath(PathUtils.resolveParent(it.fullPath)) }
                .distinct()
                .filterNot { PathUtils.isRoot(it) }
            val updateResult = nodeDao.updateMulti(query, NodeQueryHelper.nodeDeleteUpdate(operator, deleteTime))
            deletedNum = updateResult.modifiedCount
            deletedSize = nodeBaseService.aggregateComputeSize(criteria.and(TNode::deleted).isEqualTo(deleteTime))
            quotaService.decreaseUsedVolume(projectId, repoName, deletedSize)
            // 批量更新上层目录的修改信息
            val parentNodeQuery = NodeQueryHelper.nodeQuery(projectId, repoName, parentFullPaths)
            val parentNodeUpdate = NodeQueryHelper.update(operator)
            nodeDao.updateMulti(parentNodeQuery, parentNodeUpdate)
        } catch (exception: DuplicateKeyException) {
            logger.warn("Delete node[/$projectId/$repoName] created before $date error: [${exception.message}]")
        }
        logger.info(
            "Delete node [/$projectId/$repoName] created before $date by [$operator] success. " +
                "$deletedNum nodes have been deleted. The size is ${HumanReadable.size(deletedSize)}"
        )
        return NodeDeleteResult(deletedNum, deletedSize, deleteTime)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NodeDeleteSupport::class.java)
    }
}
