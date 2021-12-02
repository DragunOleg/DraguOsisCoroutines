package com.example.draguosiscoroutines

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.widget.addTextChangedListener
import com.example.draguosiscoroutines.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        transferPrefsToViewValues()
        setupListeners()
    }

    private fun setupListeners(){
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

    private fun transferViewValuesToParams() {
        saveParamsToPrefs(
            Params(
                username = binding.ghUsername.text.toString(),
                password = binding.ghPassword.text.toString(),
                org = binding.organization.text.toString(),
                variant = when (findViewById<Chip>(binding.chipGroup.checkedChipId).text) {
                    getString(R.string.blocking) -> ChipsVariant.BLOCKING
                    getString(R.string.background) -> ChipsVariant.BACKGROUND
                    getString(R.string.callbacks) -> ChipsVariant.CALLBACKS
                    getString(R.string.suspend) -> ChipsVariant.SUSPEND
                    getString(R.string.concurrent) -> ChipsVariant.CONCURRENT
                    getString(R.string.not_cancellable) -> ChipsVariant.NOT_CANCELLABLE
                    getString(R.string.progress) -> ChipsVariant.PROGRESS
                    getString(R.string.channels) -> ChipsVariant.CHANNELS
                    else -> ChipsVariant.BLOCKING
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
                    ChipsVariant.BLOCKING -> binding.chipBlocking.id
                    ChipsVariant.BACKGROUND -> binding.chipBackground.id
                    ChipsVariant.CALLBACKS -> binding.chipCallbacks.id
                    ChipsVariant.SUSPEND -> binding.chipSuspend.id
                    ChipsVariant.CONCURRENT -> binding.chipConcurrent.id
                    ChipsVariant.NOT_CANCELLABLE -> binding.chipNotCancellable.id
                    ChipsVariant.PROGRESS -> binding.chipProgress.id
                    ChipsVariant.CHANNELS -> binding.chipChannels.id
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
        val z = ChipsVariant.values()[prefs.getInt(PREFS_CHIP_VARIAN, 0)]
        with(prefs) {
            return Params(
                username = this.getString(PREFS_USERNAME, "") ?: "",
                password = this.getString(PREFS_PASSWORD, "") ?: "",
                org = this.getString(PREFS_ORG, "") ?: "",
                variant = ChipsVariant.values()[this.getInt(PREFS_CHIP_VARIAN, 0)]
            )
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
