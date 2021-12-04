package com.example.draguosiscoroutines.tasks

import com.example.draguosiscoroutines.GitHubService
import com.example.draguosiscoroutines.RequestData
import com.example.draguosiscoroutines.User
import com.example.draguosiscoroutines.logRepos
import com.example.draguosiscoroutines.logUsers
import retrofit2.Response

fun loadContributorsBlocking(service: GitHubService, req: RequestData) : List<User> {
    val repos = service
        .getOrgReposCall(req.org)
        .execute() // Executes request and blocks the current thread
        .also { logRepos(req, it) }
        .body() ?: listOf()

    return repos.flatMap { repo ->
        service
            .getRepoContributorsCall(req.org, repo.name)
            .execute() // Executes request and blocks the current thread
            .also { logUsers(repo, it) }
            .bodyList()
    }.aggregate()
}

fun <T> Response<List<T>>.bodyList(): List<T> {
    return body() ?: listOf()
}