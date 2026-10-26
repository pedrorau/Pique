package com.pedrorau.pique.knots

import com.pedrorau.pique.resources.Res
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** Loads the bundled knots once and keeps them in memory; the content never changes at runtime. */
object KnotRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cache: List<Knot>? = null

    @OptIn(ExperimentalResourceApi::class)
    suspend fun all(): List<Knot> = mutex.withLock {
        cache ?: run {
            val index = json.decodeFromString<KnotIndex>(Res.readBytes("files/knots/index.json").decodeToString())
            index.knots
                .map { id -> json.decodeFromString<Knot>(Res.readBytes("files/knots/$id.json").decodeToString()) }
                .also { cache = it }
        }
    }

    suspend fun byId(id: String): Knot? = all().firstOrNull { it.id == id }
}
