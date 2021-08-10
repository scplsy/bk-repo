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

package com.tencent.bkrepo.opdata.model

import com.tencent.bkrepo.opdata.constant.OPDATA_PROJECT_ID
import com.tencent.bkrepo.opdata.constant.OPDATA_PROJECT_NAME
import com.tencent.bkrepo.opdata.constant.OPDATA_REPOSITORY
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Service

@Service
class RepoModel @Autowired constructor(
    private var mongoTemplate: MongoTemplate
) {

    fun getRepoListByProjectId(projectId: String): List<String> {
        val query = Query(
            Criteria.where(OPDATA_PROJECT_ID).`is`(projectId)
        )
        val data = mutableListOf<String>()
        val results = mongoTemplate.find(query, MutableMap::class.java, OPDATA_REPOSITORY)
        results.forEach {
            val repoName = it[OPDATA_PROJECT_NAME] as String
            data.add(repoName)
        }
        return data
    }

    fun getProjectAndRepoListByStorageCredentialId(
        storageCredentialId: String
    ): MutableMap<String, MutableList<String>> {
        val query = Query(
            Criteria.where("credentialsKey").isEqualTo(storageCredentialId)
        )
        val data = mutableMapOf<String, MutableList<String>>()
        val results = mongoTemplate.find(query, MutableMap::class.java, OPDATA_REPOSITORY)
        results.forEach {
            val projectId = it[OPDATA_PROJECT_ID] as String
            val repoName = it["name"] as String
            if (data.containsKey(projectId)) {
                data[projectId]!!.add(repoName)
            } else {
                data[projectId] = mutableListOf(repoName)
            }
        }
        return data
    }
}
