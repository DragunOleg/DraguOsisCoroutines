package com.example.draguosiscoroutines.tasks

import com.example.draguosiscoroutines.GitHubService
import com.example.draguosiscoroutines.RequestData
import com.example.draguosiscoroutines.User
import com.example.draguosiscoroutines.log
import com.example.draguosiscoroutines.logRepos
import com.example.draguosiscoroutines.logUsers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun loadContributorsCallbacks(
    service: GitHubService,
    req: RequestData,
    updateResults: (List<User>) -> Unit
) {
    service.getOrgReposCall(req.org).onResponse { responseRepos ->
        logRepos(req, responseRepos)
        val repos = responseRepos.bodyList()
        val allUsers = mutableListOf<User>()
        var reposCounter = repos.size
        repos.forEach { repo ->
            service.getRepoContributorsCall(req.org, repo.name).onResponse { responseUsers ->
                reposCounter--
                logUsers(repo, responseUsers)
                val users = responseUsers.bodyList()
                allUsers += users
                if (reposCounter == 0) updateResults(allUsers.aggregate())

            }
        }
    }
}


inline fun <T> Call<T>.onResponse(crossinline callback: (Response<T>) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            callback(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            log.error("Call failed", t)
        }
    })
}
