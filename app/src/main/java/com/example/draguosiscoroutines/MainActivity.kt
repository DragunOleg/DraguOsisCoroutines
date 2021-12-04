package com.example.draguosiscoroutines

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.draguosiscoroutines.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Job
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.View


class MainActivity : AppCompatActivity(), Contributors {
    private lateinit var binding: ActivityMainBinding
    override val job: Job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        transferPrefsToViewValues()
        setupSaveListeners()
        setupOtherThings()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelJob()
    }

    override fun getSelectedVariant(): Variant = getParamsFromPrefs().variant

    override fun getAllParams() = getParamsFromPrefs()

    override fun updateContributors(users: List<User>) {
        if (users.isEmpty()) log.info("Clearing result")
        (binding.recycler.adapter as RecyclerAdapter).update(users)
    }

    override fun setLoadingStatus(text: String, iconRunning: Boolean) {
        binding.progressText.text = text
        binding.progressCircular.visibility = when (iconRunning) {
            true -> View.VISIBLE
            false -> View.INVISIBLE
        }
    }

    override fun setActionsStatus(newLoadingEnabled: Boolean, cancellationEnabled: Boolean) {
        binding.bLoad.isClickable = newLoadingEnabled
        binding.bCancel.isClickable = cancellationEnabled
    }

    override fun addCancelListener(listener: () -> Unit) =
        binding.bCancel.setOnClickListener { listener.invoke() }

    override fun removeCancelListener() = binding.bCancel.setOnClickListener(null)

    override fun runOnActivityUiThread(something: () -> Unit) = runOnUiThread(something)

    private fun setupSaveListeners() {
        with(binding) {
            chipGroup.setOnCheckedChangeListener { _, _ ->
                transferViewValuesToParams()
            }
            ghUsername.addTextChangedListener {
                transferViewValuesToParams()
            }
            ghPassword.addTextChangedListener {
                transferViewValuesToParams()
            }
            organization.addTextChangedListener {
                transferViewValuesToParams()
            }
        }

    }

    private fun setupOtherThings() {
        with(binding) {
            recycler.adapter = RecyclerAdapter(listOf())
            bLoad.setOnClickListener { loadContributors() }
        }
        //need to run network on main thread
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    private fun transferViewValuesToParams() {
        saveParamsToPrefs(
            Params(
                username = binding.ghUsername.text.toString(),
                password = binding.ghPassword.text.toString(),
                org = binding.organization.text.toString(),
                variant = when (findViewById<Chip>(binding.chipGroup.checkedChipId).text) {
                    getString(R.string.blocking) -> Variant.BLOCKING
                    getString(R.string.background) -> Variant.BACKGROUND
                    getString(R.string.callbacks) -> Variant.CALLBACKS
                    getString(R.string.suspend) -> Variant.SUSPEND
                    getString(R.string.concurrent) -> Variant.CONCURRENT
                    getString(R.string.not_cancellable) -> Variant.NOT_CANCELLABLE
                    getString(R.string.progress) -> Variant.PROGRESS
                    getString(R.string.channels) -> Variant.CHANNELS
                    else -> Variant.BLOCKING
                }
            )
        )
    }

    private fun transferPrefsToViewValues() {
        getParamsFromPrefs().also {
            binding.ghUsername.setText(it.username)
            binding.ghPassword.setText(it.password)
            binding.organization.setText(it.org)
            binding.chipGroup.check(
                when (it.variant) {
                    Variant.BLOCKING -> binding.chipBlocking.id
                    Variant.BACKGROUND -> binding.chipBackground.id
                    Variant.CALLBACKS -> binding.chipCallbacks.id
                    Variant.SUSPEND -> binding.chipSuspend.id
                    Variant.CONCURRENT -> binding.chipConcurrent.id
                    Variant.NOT_CANCELLABLE -> binding.chipNotCancellable.id
                    Variant.PROGRESS -> binding.chipProgress.id
                    Variant.CHANNELS -> binding.chipChannels.id
                }
            )

        }
    }

    private fun saveParamsToPrefs(params: Params) {
        val prefs = getSharedPreferences(PREFS_NAMESPACE, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString(PREFS_USERNAME, params.username)
            putString(PREFS_PASSWORD, params.password)
            putString(PREFS_ORG, params.org)
            putInt(PREFS_CHIP_VARIAN, params.variant.ordinal)
            apply()
        }
    }

    private fun getParamsFromPrefs(): Params {
        val prefs = getSharedPreferences(PREFS_NAMESPACE, Context.MODE_PRIVATE)
        with(prefs) {
            return Params(
                username = this.getString(PREFS_USERNAME, "") ?: "",
                password = this.getString(PREFS_PASSWORD, "") ?: "",
                org = this.getString(PREFS_ORG, "kotlin") ?: "kotlin",
                variant = Variant.values()[this.getInt(PREFS_CHIP_VARIAN, 0)]
            )
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
