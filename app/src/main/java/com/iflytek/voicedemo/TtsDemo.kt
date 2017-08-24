package com.iflytek.voicedemo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast

import com.iflytek.cloud.ErrorCode
import com.iflytek.cloud.InitListener
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechSynthesizer
import com.iflytek.cloud.SpeechUtility
import com.iflytek.cloud.SynthesizerListener
import com.iflytek.speech.setting.TtsSettings
import com.iflytek.speech.util.ApkInstaller
import com.iflytek.sunflower.FlowerCollector
import kotlinx.android.synthetic.main.ttsdemo.*

class TtsDemo : Activity(), OnClickListener {
    // 语音合成对象
    private var mTts: SpeechSynthesizer? = null

    // 默认发音人
    private var voicer = "xiaoyan"

    private var mCloudVoicersEntries: Array<String>? = null
    private var mCloudVoicersValue: Array<String>? = null

    // 缓冲进度
    private var mPercentForBuffering = 0
    // 播放进度
    private var mPercentForPlaying = 0

    // 云端/本地单选按钮
    //private var mRadioGroup: RadioGroup? = null

    // 引擎类型
    private var mEngineType = SpeechConstant.TYPE_CLOUD
    // 语记安装助手类
    internal var mInstaller: ApkInstaller? = null

    private var mToast: Toast? = null
    private var mSharedPreferences: SharedPreferences? = null

    @SuppressLint("ShowToast")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.ttsdemo)

        initLayout()
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(this@TtsDemo, mTtsInitListener)

        // 云端发音人名称列表
        mCloudVoicersEntries = resources.getStringArray(R.array.voicer_cloud_entries)
        mCloudVoicersValue = resources.getStringArray(R.array.voicer_cloud_values)

        mSharedPreferences = getSharedPreferences(TtsSettings.TtsFragment.PREFER_NAME, Context.MODE_PRIVATE)
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)

        mInstaller = ApkInstaller(this@TtsDemo)
    }

    /**
     * 初始化Layout。
     */
    private fun initLayout() {
        tts_play.setOnClickListener(this@TtsDemo)
        tts_cancel.setOnClickListener(this@TtsDemo)
        tts_pause.setOnClickListener(this@TtsDemo)
        tts_resume.setOnClickListener(this@TtsDemo)
        image_tts_set.setOnClickListener(this@TtsDemo)
        tts_btn_person_select.setOnClickListener(this@TtsDemo)

        tts_rediogroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.tts_radioCloud -> mEngineType = SpeechConstant.TYPE_CLOUD
                R.id.tts_radioLocal -> {
                    mEngineType = SpeechConstant.TYPE_LOCAL
                    /**
                     * 选择本地合成
                     * 判断是否安装语记,未安装则跳转到提示安装页面
                     */
                    /**
                     * 选择本地合成
                     * 判断是否安装语记,未安装则跳转到提示安装页面
                     */
                    val utility = SpeechUtility.getUtility()
                    if (null != utility && !utility.checkServiceInstalled()) {
                        mInstaller?.install()
                    }
                }
                else -> {
                }
            }
        }
    }

    override fun onClick(view: View) {
        if (null == mTts) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化")
            return
        }

        when (view.id) {
            R.id.image_tts_set -> if (SpeechConstant.TYPE_CLOUD == mEngineType) {
                val intent = Intent(this@TtsDemo, TtsSettings::class.java)
                startActivity(intent)
            } else {
                // 本地设置跳转到语记中
                if (!SpeechUtility.getUtility().checkServiceInstalled()) {
                    mInstaller?.install()
                } else {
                    SpeechUtility.getUtility().openEngineSettings(null)
                }
            }
        // 开始合成
        // 收到onCompleted 回调时，合成结束、生成合成音频
        // 合成的音频格式：只支持pcm格式
            R.id.tts_play -> {
                // 移动数据分析，收集开始合成事件
                FlowerCollector.onEvent(this@TtsDemo, "tts_play")

                val text = (findViewById<View>(R.id.tts_text) as EditText).text.toString()
                // 设置参数
                setParam()
                val code = mTts!!.startSpeaking(text, mTtsListener)
                //			/**
                //			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
                //			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
                //			*/
                //			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
                //			int code = mTts.synthesizeToUri(text, path, mTtsListener);

                if (code != ErrorCode.SUCCESS) {
                    if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                        //未安装则跳转到提示安装页面
                        mInstaller?.install()
                    } else {
                        showTip("语音合成失败,错误码: " + code)
                    }
                }
            }
        // 取消合成
            R.id.tts_cancel -> mTts!!.stopSpeaking()
        // 暂停播放
            R.id.tts_pause -> mTts!!.pauseSpeaking()
        // 继续播放
            R.id.tts_resume -> mTts!!.resumeSpeaking()
        // 选择发音人
            R.id.tts_btn_person_select -> showPresonSelectDialog()
        }
    }

    private var selectedNum = 0
    /**
     * 发音人选择。
     */
    private fun showPresonSelectDialog() {
        when (tts_rediogroup?.checkedRadioButtonId) {
        // 选择在线合成
            R.id.tts_radioCloud -> AlertDialog.Builder(this).setTitle("在线合成发音人选项")
                    .setSingleChoiceItems(mCloudVoicersEntries, // 单选框有几项,各是什么名字
                            selectedNum // 默认的选项
                    ) { dialog, which ->
                        // 点击单选框后的处理
                        // 点击了哪一项
                        voicer = mCloudVoicersValue!![which]
                        if ("catherine" == voicer || "henry" == voicer || "vimary" == voicer) {
                            tts_text.setText(R.string.text_tts_source_en)
                        } else {
                            tts_text.setText(R.string.text_tts_source)
                        }
                        selectedNum = which
                        dialog.dismiss()
                    }.show()

        // 选择本地合成
            R.id.tts_radioLocal -> {
                val utility = SpeechUtility.getUtility()
                if (null != utility) {
                    if (!utility.checkServiceInstalled()) {
                        mInstaller?.install()
                    } else {
                        utility.openEngineSettings(SpeechConstant.ENG_TTS)
                    }
                }
            }
            else -> {
            }
        }
    }

    /**
     * 初始化监听。
     */
    private val mTtsInitListener = InitListener { code ->
        Log.d(TAG, "InitListener init() code = " + code)
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败,错误码：" + code)
        } else {
            // 初始化成功，之后可以调用startSpeaking方法
            // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
            // 正确的做法是将onCreate中的startSpeaking调用移至这里
        }
    }

    /**
     * 合成回调监听。
     */
    private val mTtsListener = object : SynthesizerListener {

        override fun onSpeakBegin() {
            showTip("开始播放")
        }

        override fun onSpeakPaused() {
            showTip("暂停播放")
        }

        override fun onSpeakResumed() {
            showTip("继续播放")
        }

        override fun onBufferProgress(percent: Int, beginPos: Int, endPos: Int,
                                      info: String) {
            // 合成进度
            mPercentForBuffering = percent
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying))
        }

        override fun onSpeakProgress(percent: Int, beginPos: Int, endPos: Int) {
            // 播放进度
            mPercentForPlaying = percent
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying))
        }

        override fun onCompleted(error: SpeechError?) {
            if (error == null) {
                showTip("播放完成")
            } else {
                showTip(error.getPlainDescription(true))
            }
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
    private fun setParam() {
        // 清空参数
        mTts!!.setParameter(SpeechConstant.PARAMS, null)
        // 根据合成引擎设置相应参数
        if (mEngineType == SpeechConstant.TYPE_CLOUD) {
            mTts!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
            // 设置在线合成发音人
            mTts!!.setParameter(SpeechConstant.VOICE_NAME, voicer)
            //设置合成语速
            mTts!!.setParameter(SpeechConstant.SPEED, mSharedPreferences!!.getString("speed_preference", "50"))
            //设置合成音调
            mTts!!.setParameter(SpeechConstant.PITCH, mSharedPreferences!!.getString("pitch_preference", "50"))
            //设置合成音量
            mTts!!.setParameter(SpeechConstant.VOLUME, mSharedPreferences!!.getString("volume_preference", "50"))
        } else {
            mTts!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL)
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts!!.setParameter(SpeechConstant.VOICE_NAME, "")
            /**
             * 本地合成不设置语速、音调、音量，默认使用语记设置
             * 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        //设置播放器音频流类型
        mTts!!.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences!!.getString("stream_preference", "3"))
        // 设置播放合成音频打断音乐播放，默认为true
        mTts!!.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true")

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts!!.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")
        mTts!!.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory().toString() + "/msc/tts.wav")
    }

    override fun onDestroy() {
        super.onDestroy()

        if (null != mTts) {
            mTts!!.stopSpeaking()
            // 退出时释放连接
            mTts!!.destroy()
        }
    }

    override fun onResume() {
        //移动数据统计分析
        FlowerCollector.onResume(this@TtsDemo)
        FlowerCollector.onPageStart(TAG)
        super.onResume()
    }

    override fun onPause() {
        //移动数据统计分析
        FlowerCollector.onPageEnd(TAG)
        FlowerCollector.onPause(this@TtsDemo)
        super.onPause()
    }

    companion object {
        private val TAG = TtsDemo::class.java.simpleName
    }

}
