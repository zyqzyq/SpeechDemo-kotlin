package com.iflytek.voicedemo

import com.iflytek.ise.result.xml.XmlResultParser
import com.iflytek.speech.setting.IseSettings
import com.iflytek.sunflower.FlowerCollector
import com.iflytek.cloud.EvaluatorListener
import com.iflytek.cloud.EvaluatorResult
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechEvaluator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.isedemo.*

/**
 * 语音评测demo
 */
class IseDemo : Activity(), OnClickListener {

    private var mEvaTextEditText: EditText? = null
    private var mResultEditText: EditText? = null
    private var mIseStartButton: Button? = null
    private var mToast: Toast? = null

    // 评测语种
    private var language: String? = null
    // 评测题型
    private var category: String? = null
    // 结果等级
    private var result_level: String? = null

    private var mLastResult: String? = null
    private var mIse: SpeechEvaluator? = null


    // 评测监听接口
    private val mEvaluatorListener = object : EvaluatorListener {

        override fun onResult(result: EvaluatorResult, isLast: Boolean) {
            Log.d(TAG, "evaluator result :" + isLast)

            if (isLast) {
                val builder = StringBuilder()
                builder.append(result.resultString)

                if (!TextUtils.isEmpty(builder)) {
                    mResultEditText!!.setText(builder.toString())
                }
                mIseStartButton!!.isEnabled = true
                mLastResult = builder.toString()

                showTip("评测结束")
            }
        }

        override fun onError(error: SpeechError?) {
            mIseStartButton!!.isEnabled = true
            if (error != null) {
                showTip("error:" + error.errorCode + "," + error.errorDescription)
                mResultEditText!!.setText("")
                mResultEditText!!.hint = "请点击“开始评测”按钮"
            } else {
                Log.d(TAG, "evaluator over")
            }
        }

        override fun onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.d(TAG, "evaluator begin")
        }

        override fun onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.d(TAG, "evaluator stoped")
        }

        override fun onVolumeChanged(volume: Int, data: ByteArray) {
            showTip("当前音量：" + volume)
            Log.d(TAG, "返回音频数据：" + data.size)
        }

        override fun onEvent(eventType: Int, arg1: Int, arg2: Int, obj: Bundle?) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.isedemo)

        mIse = SpeechEvaluator.createEvaluator(this@IseDemo, null)
        initUI()
        setEvaText()
    }

    private fun initUI() {
        image_ise_set.setOnClickListener(this@IseDemo)
        mEvaTextEditText = ise_eva_text
        mResultEditText = ise_result_text
        mIseStartButton = ise_start
        mIseStartButton!!.setOnClickListener(this@IseDemo)
        ise_parse.setOnClickListener(this@IseDemo)
        ise_stop.setOnClickListener(this@IseDemo)
       ise_cancel.setOnClickListener(this@IseDemo)

        mToast = Toast.makeText(this@IseDemo, "", Toast.LENGTH_LONG)
    }

    override fun onClick(view: View) {
        if (null == mIse) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化")
            return
        }

        when (view.id) {
            R.id.image_ise_set -> {
                val intent = Intent(this@IseDemo, IseSettings::class.java)
                startActivityForResult(intent, REQUEST_CODE_SETTINGS)
            }
            R.id.ise_start -> {
                if (mIse == null) {
                    return
                }

                val evaText = mEvaTextEditText!!.text.toString()
                mLastResult = null
                mResultEditText!!.setText("")
                mResultEditText!!.hint = "请朗读以上内容"
                mIseStartButton!!.isEnabled = false

                setParams()
                mIse!!.startEvaluating(evaText, null, mEvaluatorListener)
            }
            R.id.ise_parse ->
                // 解析最终结果
                if (!TextUtils.isEmpty(mLastResult)) {
                    val resultParser = XmlResultParser()
                    val result = resultParser.parse(mLastResult)

                    if (null != result) {
                        mResultEditText!!.setText(result.toString())
                    } else {
                        showTip("解析结果为空")
                    }
                }
            R.id.ise_stop -> if (mIse!!.isEvaluating) {
                mResultEditText!!.hint = "评测已停止，等待结果中..."
                mIse!!.stopEvaluating()
            }
            R.id.ise_cancel -> {
                mIse!!.cancel()
                mIseStartButton!!.isEnabled = true
                mResultEditText!!.setText("")
                mResultEditText!!.hint = "请点击“开始评测”按钮"
                mLastResult = null
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            if (REQUEST_CODE_SETTINGS == requestCode) {
                setEvaText()
            }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (null != mIse) {
            mIse!!.destroy()
            mIse = null
        }
    }

    // 设置评测试题
    private fun setEvaText() {
        val pref = getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE)
        language = pref.getString(SpeechConstant.LANGUAGE, "zh_cn")
        category = pref.getString(SpeechConstant.ISE_CATEGORY, "read_sentence")

        var text = ""
        if ("en_us" == language) {
            if ("read_word" == category) {
                text = getString(R.string.text_en_word)
            } else if ("read_sentence" == category) {
                text = getString(R.string.text_en_sentence)
            }
        } else {
            // 中文评测
            if ("read_syllable" == category) {
                text = getString(R.string.text_cn_syllable)
            } else if ("read_word" == category) {
                text = getString(R.string.text_cn_word)
            } else if ("read_sentence" == category) {
                text = getString(R.string.text_cn_sentence)
            }
        }

        mEvaTextEditText!!.setText(text)
        mResultEditText!!.setText("")
        mLastResult = null
        mResultEditText!!.hint = "请点击“开始评测”按钮"
    }

    private fun showTip(str: String) {
        if (!TextUtils.isEmpty(str)) {
            mToast!!.setText(str)
            mToast!!.show()
        }
    }

    private fun setParams() {
        val pref = getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE)
        // 设置评测语言
        language = pref.getString(SpeechConstant.LANGUAGE, "zh_cn")
        // 设置需要评测的类型
        category = pref.getString(SpeechConstant.ISE_CATEGORY, "read_sentence")
        // 设置结果等级（中文仅支持complete）
        result_level = pref.getString(SpeechConstant.RESULT_LEVEL, "complete")
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        val vad_bos = pref.getString(SpeechConstant.VAD_BOS, "5000")
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        val vad_eos = pref.getString(SpeechConstant.VAD_EOS, "1800")
        // 语音输入超时时间，即用户最多可以连续说多长时间；
        val speech_timeout = pref.getString(SpeechConstant.KEY_SPEECH_TIMEOUT, "-1")

        mIse!!.setParameter(SpeechConstant.LANGUAGE, language)
        mIse!!.setParameter(SpeechConstant.ISE_CATEGORY, category)
        mIse!!.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8")
        mIse!!.setParameter(SpeechConstant.VAD_BOS, vad_bos)
        mIse!!.setParameter(SpeechConstant.VAD_EOS, vad_eos)
        mIse!!.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, speech_timeout)
        mIse!!.setParameter(SpeechConstant.RESULT_LEVEL, result_level)

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIse!!.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")
        mIse!!.setParameter(SpeechConstant.ISE_AUDIO_PATH, Environment.getExternalStorageDirectory().absolutePath + "/msc/ise.wav")
    }

    override fun onResume() {
        // 开放统计 移动数据统计分析
        FlowerCollector.onResume(this@IseDemo)
        FlowerCollector.onPageStart(TAG)
        super.onResume()
    }

    override fun onPause() {
        // 开放统计 移动数据统计分析
        FlowerCollector.onPageEnd(TAG)
        FlowerCollector.onPause(this@IseDemo)
        super.onPause()
    }

    companion object {
        private val TAG = IseDemo::class.java.simpleName

        private val PREFER_NAME = "ise_settings"
        private val REQUEST_CODE_SETTINGS = 1
    }
}
