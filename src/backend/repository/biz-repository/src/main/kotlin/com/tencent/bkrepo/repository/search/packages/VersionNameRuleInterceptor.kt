/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2024 THL A29 Limited, a Tencent company.  All rights reserved.
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

package com.tencent.bkrepo.repository.search.packages

import com.tencent.bkrepo.common.api.exception.ErrorCodeException
import com.tencent.bkrepo.common.api.message.CommonMessageCode
import com.tencent.bkrepo.common.query.enums.OperationType
import com.tencent.bkrepo.common.query.model.Rule
import com.tencent.bkrepo.repository.dao.PackageVersionDao
import com.tencent.bkrepo.repository.model.TPackageVersion
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component

/**
 * 版本号规则拦截器
 */
@Component
class VersionNameRuleInterceptor(
    override val packageVersionDao: PackageVersionDao
) : VersionRuleInterceptor(packageVersionDao) {

    override fun match(rule: Rule): Boolean {
        return rule is Rule.QueryRule && rule.field == "version"
    }

    override fun getVersionCriteria(rule: Rule, context: PackageQueryContext): Criteria {
        with(rule as Rule.QueryRule) {
            val versionQueryRule = when (operation) {
                OperationType.IN ->
                    rule.copy(field = TPackageVersion::name.name, value = (value as List<*>).map { it.toString() })

                in SUPPORT_OPERATIONS -> rule.copy(field = TPackageVersion::name.name, value = value.toString())
                else -> throw ErrorCodeException(
                    CommonMessageCode.METHOD_NOT_ALLOWED,
                    "$field only support ${SUPPORT_OPERATIONS.map { it.name }} operation type."
                )
            }.toFixed()
            return context.interpreter.resolveRule(versionQueryRule, context)
        }
    }

    companion object {
        private val SUPPORT_OPERATIONS = arrayOf(
            OperationType.EQ,
            OperationType.IN,
            OperationType.MATCH,
            OperationType.MATCH_I
        )
    }
}
