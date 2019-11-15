package com.tencent.bkrepo.docker.artifact.repomd

import com.tencent.bkrepo.docker.DockerWorkContext
import com.tencent.bkrepo.docker.repomd.Artifact
import com.tencent.bkrepo.docker.v2.helpers.DockerSearchBlobPolicy
import java.io.InputStream
import java.net.URI
import org.slf4j.LoggerFactory

class DockerPackageWorkContext() : DockerWorkContext {

    private var contextPath: String = ""
    private lateinit var contextMap: MutableMap<String, Any>

    companion object {
        private val log = LoggerFactory.getLogger(DockerPackageWorkContext::class.java)
        private val FIND_BLOBS_QUERY_LIMIT = 10
        private val READABLE_DOCKER_REPOSITORIES_LIMIT = 5
        val SHA2_PROPERTY = "sha256"
        val SHA2_FILENAME_PREFIX = "sha256__"
    }

    override fun translateRepoId(id: String): String {
        return id
    }

    override fun readGlobal(fullPath: String): InputStream {
        throw UnsupportedOperationException("NOT IMPLEMENTED")
    }

    override fun isBlobReadable(blob: Artifact): Boolean {
        return true
    }

    override fun findBlobsGlobally(digestFileName: String, searchPolicy: DockerSearchBlobPolicy): Iterable<Artifact> {
        throw UnsupportedOperationException("NOT IMPLEMENTED")
    }

    override fun cleanup(repoId: String, uploadsPath: String) {
        return
        // (ContextHelper.get().beanForType(DockerService::class.java) as DockerService).cleanup(repoId, uploadsPath)
    }

    override fun onTagPushedSuccessfully(s: String, s1: String, s2: String) {}

    override fun obtainManifestLock(repoTag: String): String {
        return ""
    }

    override fun releaseManifestLock(lockId: String, repoTag: String) {}

    override fun copy(sourcePath: String, targetPath: String): Boolean {
        throw UnsupportedOperationException("NOT IMPLEMENTED")
    }

    override fun getContextPath(): String {
        return this.contextPath
    }

    override fun getSubject(): String {
        throw UnsupportedOperationException("NOT IMPLEMENTED")
    }

    override fun getContextMap(): Map<String, Any> {
        return this.contextMap
    }

    override fun setSystem() {
    }

    override fun unsetSystem() {
    }

    override fun rewriteRepoURI(repoKey: String, uri: URI, headers: MutableSet<MutableMap.MutableEntry<String, List<String>>>): URI {
        return uri
        // return DockerInternalRewrite.rewriteBack(repoKey, uri, headers)
    }
}
