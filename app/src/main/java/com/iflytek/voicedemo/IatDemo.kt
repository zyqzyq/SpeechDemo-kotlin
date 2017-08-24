package com.iflytek.voicedemo


import java.util.LinkedHashMap

import org.json.JSONException
import org.json.JSONObject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.widget.EditText
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.Toast

import com.iflytek.cloud.ErrorCode
import com.iflytek.cloud.InitListener
import com.iflytek.cloud.LexiconListener
import com.iflytek.cloud.RecognizerListener
import com.iflytek.cloud.RecognizerResult
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechRecognizer
import com.iflytek.cloud.SpeechUtility
import com.iflytek.cloud.ui.RecognizerDialog
import com.iflytek.cloud.ui.RecognizerDialogListener
import com.iflytek.cloud.util.ContactManager
import com.iflytek.cloud.util.ContactManager.ContactListener
import com.iflytek.speech.setting.IatSettings
import com.iflytek.speech.util.ApkInstaller
import com.iflytek.speech.util.FucUtil
import com.iflytek.speech.util.JsonParser
import com.iflytek.sunflower.FlowerCollector
import kotlinx.android.synthetic.main.iatdemo.*

class IatDemo : Activity(), OnClickListener {
    // 语音听写对象
    private var mIat: SpeechRecognizer? = null
    // 语音听写UI
    private var mIatDialog: RecognizerDialog? = null
    // 用HashMap存储听写结果
    private val mIatResults = LinkedHashMap<String, String>()

    private var mResultText: EditText? = null
    private var mToast: Toast? = null
    private var mSharedPreferences: SharedPreferences? = null
    // 引擎类型
    private var mEngineType = SpeechConstant.TYPE_CLOUD
    // 语记安装助手类
    internal var mInstaller: ApkInstaller? = null

    private var mTranslateEnable = false


    @SuppressLint("ShowToast")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.iatdemo)

        initLayout()
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(this@IatDemo, mInitListener)

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = RecognizerDialog(this@IatDemo, mInitListener)

        mSharedPreferences = getSharedPreferences(IatSettings.IatFragment.PREFER_NAME,
                Activity.MODE_PRIVATE)
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        mResultText = iat_text
        mInstaller = ApkInstaller(this@IatDemo)
    }

    /**
     * 初始化Layout。
     */
    private fun initLayout() {
        iat_recognize.setOnClickListener(this@IatDemo)
        iat_recognize_stream.setOnClickListener(this@IatDemo)
        iat_upload_contacts.setOnClickListener(this@IatDemo)
        iat_upload_userwords.setOnClickListener(this@IatDemo)
        iat_stop.setOnClickListener(this@IatDemo)
        iat_cancel.setOnClickListener(this@IatDemo)
        image_iat_set.setOnClickListener(this@IatDemo)
        // 选择云端or本地
        radioGroup.setOnCheckedChangeListener(OnCheckedChangeListener { radioGroup, checkedId ->
            if (null == mIat) {
                // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
                showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化")
                return@OnCheckedChangeListener
            }

            when (checkedId) {
                R.id.iatRadioCloud -> {
                    mEngineType = SpeechConstant.TYPE_CLOUD
                    iat_upload_contacts.isEnabled = true
                    iat_upload_userwords.isEnabled = true
                }
                R.id.iatRadioLocal -> {
                    mEngineType = SpeechConstant.TYPE_LOCAL
                    iat_upload_contacts.isEnabled = false
                    iat_upload_userwords.isEnabled = false
                    /**
                     * 选择本地听写 判断是否安装语记,未安装则跳转到提示安装页面
                     */
                    /**
                     * 选择本地听写 判断是否安装语记,未安装则跳转到提示安装页面
                     */
                    if (!SpeechUtility.getUtility().checkServiceInstalled()) {
                        mInstaller?.install()
                    } else {
                        val result = FucUtil.checkLocalResource()
                        if (!TextUtils.isEmpty(result)) {
                            showTip(result)
                        }
                    }
                }
                R.id.iatRadioMix -> {
                    mEngineType = SpeechConstant.TYPE_MIX
                    iat_upload_contacts.isEnabled = false
                    iat_upload_userwords.isEnabled = false
                    /**
                     * 选择本地听写 判断是否安装语记,未安装则跳转到提示安装页面
                     */
                    /**
                     * 选择本地听写 判断是否安装语记,未安装则跳转到提示安装页面
                     */
                    if (!SpeechUtility.getUtility().checkServiceInstalled()) {
                        mInstaller?.install()
                    } else {
                        val result = FucUtil.checkLocalResource()
                        if (!TextUtils.isEmpty(result)) {
                            showTip(result)
                        }
                    }
                }
                else -> {
                }
            }
        })
    }

    internal var ret = 0 // 函数调用返回值

    override fun onClick(view: View) {
        if (null == mIat) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化")
            return
        }

        when (view.id) {
        // 进入参数设置页面
            R.id.image_iat_set -> {
                val intents = Intent(this@IatDemo, IatSettings::class.java)
                startActivity(intents)
            }
        // 开始听写
        // 如何判断一次听写结束：OnResult isLast=true 或者 onError
            R.id.iat_recognize -> {
                // 移动数据分析，收集开始听写事件
                FlowerCollector.onEvent(this@IatDemo, "iat_recognize")

                mResultText!!.text = null// 清空显示内容
                mIatResults.clear()
                // 设置参数
                setParam()
                val isShowDialog = mSharedPreferences!!.getBoolean(
                        getString(R.string.pref_key_iat_show), true)
                if (isShowDialog) {
                    // 显示听写对话框
                    mIatDialog!!.setListener(mRecognizerDialogListener)
                    mIatDialog!!.show()
                    showTip(getString(R.string.text_begin))
                } else {
                    // 不显示听写对话框
                    ret = mIat!!.startListening(mRecognizerListener)
                    if (ret != ErrorCode.SUCCESS) {
                        showTip("听写失败,错误码：" + ret)
                    } else {
                        showTip(getString(R.string.text_begin))
                    }
                }
            }
        // 音频流识别
            R.id.iat_recognize_stream -> {
                mResultText!!.text = null// 清空显示内容
                mIatResults.clear()
                // 设置参数
                setParam()
                // 设置音频来源为外部文件
                mIat!!.setParameter(SpeechConstant.AUDIO_SOURCE, "-1")
                // 也可以像以下这样直接设置音频文件路径识别（要求设置文件在sdcard上的全路径）：
                // mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-2");
                // mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, "sdcard/XXX/XXX.pcm");
                ret = mIat!!.startListening(mRecognizerListener)
                if (ret != ErrorCode.SUCCESS) {
                    showTip("识别失败,错误码：" + ret)
                } else {
                    val audioData = FucUtil.readAudioFile(this@IatDemo, "iattest.wav")
                    if (null != audioData) {
                        showTip(getString(R.string.text_begin_recognizer))
                        // 一次（也可以分多次）写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），位长16bit，单声道的wav或者pcm
                        // 写入8KHz采样的音频时，必须先调用setParameter(SpeechConstant.SAMPLE_RATE, "8000")设置正确的采样率
                        // 注：当音频过长，静音部分时长超过VAD_EOS将导致静音后面部分不能识别。
                        // 音频切分方法：FucUtil.splitBuffer(byte[] buffer,int length,int spsize);
                        mIat!!.writeAudio(audioData, 0, audioData.size)
                        mIat!!.stopListening()
                    } else {
                        mIat!!.cancel()
                        showTip("读取音频流失败")
                    }
                }
            }
        // 停止听写
            R.id.iat_stop -> {
                mIat!!.stopListening()
                showTip("停止听写")
            }
        // 取消听写
            R.id.iat_cancel -> {
                mIat!!.cancel()
                showTip("取消听写")
            }
        // 上传联系人
            R.id.iat_upload_contacts -> {
                showTip(getString(R.string.text_upload_contacts))
                val mgr = ContactManager.createManager(this@IatDemo,
                        mContactListener)
                mgr.asyncQueryAllContactsName()
            }
        // 上传用户词表
            R.id.iat_upload_userwords -> {
                showTip(getString(R.string.text_upload_userwords))
                val contents = FucUtil.readFile(this@IatDemo, "userwords", "utf-8")
                mResultText!!.setText(contents)
                // 指定引擎类型
                mIat!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
                mIat!!.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8")
                ret = mIat!!.updateLexicon("userword", contents, mLexiconListener)
                if (ret != ErrorCode.SUCCESS)
                    showTip("上传热词失败,错误码：" + ret)
            }
            else -> {
            }
        }
    }

    /**
     * 初始化监听器。
     */
    private val mInitListener = InitListener { code ->
        Log.d(TAG, "SpeechRecognizer init() code = " + code)
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败，错误码：" + code)
        }
    }

    /**
     * 上传联系人/词表监听器。
     */
    private val mLexiconListener = LexiconListener { lexiconId, error ->
        if (error != null) {
            showTip(error.toString())
        } else {
            showTip(getString(R.string.text_upload_success))
        }
    }

    /**
     * 听写监听器。
     */
    private val mRecognizerListener = object : RecognizerListener {

        override fun onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话")
        }

        override fun onError(error: SpeechError) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            if (mTranslateEnable && error.errorCode == 14002) {
                showTip(error.getPlainDescription(true) + "\n请确认是否已开通翻译功能")
            } else {
                showTip(error.getPlainDescription(true))
            }
        }

        override fun onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话")
        }

        override fun onResult(results: RecognizerResult, isLast: Boolean) {
            Log.d(TAG, results.resultString)
            if (mTranslateEnable) {
                printTransResult(results)
            } else {
                printResult(results)
            }

            if (isLast) {
                // TODO 最后的结果
            }
        }

        override fun onVolumeChanged(volume: Int, data: ByteArray) {
            showTip("当前正在说话，音量大小：" + volume)
            Log.d(TAG, "返回音频数据：" + data.size)
        }

        override fun onEvent(eventType: Int, arg1: Int, arg2: Int, obj: Bundle?) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    }

    private fun printResult(results: RecognizerResult) {
        val text = JsonParser.parseIatResult(results.resultString)

        var sn: String? = null
        // 读取json结果中的sn字段
        try {
            val resultJson = JSONObject(results.resultString)
            sn = resultJson.optString("sn")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        mIatResults.put(sn!!, text)

        val resultBuffer = StringBuffer()
        for (key in mIatResults.keys) {
            resultBuffer.append(mIatResults[key])
        }

        mResultText!!.setText(resultBuffer.toString())
        mResultText!!.setSelection(mResultText!!.length())
    }

    /**
     * 听写UI监听器
     */
    private val mRecognizerDialogListener = object : RecognizerDialogListener {
        override fun onResult(results: RecognizerResult, isLast: Boolean) {
            if (mTranslateEnable) {
                printTransResult(results)
            } else {
                printResult(results)
            }

        }

        /**
         * 识别回调错误.
         */
        override fun onError(error: SpeechError) {
            if (mTranslateEnable && error.errorCode == 14002) {
                showTip(error.getPlainDescription(true) + "\n请确认是否已开通翻译功能")
            } else {
                showTip(error.getPlainDescription(true))
            }
        }

    }

    /**
     * 获取联系人监听器。
     */
    private val mContactListener = ContactListener { contactInfos, changeFlag ->
        // 注：实际应用中除第一次上传之外，之后应该通过changeFlag判断是否需要上传，否则会造成不必要的流量.
        // 每当联系人发生变化，该接口都将会被回调，可通过ContactManager.destroy()销毁对象，解除回调。
        // if(changeFlag) {
        // 指定引擎类型
        runOnUiThread { mResultText!!.setText(contactInfos) }

        mIat!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
        mIat!!.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8")
        ret = mIat!!.updateLexicon("contact", contactInfos, mLexiconListener)
        if (ret != ErrorCode.SUCCESS) {
            showTip("上传联系人失败：" + ret)
        }
    }

    private fun showTip(str: String) {
        mToast!!.setText(str)
        mToast!!.show()
    }

    /**
     * 参数设置

     * @param param
     * *
     * @return
     */
    fun setParam() {
        // 清空参数
        mIat!!.setParameter(SpeechConstant.PARAMS, null)

        // 设置听写引擎
        mIat!!.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType)
        // 设置返回结果格式
        mIat!!.setParameter(SpeechConstant.RESULT_TYPE, "json")

        this.mTranslateEnable = mSharedPreferences!!.getBoolean(this.getString(R.string.pref_key_translate), false)
        if (mTranslateEnable) {
            Log.i(TAG, "translate enable")
            mIat!!.setParameter(SpeechConstant.ASR_SCH, "1")
            mIat!!.setParameter(SpeechConstant.ADD_CAP, "translate")
            mIat!!.setParameter(SpeechConstant.TRS_SRC, "its")
        }

        val lag = mSharedPreferences!!.getString("iat_language_preference",
                "mandarin")
        if (lag == "en_us") {
            // 设置语言
            mIat!!.setParameter(SpeechConstant.LANGUAGE, "en_us")
            mIat!!.setParameter(SpeechConstant.ACCENT, null)

            if (mTranslateEnable) {
                mIat!!.setParameter(SpeechConstant.ORI_LANG, "en")
                mIat!!.setParameter(SpeechConstant.TRANS_LANG, "cn")
            }
        } else {
            // 设置语言
            mIat!!.setParameter(SpeechConstant.LANGUAGE, "zh_cn")
            // 设置语言区域
            mIat!!.setParameter(SpeechConstant.ACCENT, lag)

            if (mTranslateEnable) {
                mIat!!.setParameter(SpeechConstant.ORI_LANG, "cn")
                mIat!!.setParameter(SpeechConstant.TRANS_LANG, "en")
            }
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat!!.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences!!.getString("iat_vadbos_preference", "4000"))

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat!!.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences!!.getString("iat_vadeos_preference", "1000"))

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat!!.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences!!.getString("iat_punc_preference", "1"))

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat!!.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")
        mIat!!.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory().toString() + "/msc/iat.wav")
    }

    private fun printTransResult(results: RecognizerResult) {
        val trans = JsonParser.parseTransResult(results.resultString, "dst")
        val oris = JsonParser.parseTransResult(results.resultString, "src")

        if (TextUtils.isEmpty(trans) || TextUtils.isEmpty(oris)) {
            showTip("解析结果失败，请确认是否已开通翻译功能。")
        } else {
            mResultText!!.setText("原始语言:\n$oris\n目标语言:\n$trans")
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        if (null != mIat) {
            // 退出时释放连接
            mIat!!.cancel()
            mIat!!.destroy()
        }
    }

    override fun onResume() {
        // 开放统计 移动数据统计分析
        FlowerCollector.onResume(this@IatDemo)
        FlowerCollector.onPageStart(TAG)
        super.onResume()
    }

    override fun onPause() {
        // 开放统计 移动数据统计分析
        FlowerCollector.onPageEnd(TAG)
        FlowerCollector.onPause(this@IatDemo)
        super.onPause()
    }

    companion object {
        private val TAG = IatDemo::class.java.simpleName
    }
}
