package com.projetapp

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs_settings, rootKey)

        // Hook the “dark mode” switch to apply immediately
        val darkSwitch = findPreference<SwitchPreferenceCompat>("pref_dark_mode")
        darkSwitch?.setOnPreferenceChangeListener { _, newValue ->
            ThemeHelper.setDarkMode(requireContext(), newValue as Boolean)
            requireActivity().recreate()
            true
        }
    }
}
