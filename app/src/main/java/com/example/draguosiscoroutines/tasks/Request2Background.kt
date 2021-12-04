package com.example.draguosiscoroutines.tasks

import com.example.draguosiscoroutines.GitHubService
import com.example.draguosiscoroutines.RequestData
import com.example.draguosiscoroutines.User
import kotlin.concurrent.thread

fun loadContributorsBackground(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    thread {
        updateResults(loadContributorsBlocking(service, req))
    }
}