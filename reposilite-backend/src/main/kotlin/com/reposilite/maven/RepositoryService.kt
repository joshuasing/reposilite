/*
 * Copyright (c) 2021 dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reposilite.maven

import com.reposilite.web.http.ErrorResponse
import com.reposilite.web.http.errorResponse
import io.javalin.http.HttpCode.NOT_FOUND
import net.dzikoysk.dynamiclogger.Journalist
import net.dzikoysk.dynamiclogger.Logger
import panda.std.Result
import panda.std.asSuccess

internal class RepositoryService(
    private val journalist: Journalist,
    private val repositories: Map<String, Repository>
) : Journalist {

    fun findRepository(name: String): Result<Repository, ErrorResponse> =
        getRepository(name)?.asSuccess() ?: errorResponse(NOT_FOUND, "Repository $name not found")

    fun getRepository(name: String): Repository? =
        repositories[name]

    fun getRepositories(): Collection<Repository> =
        repositories.values

    override fun getLogger(): Logger =
        journalist.logger

}