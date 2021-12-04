package com.example.draguosiscoroutines.tasks

import com.example.draguosiscoroutines.User

fun List<User>.aggregate(): List<User> =
    groupBy { it.login }
        .map { (login, group) -> User(login, group.sumOf { it.contributions }) }
        .sortedByDescending { it.contributions }