package com.iflytek.speech.setting

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import com.iflytek.speech.util.SettingTextWatcher
import com.iflytek.voicedemo.R
import android.view.Window.FEATURE_NO_TITLE
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.PreferenceActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window


/**
 * 听写设置界面
 */
class IatSettings : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        fragmentManager.beginTransaction().
                replace(android.R.id.content, IatFragment())
                .commit()
    }

    class IatFragment:PreferenceFragment(), OnPreferenceChangeListener
    {
        private var mVadbosPreference: EditTextPreference? = null
        private var mVadeosPreference: EditTextPreference? = null

        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferencesName = PREFER_NAME
            addPreferencesFromResource(R.xml.iat_setting)

            mVadbosPreference = findPreference("iat_vadbos_preference") as EditTextPreference
            mVadbosPreference!!.editText.addTextChangedListener(SettingTextWatcher(activity, mVadbosPreference!!, 0, 10000))

            mVadeosPreference = findPreference("iat_vadeos_preference") as EditTextPreference
            mVadeosPreference!!.editText.addTextChangedListener(SettingTextWatcher( activity, mVadeosPreference!!, 0, 10000))
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            return true
        }

        companion object {
            val PREFER_NAME = "com.iflytek.setting"
        }
    }
}