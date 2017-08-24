package com.iflytek.speech.setting

import android.app.Activity
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.view.Window

import com.iflytek.speech.util.SettingTextWatcher
import com.iflytek.sunflower.FlowerCollector
import com.iflytek.voicedemo.R

/**
 * 语义理解设置界面
 */
class UnderstanderSettings : Activity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content,UnderstanderFragment())
                .commit()
    }
    class UnderstanderFragment: PreferenceFragment(), OnPreferenceChangeListener {
        private var mVadbosPreference: EditTextPreference? = null
        private var mVadeosPreference: EditTextPreference? = null

        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferencesName = PREFER_NAME
            addPreferencesFromResource(R.xml.understand_setting)

            mVadbosPreference = findPreference("understander_vadbos_preference") as EditTextPreference
            mVadbosPreference!!.editText.addTextChangedListener(SettingTextWatcher(activity, mVadbosPreference!!, 0, 10000))

            mVadeosPreference = findPreference("understander_vadeos_preference") as EditTextPreference
            mVadeosPreference!!.editText.addTextChangedListener(SettingTextWatcher(activity, mVadeosPreference!!, 0, 10000))
        }

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            return true
        }
    }
    override fun onResume() {
        // 开放统计 移动数据统计分析
        FlowerCollector.onResume(this@UnderstanderSettings)
        FlowerCollector.onPageStart(TAG)
        super.onResume()
    }

    override fun onPause() {
        // 开放统计 移动数据统计分析
        FlowerCollector.onPageEnd(TAG)
        FlowerCollector.onPause(this@UnderstanderSettings)
        super.onPause()
    }

    companion object {
        private val TAG = UnderstanderSettings::class.java.simpleName
        val PREFER_NAME = "com.iflytek.setting"
    }

}

