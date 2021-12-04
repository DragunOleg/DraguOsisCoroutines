package com.example.draguosiscoroutines.tasks

import com.example.draguosiscoroutines.GitHubService
import com.example.draguosiscoroutines.RequestData
import com.example.draguosiscoroutines.User
import com.example.draguosiscoroutines.logRepos
import com.example.draguosiscoroutines.logUsers

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    var allUsers = emptyList<User>()
    for ((index, repo) in repos.withIndex()) {
        val users = service.getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()

        allUsers = (allUsers + users).aggregate()
        updateResults(allUsers, index == repos.lastIndex)
    }
}
