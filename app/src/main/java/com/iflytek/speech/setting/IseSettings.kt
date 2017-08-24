/**

 */
package com.iflytek.speech.setting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.*
import android.preference.Preference.OnPreferenceChangeListener
import android.text.InputType
import android.text.TextUtils
import android.view.Window
import android.widget.Toast

import com.iflytek.cloud.SpeechConstant
import com.iflytek.voicedemo.R
import org.jetbrains.anko.toast

/**
 * 评测设置界面
 */
class IseSettings : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content,IseFragment())
                .commit()
    }
    class IseFragment : PreferenceFragment() {

        private var mLanguagePref: ListPreference? = null
        private var mCategoryPref: ListPreference? = null
        private var mResultLevelPref: ListPreference? = null
        private var mVadBosPref: EditTextPreference? = null
        private var mVadEosPref: EditTextPreference? = null
        private var mSpeechTimeoutPref: EditTextPreference? = null


        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)

            preferenceManager.sharedPreferencesName = PREFER_NAME
            addPreferencesFromResource(R.xml.ise_settings)

            initUI()
        }

        private fun initUI() {
            mLanguagePref = findPreference(SpeechConstant.LANGUAGE) as ListPreference
            mCategoryPref = findPreference(SpeechConstant.ISE_CATEGORY) as ListPreference
            mResultLevelPref = findPreference(SpeechConstant.RESULT_LEVEL) as ListPreference
            mVadBosPref = findPreference(SpeechConstant.VAD_BOS) as EditTextPreference
            mVadEosPref = findPreference(SpeechConstant.VAD_EOS) as EditTextPreference
            mSpeechTimeoutPref = findPreference(SpeechConstant.KEY_SPEECH_TIMEOUT) as EditTextPreference



            mLanguagePref?.summary = "当前：" + mLanguagePref!!.entry
            mCategoryPref?.summary = "当前：" + mCategoryPref!!.entry
            mResultLevelPref?.summary = "当前：" + mResultLevelPref!!.entry
            mVadBosPref?.summary = "当前：" + mVadBosPref!!.text + "ms"
            mVadEosPref?.summary = "当前：" + mVadEosPref!!.text + "ms"

            val speech_timeout = mSpeechTimeoutPref?.text
            var summary = "当前：" + speech_timeout
            if ("-1" != speech_timeout) {
                summary += "ms"
            }
            mSpeechTimeoutPref?.summary = summary

            mLanguagePref?.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
                if ("zh_cn" == newValue.toString()) {
                    if ("plain" == mResultLevelPref?.value) {
                        toast("汉语评测结果格式不支持plain设置")
                        return@OnPreferenceChangeListener false
                    }
                } else {
                    if ("read_syllable" == mCategoryPref?.value) {
                        toast("英语评测不支持单字")
                        return@OnPreferenceChangeListener false
                    }
                }

                val newValueIndex = mLanguagePref?.findIndexOfValue(newValue.toString())
                val newEntry = mLanguagePref!!.entries[newValueIndex!!] as String
                mLanguagePref?.summary = "当前：" + newEntry
                true
            }

            mCategoryPref?.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
                if ("en_us" == mLanguagePref?.value && "read_syllable" == newValue.toString()) {
                    toast("英语评测不支持单字，请选其他项")
                    return@OnPreferenceChangeListener false
                }

                val newValueIndex = mCategoryPref!!.findIndexOfValue(newValue.toString())
                val newEntry = mCategoryPref!!.entries[newValueIndex] as String
                mCategoryPref?.summary = "当前：" + newEntry
                true
            }

            mResultLevelPref?.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
                if ("zh_cn" == mLanguagePref!!.value && "plain" == newValue.toString()) {
                    toast("汉语评测不支持plain，请选其他项")
                    return@OnPreferenceChangeListener false
                }

                mResultLevelPref?.summary = "当前：" + newValue.toString()
                true
            }

            mVadBosPref?.editText?.inputType = InputType.TYPE_CLASS_NUMBER
            mVadBosPref?.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
                val bos: Int
                try {
                    bos = Integer.parseInt(newValue.toString())
                } catch (e: Exception) {
                    toast("无效输入！")
                    return@OnPreferenceChangeListener false
                }

                if (bos < 0 || bos > 30000) {
                    toast("取值范围为0~30000")
                    return@OnPreferenceChangeListener false
                }

                mVadBosPref?.summary = "当前：" + bos + "ms"
                true
            }

            mVadEosPref?.editText?.inputType = InputType.TYPE_CLASS_NUMBER
            mVadEosPref?.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
                val eos: Int
                try {
                    eos = Integer.parseInt(newValue.toString())
                } catch (e: Exception) {
                    toast("无效输入！")
                    return@OnPreferenceChangeListener false
                }

                if (eos < 0 || eos > 30000) {
                    toast("取值范围为0~30000")
                    return@OnPreferenceChangeListener false
                }

                mVadEosPref?.summary = "当前：" + eos + "ms"
                true
            }

            mSpeechTimeoutPref?.editText?.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_CLASS_NUMBER
            mSpeechTimeoutPref?.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
                val speech_timeout: Int
                try {
                    speech_timeout = Integer.parseInt(newValue.toString())
                } catch (e: Exception) {
                    toast("无效输入！")
                    return@OnPreferenceChangeListener false
                }

                if (speech_timeout < -1) {
                    toast("必须大于等于-1")
                    return@OnPreferenceChangeListener false
                }

                if (speech_timeout == -1) {
                    mSpeechTimeoutPref?.summary = "当前：-1"
                } else {
                    mSpeechTimeoutPref?.summary = "当前：" + speech_timeout + "ms"
                }

                true
            }
        }

        companion object {
            private val PREFER_NAME = "ise_settings"
        }
    }


}