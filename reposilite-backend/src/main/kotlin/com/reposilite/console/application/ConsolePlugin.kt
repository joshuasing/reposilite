/*
 * Copyright (c) 2022 dzikoysk
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
package com.reposilite.console.application

import com.reposilite.configuration.shared.SharedConfigurationFacade
import com.reposilite.console.HelpCommand
import com.reposilite.console.LevelCommand
import com.reposilite.console.StopCommand
import com.reposilite.console.api.CommandsSetupEvent
import com.reposilite.console.infrastructure.CliEndpoint
import com.reposilite.console.infrastructure.ConsoleEndpoint
import com.reposilite.plugin.api.Facade
import com.reposilite.plugin.api.Plugin
import com.reposilite.plugin.api.ReposiliteDisposeEvent
import com.reposilite.plugin.api.ReposiliteInitializeEvent
import com.reposilite.plugin.api.ReposilitePlugin
import com.reposilite.plugin.api.ReposiliteStartedEvent
import com.reposilite.plugin.event
import com.reposilite.plugin.facade
import com.reposilite.plugin.parameters
import com.reposilite.plugin.reposilite
import com.reposilite.web.api.HttpServerInitializationEvent
import com.reposilite.web.api.RoutingSetupEvent
import com.reposilite.web.application.WebSettings

@Plugin(name = "console", dependencies = [ "shared-configuration", "failure", "access-token", "authentication" ])
internal class ConsolePlugin : ReposilitePlugin() {

    override fun initialize(): Facade {
        val sharedConfigurationFacade = facade<SharedConfigurationFacade>()
        val consoleFacade = ConsoleComponents(this, facade()).consoleFacade()

        event { _: ReposiliteInitializeEvent ->
            consoleFacade.registerCommand(HelpCommand(consoleFacade))
            consoleFacade.registerCommand(LevelCommand(reposilite().journalist))
            consoleFacade.registerCommand(StopCommand(reposilite()))

            val setup = extensions().emitEvent(CommandsSetupEvent())
            setup.getCommands().forEach { consoleFacade.registerCommand(it) }

            // disable console daemon in tests due to issues with coverage and interrupt method call
            // https://github.com/jacoco/jacoco/issues/1066
            if (!parameters().testEnv) {
                consoleFacade.commandExecutor.hook()
            }
        }

        event { event: RoutingSetupEvent ->
            event.registerRoutes(ConsoleEndpoint(consoleFacade))
        }

        event { event: HttpServerInitializationEvent ->
            event.javalin.ws(
                "/api/console/sock",
                CliEndpoint(
                    journalist = reposilite().journalist,
                    accessTokenFacade = facade(),
                    authenticationFacade = facade(),
                    consoleFacade = consoleFacade,
                    forwardedIp = sharedConfigurationFacade.getDomainSettings<WebSettings>().computed { it.forwardedIp }
                )
            )
        }

        if (!parameters().testEnv) {
            event { _: ReposiliteStartedEvent ->
                reposilite().ioService.execute {
                    consoleFacade.executeCommand("help")
                    logger.info("")
                    logger.info("Collecting status metrics...")
                    logger.info("")
                    consoleFacade.executeCommand("status")
                    logger.info("")
                }
            }
        }

        event { _: ReposiliteDisposeEvent ->
            consoleFacade.commandExecutor.stop()
        }

        return consoleFacade
    }

}
