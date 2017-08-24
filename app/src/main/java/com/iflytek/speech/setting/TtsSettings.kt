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
import com.iflytek.voicedemo.R


/**
 * 合成设置界面
 */
class TtsSettings : Activity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content,TtsFragment())
                .commit()
    }

class TtsFragment:PreferenceFragment(), OnPreferenceChangeListener {
    private var mSpeedPreference: EditTextPreference? = null
    private var mPitchPreference: EditTextPreference? = null
    private var mVolumePreference: EditTextPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 指定保存文件名字
        preferenceManager.sharedPreferencesName = PREFER_NAME
        addPreferencesFromResource(R.xml.tts_setting)
        mSpeedPreference = findPreference("speed_preference") as EditTextPreference
        mSpeedPreference!!.editText.addTextChangedListener(SettingTextWatcher(activity, mSpeedPreference!!, 0, 200))

        mPitchPreference = findPreference("pitch_preference") as EditTextPreference
        mPitchPreference!!.editText.addTextChangedListener(SettingTextWatcher(activity, mPitchPreference!!, 0, 100))

        mVolumePreference = findPreference("volume_preference") as EditTextPreference
        mVolumePreference!!.editText.addTextChangedListener(SettingTextWatcher(activity, mVolumePreference!!, 0, 100))

    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        return true
    }

    companion object {

        val PREFER_NAME = "com.iflytek.setting"
    }


}
}