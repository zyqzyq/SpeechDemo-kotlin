package com.iflytek.voicedemo

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
import android.widget.Toast
import org.json.JSONObject
import com.iflytek.cloud.ErrorCode
import com.iflytek.cloud.InitListener
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechUnderstander
import com.iflytek.cloud.SpeechUnderstanderListener
import com.iflytek.cloud.TextUnderstander
import com.iflytek.cloud.TextUnderstanderListener
import com.iflytek.cloud.UnderstanderResult
import com.iflytek.speech.setting.UnderstanderSettings
import com.iflytek.sunflower.FlowerCollector
import kotlinx.android.synthetic.main.understander.*

class UnderstanderDemo : Activity(), OnClickListener {
    // 语义理解对象（语音到语义）。
    private var mSpeechUnderstander: SpeechUnderstander? = null
    // 语义理解对象（文本到语义）。
    private var mTextUnderstander: TextUnderstander? = null
    private var mToast: Toast? = null
    private var mUnderstanderText: EditText? = null

    private var mSharedPreferences: SharedPreferences? = null

    @SuppressLint("ShowToast")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.understander)

        initLayout()
        /**
         * 申请的appid时，我们为开发者开通了开放语义（语义理解）
         * 由于语义理解的场景繁多，需开发自己去开放语义平台：http://www.xfyun.cn/services/osp
         * 配置相应的语音场景，才能使用语义理解，否则文本理解将不能使用，语义理解将返回听写结果。
         */
        // 初始化对象
        mSpeechUnderstander = SpeechUnderstander.createUnderstander(this@UnderstanderDemo, mSpeechUdrInitListener)
        mTextUnderstander = TextUnderstander.createTextUnderstander(this@UnderstanderDemo, mTextUdrInitListener)

        mToast = Toast.makeText(this@UnderstanderDemo, "", Toast.LENGTH_SHORT)
    }

    /**
     * 初始化Layout。
     */
    private fun initLayout() {
        text_understander.setOnClickListener(this@UnderstanderDemo)
        start_understander.setOnClickListener(this@UnderstanderDemo)

        mUnderstanderText = understander_text

        understander_stop.setOnClickListener(this@UnderstanderDemo)
        understander_cancel.setOnClickListener(this@UnderstanderDemo)
        image_understander_set.setOnClickListener(this@UnderstanderDemo)

        mSharedPreferences = getSharedPreferences(UnderstanderSettings.PREFER_NAME, Activity.MODE_PRIVATE)
    }

    /**
     * 初始化监听器（语音到语义）。
     */
    private val mSpeechUdrInitListener = InitListener { code ->
        Log.d(TAG, "speechUnderstanderListener init() code = " + code)
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败,错误码：" + code)
        }
    }

    /**
     * 初始化监听器（文本到语义）。
     */
    private val mTextUdrInitListener = InitListener { code ->
        Log.d(TAG, "textUnderstanderListener init() code = " + code)
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败,错误码：" + code)
        }
    }


    internal var ret = 0// 函数调用返回值
    override fun onClick(view: View) {
        if (null == this.mSpeechUnderstander) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化")
            return
        }

        when (view.id) {
        // 进入参数设置页面
            R.id.image_understander_set -> {
                val intent = Intent(this@UnderstanderDemo, UnderstanderSettings::class.java)
                startActivity(intent)
            }
        // 开始文本理解
            R.id.text_understander -> {
                mUnderstanderText!!.setText("")
                val text = "合肥明天的天气怎么样？"
                showTip(text)

                if (mTextUnderstander!!.isUnderstanding) {
                    mTextUnderstander!!.cancel()
                    showTip("取消")
                } else {
                    // 设置语义情景
                    //mTextUnderstander.setParameter(SpeechConstant.SCENE, "main");
                    ret = mTextUnderstander!!.understandText(text, mTextUnderstanderListener)
                    if (ret != 0) {
                        showTip("语义理解失败,错误码:" + ret)
                    }
                }
            }
        // 开始语音理解
            R.id.start_understander -> {
                mUnderstanderText!!.setText("")
                // 设置参数
                setParam()

                if (mSpeechUnderstander!!.isUnderstanding) {// 开始前检查状态
                    mSpeechUnderstander!!.stopUnderstanding()
                    showTip("停止录音")
                } else {
                    ret = mSpeechUnderstander!!.startUnderstanding(mSpeechUnderstanderListener)
                    if (ret != 0) {
                        showTip("语义理解失败,错误码:" + ret)
                    } else {
                        showTip(getString(R.string.text_begin))
                    }
                }
            }
        // 停止语音理解
            R.id.understander_stop -> {
                mSpeechUnderstander!!.stopUnderstanding()
                showTip("停止语义理解")
            }
        // 取消语音理解
            R.id.understander_cancel -> {
                mSpeechUnderstander!!.cancel()
                showTip("取消语义理解")
            }
            else -> {
            }
        }
    }

    private val mTextUnderstanderListener = object : TextUnderstanderListener {

        override fun onResult(result: UnderstanderResult?) {
            if (null != result) {
                // 显示
                val text = result.resultString
                if (!TextUtils.isEmpty(text)) {
                    mUnderstanderText!!.setText(text)

                    if (0 != getResultError(text)) {
                        showTip(errorTip, Toast.LENGTH_LONG)
                    }
                }
            } else {
                Log.d(TAG, "understander result:null")
                showTip("识别结果不正确。")
            }
        }

        override fun onError(error: SpeechError) {
            // 文本语义不能使用回调错误码14002，请确认您下载sdk时是否勾选语义场景和私有语义的发布
            // 请到 aiui.xfyun.cn 配置语义，从1115前的SDK更新到1116以上版本SDK后，语义需要重新到 aiui.xfyun.cn 配置
            showTip("onError Code：" + error.errorCode + ", " + errorTip, Toast.LENGTH_LONG)
        }
    }

    /**
     * 语义理解回调。
     */
    private val mSpeechUnderstanderListener = object : SpeechUnderstanderListener {

        override fun onResult(result: UnderstanderResult?) {
            if (null != result) {
                Log.d(TAG, result.resultString)

                // 显示
                val text = result.resultString
                if (!TextUtils.isEmpty(text)) {
                    mUnderstanderText!!.setText(text)
                    if (0 != getResultError(text)) {
                        showTip(errorTip, Toast.LENGTH_LONG)
                    }
                }
            } else {
                showTip("识别结果不正确。")
            }
        }

        override fun onVolumeChanged(volume: Int, data: ByteArray) {
            showTip("当前正在说话，音量大小：" + volume)
            Log.d(TAG, data.size.toString() + "")
        }

        override fun onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话")
        }

        override fun onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话")
        }

        override fun onError(error: SpeechError) {
            if (error.errorCode == ErrorCode.MSP_ERROR_NO_DATA) {
                showTip(error.getPlainDescription(true), Toast.LENGTH_LONG)
            } else {
                showTip(error.getPlainDescription(true) + ", " + errorTip, Toast.LENGTH_LONG)
            }
        }

        override fun onEvent(eventType: Int, arg1: Int, arg2: Int, obj: Bundle?) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (null != mSpeechUnderstander) {
            // 退出时释放连接
            mSpeechUnderstander!!.cancel()
            mSpeechUnderstander!!.destroy()
        }

        if (null != mTextUnderstander) {
            if (mTextUnderstander!!.isUnderstanding)
                mTextUnderstander!!.cancel()
            mTextUnderstander!!.destroy()
        }
    }

    private fun showTip(str: String) {
        mToast!!.setText(str)
        mToast!!.show()
    }

    private fun showTip(str: String, duration: Int) {
        val lastDuration = mToast!!.duration
        mToast!!.setText(str)
        mToast!!.duration = duration
        mToast!!.show()
        mToast!!.duration = lastDuration
    }

    private fun getResultError(resultText: String): Int {
        var error = 0
        try {
            val KEY_ERROR = "error"
            val KEY_CODE = "code"
            val joResult = JSONObject(resultText)
            val joError = joResult.optJSONObject(KEY_ERROR)
            if (null != joError) {
                error = joError.optInt(KEY_CODE)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        //end of try-catch

        return error
    }

    /**
     * 参数设置
     * @param param
     * *
     * @return
     */
    fun setParam() {
        val lang = mSharedPreferences!!.getString("understander_language_preference", "mandarin")
        if (lang == "en_us") {
            // 设置语言
            mSpeechUnderstander!!.setParameter(SpeechConstant.LANGUAGE, "en_us")
            mSpeechUnderstander!!.setParameter(SpeechConstant.ACCENT, null)
        } else {
            // 设置语言
            mSpeechUnderstander!!.setParameter(SpeechConstant.LANGUAGE, "zh_cn")
            // 设置语言区域
            mSpeechUnderstander!!.setParameter(SpeechConstant.ACCENT, lang)
        }
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mSpeechUnderstander!!.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences!!.getString("understander_vadbos_preference", "4000"))

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mSpeechUnderstander!!.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences!!.getString("understander_vadeos_preference", "1000"))

        // 设置标点符号，默认：1（有标点）
        mSpeechUnderstander!!.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences!!.getString("understander_punc_preference", "1"))

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mSpeechUnderstander!!.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")
        mSpeechUnderstander!!.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory().toString() + "/msc/sud.wav")

        // 设置语义情景
        //mSpeechUnderstander.setParameter(SpeechConstant.SCENE, "main");
    }

    override fun onResume() {
        //移动数据统计分析
        FlowerCollector.onResume(this@UnderstanderDemo)
        FlowerCollector.onPageStart(TAG)
        super.onResume()
    }

    override fun onPause() {
        //移动数据统计分析
        FlowerCollector.onPageEnd(TAG)
        FlowerCollector.onPause(this@UnderstanderDemo)
        super.onPause()
    }

    companion object {
        private val TAG = UnderstanderDemo::class.java.simpleName

        val errorTip = "请确认是否有在 aiui.xfyun.cn 配置语义。（另外，已开通语义，但从1115（含1115）以前的SDK更新到1116以上版本SDK后，语义需要重新到 aiui.xfyun.cn 配置）"
    }

}
