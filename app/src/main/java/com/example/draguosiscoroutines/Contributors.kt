package com.example.draguosiscoroutines

import android.net.wifi.p2p.WifiP2pManager
import com.example.draguosiscoroutines.Contributors.*
import kotlinx.coroutines.*
import com.example.draguosiscoroutines.Variant.*
import com.example.draguosiscoroutines.LoadingStatus.*
import com.example.draguosiscoroutines.tasks.loadContributorsBackground
import com.example.draguosiscoroutines.tasks.loadContributorsBlocking
import com.example.draguosiscoroutines.tasks.loadContributorsCallbacks
import com.example.draguosiscoroutines.tasks.loadContributorsChannels
import com.example.draguosiscoroutines.tasks.loadContributorsConcurrent
import com.example.draguosiscoroutines.tasks.loadContributorsNotCancellable
import com.example.draguosiscoroutines.tasks.loadContributorsProgress
import com.example.draguosiscoroutines.tasks.loadContributorsSuspend
import kotlin.coroutines.CoroutineContext

interface Contributors : CoroutineScope {

    val job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    fun loadContributors() {
        val (username, password, org, _) = getAllParams()
        val req = RequestData(username, password, org)

        clearResults()
        val service = createGitHubService(req.username, req.password)

        val startTime = System.currentTimeMillis()
        when (getSelectedVariant()) {
            BLOCKING -> { // Blocking UI thread
                val users = loadContributorsBlocking(service, req)
                updateResults(users, startTime)
            }
            BACKGROUND -> { // Blocking a background thread
                loadContributorsBackground(service, req) { users ->
                    runOnActivityUiThread {
                        updateResults(users, startTime)
                    }
                }
            }
            CALLBACKS -> { // Using callbacks
                loadContributorsCallbacks(service, req) { users ->
                    runOnActivityUiThread {
                        updateResults(users, startTime)
                    }
                }
            }
            SUSPEND -> { // Using coroutines
                launch {
                    val users = loadContributorsSuspend(service, req)
                    updateResults(users, startTime)
                }.setUpCancellation()
            }
            CONCURRENT -> { // Performing requests concurrently
                launch {
                    val users = loadContributorsConcurrent(service, req)
                    launch(Dispatchers.Main) {
                        updateResults(users, startTime)
                    }
                }.setUpCancellation()
            }
            NOT_CANCELLABLE -> { // Performing requests in a non-cancellable way
                launch {
                    val users = loadContributorsNotCancellable(service, req)
                    updateResults(users, startTime)
                }.setUpCancellation()
            }
            PROGRESS -> { // Showing progress
                launch(Dispatchers.Default) {
                    loadContributorsProgress(service, req) { users, completed ->
                        withContext(Dispatchers.Main) {
                            updateResults(users, startTime, completed)
                        }
                    }
                }.setUpCancellation()
            }
            CHANNELS -> {  // Performing requests concurrently and showing progress
                launch(Dispatchers.Default) {
                    loadContributorsChannels(service, req) { users, completed ->
                        withContext(Dispatchers.Main) {
                            updateResults(users, startTime, completed)
                        }
                    }
                }.setUpCancellation()
            }
        }
    }

    private fun clearResults() {
        updateContributors(listOf())
        updateLoadingStatus(IN_PROGRESS)
        setActionsStatus(newLoadingEnabled = false)
    }

    private fun updateResults(
        users: List<User>,
        startTime: Long,
        completed: Boolean = true
    ) {
        updateContributors(users)
        updateLoadingStatus(if (completed) COMPLETED else IN_PROGRESS, startTime)
        if (completed) {
            setActionsStatus(newLoadingEnabled = true)
        }
    }

    private fun updateLoadingStatus(
        status: LoadingStatus,
        startTime: Long? = null
    ) {
        val time = if (startTime != null) {
            val time = System.currentTimeMillis() - startTime
            "${(time / 1000)}.${time % 1000 / 100} sec"
        } else ""

        val text = "Loading status: " +
            when (status) {
                COMPLETED -> "completed in $time"
                IN_PROGRESS -> "in progress $time"
                CANCELED -> "canceled"
            }
        setLoadingStatus(text, status == IN_PROGRESS)
    }

    private fun Job.setUpCancellation() {
        // make active the 'cancel' button
        setActionsStatus(newLoadingEnabled = false, cancellationEnabled = true)

        val loadingJob = this

        // cancel the loading job if the 'cancel' button was clicked
        val listener = {
            loadingJob.cancel()
            updateLoadingStatus(CANCELED)
        }
        addCancelListener(listener)

        // update the status and remove the listener after the loading job is completed
        launch {
            loadingJob.join()
            setActionsStatus(newLoadingEnabled = true)
            removeCancelListener()
        }
    }

    fun cancelJob() {
        job.cancel()
    }

    fun runOnActivityUiThread(something: () -> Unit)

    fun getSelectedVariant(): Variant

    fun getAllParams(): Params

    fun updateContributors(users: List<User>)

    fun setLoadingStatus(text: String, iconRunning: Boolean)

    fun setActionsStatus(newLoadingEnabled: Boolean, cancellationEnabled: Boolean = false)

    fun addCancelListener(listener: () -> Unit)

    fun removeCancelListener()
}

enum class LoadingStatus { COMPLETED, CANCELED, IN_PROGRESS }
