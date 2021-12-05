package com.example.draguosiscoroutines.tasks

import com.example.draguosiscoroutines.GitHubService
import com.example.draguosiscoroutines.RequestData
import com.example.draguosiscoroutines.User
import com.example.draguosiscoroutines.logRepos
import com.example.draguosiscoroutines.logUsers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannelsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, size: Int, currentItems: Int, completed: Boolean) -> Unit
) {
    coroutineScope {
        val channel = Channel<List<User>>()
        var allUsers = emptyList<User>()

        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .bodyList()

        repos.forEach { repo ->
            launch {
                val users = service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(users)
            }
        }

        repeat(repos.size) {
            val users = channel.receive()
            allUsers = (allUsers + users).aggregate()
            updateResults(allUsers, repos.size, it, it == repos.lastIndex)
        }
    }
}
