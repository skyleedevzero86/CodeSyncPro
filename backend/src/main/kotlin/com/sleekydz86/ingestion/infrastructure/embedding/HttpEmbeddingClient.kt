package com.sleekydz86.ingestion.infrastructure.embedding

import com.sleekydz86.ingestion.domain.model.SourceConfig
import com.sleekydz86.ingestion.domain.port.EmbeddingClient
import com.sleekydz86.ingestion.global.config.EmbeddingProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Component
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.concurrent.TimeUnit

@Component
class HttpEmbeddingClient(
    private val properties: EmbeddingProperties,
) : EmbeddingClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(properties.timeoutSeconds.toLong(), TimeUnit.SECONDS)
        .readTimeout(properties.timeoutSeconds.toLong(), TimeUnit.SECONDS)
        .writeTimeout(properties.timeoutSeconds.toLong(), TimeUnit.SECONDS)
        .build()

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    override suspend fun upsertDocument(
        sourceConfig: SourceConfig,
        projectPath: String,
        filePath: String,
        content: String,
        commitSha: String?,
        branchName: String,
    ) = withContext(Dispatchers.IO) {
        val body = buildMap {
            put("project_path", projectPath)
            put("file_path", filePath)
            put("content", content)
            put("commit_sha", commitSha)
            put("branch_name", branchName)
            put("source_base_url", sourceConfig.baseUrl)
        }
        val json = jacksonObjectMapper().writeValueAsString(body)
        val requestBody = json.toRequestBody(jsonType)
        val requestBuilder = Request.Builder()
            .url(properties.endpointUrl)
            .post(requestBody)
        if (properties.apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer ${properties.apiKey}")
        }
        val request = requestBuilder.build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw EmbeddingClientException(
                    "Embedding upsert failed: ${response.code} ${response.message}, body=${response.body?.string()}"
                )
            }
        }
    }
}

class EmbeddingClientException(message: String) : RuntimeException(message)
