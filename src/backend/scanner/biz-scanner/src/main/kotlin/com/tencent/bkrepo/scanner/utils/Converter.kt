/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2022 THL A29 Limited, a Tencent company.  All rights reserved.
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

package com.tencent.bkrepo.scanner.utils

import com.tencent.bkrepo.common.api.exception.ErrorCodeException
import com.tencent.bkrepo.common.api.message.CommonMessageCode
import com.tencent.bkrepo.common.api.util.readJsonString
import com.tencent.bkrepo.common.query.enums.OperationType
import com.tencent.bkrepo.common.query.model.Rule
import com.tencent.bkrepo.common.query.model.Rule.NestedRule
import com.tencent.bkrepo.common.scanner.pojo.scanner.Scanner
import com.tencent.bkrepo.common.scanner.pojo.scanner.binauditor.BinAuditorScanExecutorResult
import com.tencent.bkrepo.common.scanner.pojo.scanner.binauditor.BinAuditorScanner
import com.tencent.bkrepo.common.scanner.pojo.scanner.utils.LEVEL_CRITICAL
import com.tencent.bkrepo.common.scanner.pojo.scanner.utils.LEVEL_HIGH
import com.tencent.bkrepo.common.scanner.pojo.scanner.utils.LEVEL_LOW
import com.tencent.bkrepo.common.scanner.pojo.scanner.utils.LEVEL_MID
import com.tencent.bkrepo.scanner.model.TScanPlan
import com.tencent.bkrepo.scanner.model.TScanTask
import com.tencent.bkrepo.scanner.model.TSubScanTask
import com.tencent.bkrepo.scanner.pojo.ScanPlan
import com.tencent.bkrepo.scanner.pojo.ScanStatus
import com.tencent.bkrepo.scanner.pojo.ScanTask
import com.tencent.bkrepo.scanner.pojo.ScanTaskStatus
import com.tencent.bkrepo.scanner.pojo.SubScanTask
import com.tencent.bkrepo.scanner.pojo.request.CreateScanPlanRequest
import com.tencent.bkrepo.scanner.pojo.request.UpdateScanPlanRequest
import com.tencent.bkrepo.scanner.pojo.response.ScanPlanBase
import com.tencent.bkrepo.scanner.pojo.response.ScanPlanInfo
import com.tencent.bkrepo.scanner.pojo.rule.ArtifactRule
import com.tencent.bkrepo.scanner.pojo.rule.RuleArtifact
import com.tencent.bkrepo.scanner.pojo.rule.RuleType
import java.time.format.DateTimeFormatter

object Converter {
    fun convert(subScanTask: TSubScanTask, scanner: Scanner): SubScanTask = with(subScanTask) {
        SubScanTask(
            taskId = id!!,
            parentScanTaskId = parentScanTaskId,
            scanner = scanner,
            sha256 = sha256,
            size = size,
            credentialsKey = credentialsKey
        )
    }

    fun convert(scanTask: TScanTask): ScanTask = with(scanTask) {
        ScanTask(
            taskId = id!!,
            createdBy = createdBy,
            triggerDateTime = createdDate.format(DateTimeFormatter.ISO_DATE_TIME),
            startDateTime = startDateTime?.format(DateTimeFormatter.ISO_DATE_TIME),
            finishedDateTime = finishedDateTime?.format(DateTimeFormatter.ISO_DATE_TIME),
            status = status,
            rule = scanTask.rule?.readJsonString(),
            total = total,
            scanning = scanning,
            failed = failed,
            scanned = scanned,
            scanner = scanner,
            scannerType = scannerType,
            scannerVersion = scannerVersion,
            scanResultOverview = scanResultOverview
        )
    }

    fun convert(scanPlan: TScanPlan): ScanPlan {
        return with(scanPlan) {
            ScanPlan(
                projectId = projectId,
                name = name,
                type = type,
                scanner = scanner,
                description = description,
                scanOnNewArtifact = scanOnNewArtifact,
                repoNames = repoNames,
                rule = rule?.readJsonString(),
                createdBy = createdBy,
                createdDate = createdDate.format(DateTimeFormatter.ISO_DATE_TIME),
                lastModifiedBy = lastModifiedBy,
                lastModifiedDate = lastModifiedDate.format(DateTimeFormatter.ISO_DATE_TIME)
            )
        }
    }

    fun convert(scanPlan: ScanPlan): ScanPlanBase {
        return with(scanPlan) {
            ScanPlanBase(
                id = id!!,
                name = name,
                type = type!!,
                scanner = scanner!!,
                description = description!!,
                projectId = projectId!!,
                autoScan = scanOnNewArtifact!!,
                scanOnNewArtifact = scanOnNewArtifact!!,
                repoNameList = repoNames!!,
                repoNames = repoNames!!,
                artifactRules = rule?.let { convert(it) } ?: emptyList(),
                rule = rule,
                createdBy = createdBy!!,
                createdDate = createdDate!!,
                lastModifiedBy = lastModifiedBy!!,
                lastModifiedDate = lastModifiedDate!!
            )
        }
    }

    fun convert(scanPlanRequest: UpdateScanPlanRequest): ScanPlan {
        return with(scanPlanRequest) {
            ScanPlan(
                id = id,
                projectId = projectId,
                name = name,
                description = description,
                scanOnNewArtifact = autoScan,
                repoNames = repoNameList,
                rule = artifactRules?.let { convert(it) }
            )
        }
    }

    fun convert(scanPlanRequest: CreateScanPlanRequest): ScanPlan {
        return with(scanPlanRequest) {
            ScanPlan(
                projectId = projectId,
                name = name,
                type = type,
                scanner = scanner,
                description = description,
                scanOnNewArtifact = autoScan,
                repoNames = repoNameList,
                rule = convert(artifactRules)
            )
        }
    }

    fun convert(scanPlan: TScanPlan, latestScanTask: TScanTask?): ScanPlanInfo {
        with(scanPlan) {
            val critical = latestScanTask?.let { getCveCount(LEVEL_CRITICAL, latestScanTask) } ?: 0L
            val high = latestScanTask?.let { getCveCount(LEVEL_HIGH, latestScanTask) } ?: 0L
            val medium = latestScanTask?.let { getCveCount(LEVEL_MID, latestScanTask) } ?: 0L
            val low = latestScanTask?.let { getCveCount(LEVEL_LOW, latestScanTask) } ?: 0L
            val artifactCount = latestScanTask?.total ?: 0L
            val status = latestScanTask?.let { convertScanTaskStatus(it.status).name } ?: ScanStatus.INIT.name

            return ScanPlanInfo(
                id = id!!,
                name = name,
                planType = type,
                projectId = projectId,
                status = status,
                artifactCount = artifactCount,
                critical = critical,
                high = high,
                medium = medium,
                low = low,
                total = critical + high + medium + low,
                createdBy = createdBy,
                createdDate = createdDate.format(DateTimeFormatter.ISO_DATE_TIME),
                lastModifiedBy = lastModifiedBy,
                lastModifiedDate = lastModifiedDate.format(DateTimeFormatter.ISO_DATE_TIME),
                lastScanDate = latestScanTask?.startDateTime?.format(DateTimeFormatter.ISO_DATE_TIME)
            )
        }
    }

    private fun convertScanTaskStatus(status: String?): ScanStatus {
        return when (status) {
            ScanTaskStatus.PENDING.name -> ScanStatus.INIT
            ScanTaskStatus.SCANNING_SUBMITTING.name, ScanTaskStatus.SCANNING_SUBMITTED.name -> ScanStatus.RUNNING
            ScanTaskStatus.PAUSE.name, ScanTaskStatus.STOPPED.name -> ScanStatus.STOP
            ScanTaskStatus.FINISHED.name -> ScanStatus.SUCCESS
            else -> throw ErrorCodeException(CommonMessageCode.PARAMETER_INVALID, status.toString())
        }
    }

    private fun getCveCount(level: String, scanTask: TScanTask): Long {
        if (scanTask.scannerType == BinAuditorScanner.TYPE) {
            val key = BinAuditorScanExecutorResult.overviewKeyOfCve(level)
            return scanTask.scanResultOverview?.get(key) ?: 0L
        }

        return 0L
    }

    private fun convert(rule: Rule): List<ArtifactRule> {
        if (rule is NestedRule && rule.relation == NestedRule.RelationType.OR) {
            return rule.rules.map { artifactRule(it) }
        } else if (rule is NestedRule && rule.relation == NestedRule.RelationType.AND || rule is Rule.QueryRule) {
            return listOf(artifactRule(rule))
        }

        throw ErrorCodeException(CommonMessageCode.PARAMETER_INVALID, rule)
    }

    private fun convert(rules: List<ArtifactRule>): Rule? {
        if (rules.isEmpty()) {
            return null
        }

        if (rules.size == 1) {
            return rule(rules[0])
        }

        return rules
            .asSequence()
            .filter { it.versionRule != null || it.nameRule != null }
            .map { rule -> rule(rule) }
            .run { NestedRule(this.toMutableList(), NestedRule.RelationType.OR) }
    }

    private fun rule(artifactRule: ArtifactRule): Rule {
        val nameRule = artifactRule.nameRule?.let { convertRule(RuleArtifact::name.name, it) }
        val versionRule = artifactRule.versionRule?.let { convertRule(RuleArtifact::version.name, it) }
        return rule(nameRule, versionRule)
    }

    private fun rule(nameRule: Rule?, versionRule: Rule?): Rule {
        require(nameRule != null || versionRule != null)
        if (nameRule == null) {
            return versionRule!!
        }

        if (versionRule == null) {
            return nameRule
        }

        return NestedRule(mutableListOf(nameRule, versionRule), NestedRule.RelationType.AND)
    }

    private fun artifactRule(rule: Rule): ArtifactRule {
        require(rule is Rule.QueryRule || rule is NestedRule && rule.relation == NestedRule.RelationType.AND)

        return ArtifactRule(
            findRuleFrom(rule, RuleArtifact::name.name),
            findRuleFrom(rule, RuleArtifact::version.name)
        )
    }

    private fun findRuleFrom(rule: Rule, filed: String): com.tencent.bkrepo.scanner.pojo.rule.Rule? {
        require(rule is Rule.QueryRule || rule is NestedRule && rule.relation == NestedRule.RelationType.AND)

        return if (rule is Rule.QueryRule && rule.field == filed) {
            convertRule(rule)
        } else if (rule is NestedRule && rule.relation == NestedRule.RelationType.AND) {
            rule.rules
                .firstOrNull { it is Rule.QueryRule && it.field == filed }
                ?.let { convertRule(it) }
        } else {
            null
        }
    }

    private fun convertRule(rule: Rule): com.tencent.bkrepo.scanner.pojo.rule.Rule {
        require(rule is Rule.QueryRule)

        return com.tencent.bkrepo.scanner.pojo.rule.Rule(
            convertRuleOperationType(rule.operation),
            rule.value.toString()
        )
    }

    private fun convertRule(field: String, rule: com.tencent.bkrepo.scanner.pojo.rule.Rule): Rule.QueryRule {
        return Rule.QueryRule(field, rule.value, convertRuleType(rule.type))
    }

    private fun convertRuleType(type: RuleType): OperationType {
        return when (type) {
            RuleType.EQ -> OperationType.EQ
            RuleType.IN -> OperationType.IN
            RuleType.REGEX -> OperationType.MATCH
        }
    }

    private fun convertRuleOperationType(type: OperationType): RuleType {
        return when (type) {
            OperationType.EQ -> RuleType.EQ
            OperationType.MATCH -> RuleType.REGEX
            OperationType.IN -> RuleType.IN
            else -> throw ErrorCodeException(CommonMessageCode.PARAMETER_INVALID, type)
        }
    }
}
