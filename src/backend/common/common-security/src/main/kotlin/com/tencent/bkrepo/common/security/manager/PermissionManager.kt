package com.tencent.bkrepo.common.security.manager

import com.tencent.bkrepo.auth.api.ServicePermissionResource
import com.tencent.bkrepo.auth.api.ServiceUserResource
import com.tencent.bkrepo.auth.pojo.CheckPermissionRequest
import com.tencent.bkrepo.auth.pojo.enums.PermissionAction
import com.tencent.bkrepo.auth.pojo.enums.ResourceType
import com.tencent.bkrepo.common.api.constant.ANONYMOUS_USER
import com.tencent.bkrepo.common.api.constant.PLATFORM_KEY
import com.tencent.bkrepo.common.artifact.exception.ArtifactNotFoundException
import com.tencent.bkrepo.common.security.exception.AccessDeniedException
import com.tencent.bkrepo.common.security.exception.AuthenticationException
import com.tencent.bkrepo.common.security.http.HttpAuthProperties
import com.tencent.bkrepo.common.security.permission.PrincipalType
import com.tencent.bkrepo.common.service.util.HttpContextHolder
import com.tencent.bkrepo.repository.api.RepositoryClient
import com.tencent.bkrepo.repository.pojo.repo.RepositoryInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 权限管理类
 */
@Component
class PermissionManager(
    private val repositoryClient: RepositoryClient,
    private val permissionResource: ServicePermissionResource,
    private val userResource: ServiceUserResource,
    private val httpAuthProperties: HttpAuthProperties
) {

    fun checkPermission(userId: String, type: ResourceType, action: PermissionAction, repositoryInfo: RepositoryInfo) {
        if (preCheck()) return
        checkRepoPermission(userId, type, action, repositoryInfo)
    }

    fun checkPermission(userId: String, type: ResourceType, action: PermissionAction, projectId: String, repoName: String? = null) {
        if (preCheck()) return
        if (type == ResourceType.PROJECT) {
            checkProjectPermission(userId, type, action, projectId)
        } else {
            val repositoryInfo = queryRepositoryInfo(projectId, repoName!!)
            checkRepoPermission(userId, type, action, repositoryInfo)
        }
    }

    fun checkPrincipal(userId: String, principalType: PrincipalType) {
        if (!httpAuthProperties.enabled) {
            if (logger.isDebugEnabled) {
                logger.debug("Auth disabled, skip checking principal")
            }
            return
        }
        // 匿名用户，提示登录
        if (userId == ANONYMOUS_USER) throw AuthenticationException()
        if (principalType == PrincipalType.ADMIN) {
            if (!isAdminUser(userId)) {
                throw AccessDeniedException()
            }
        } else if (principalType == PrincipalType.PLATFORM) {
            if (!isPlatformUser() && !isAdminUser(userId)) {
                throw AccessDeniedException()
            }
        }
    }

    private fun preCheck(): Boolean {
        return if (!httpAuthProperties.enabled) {
            if (logger.isDebugEnabled) {
                logger.debug("Auth disabled, skip checking permission")
            }
            true
        } else false
    }

    private fun queryRepositoryInfo(projectId: String, repoName: String): RepositoryInfo {
        val response = repositoryClient.detail(projectId, repoName)
        return response.data ?: throw ArtifactNotFoundException("Repository[$repoName] not found")
    }

    private fun checkRepoPermission(userId: String, type: ResourceType, action: PermissionAction, repositoryInfo: RepositoryInfo) {
        // public仓库且为READ操作，直接跳过
        if (type == ResourceType.REPO && action == PermissionAction.READ && repositoryInfo.public) return
        val appId = HttpContextHolder.getRequest().getAttribute(PLATFORM_KEY) as? String
        // 匿名用户，提示登录
        if (userId == ANONYMOUS_USER && appId == null) throw AuthenticationException()
        // auth 校验
        with(repositoryInfo) {
            val checkRequest = CheckPermissionRequest(userId, type, action, projectId, name, null, null, appId)
            checkPermission(checkRequest)
        }
    }

    private fun checkProjectPermission(userId: String, type: ResourceType, action: PermissionAction, projectId: String) {
        val checkRequest = CheckPermissionRequest(userId, type, action, projectId)
        checkPermission(checkRequest)
    }

    private fun checkPermission(checkRequest: CheckPermissionRequest) {
        if (permissionResource.checkPermission(checkRequest).data != true) {
            throw AccessDeniedException()
        }
    }

    private fun isPlatformUser(): Boolean {
        return HttpContextHolder.getRequest().getAttribute(PLATFORM_KEY) != null
    }

    private fun isAdminUser(userId: String): Boolean {
        return userResource.detail(userId).data?.admin == true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PermissionManager::class.java)
    }
}
