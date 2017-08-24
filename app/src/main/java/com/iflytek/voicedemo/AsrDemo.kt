package com.iflytek.voicedemo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.widget.Toast

import com.iflytek.cloud.ErrorCode
import com.iflytek.cloud.GrammarListener
import com.iflytek.cloud.InitListener
import com.iflytek.cloud.LexiconListener
import com.iflytek.cloud.RecognizerListener
import com.iflytek.cloud.RecognizerResult
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechRecognizer
import com.iflytek.cloud.SpeechUtility
import com.iflytek.cloud.util.ContactManager
import com.iflytek.cloud.util.ContactManager.ContactListener
import com.iflytek.speech.util.ApkInstaller
import com.iflytek.speech.util.FucUtil
import com.iflytek.speech.util.JsonParser
import com.iflytek.sunflower.FlowerCollector
import kotlinx.android.synthetic.main.isrdemo.*

class AsrDemo : Activity(), OnClickListener {
    // 语音识别对象
    private var mAsr: SpeechRecognizer? = null
    private var mToast: Toast? = null
    // 缓存
    private var mSharedPreferences: SharedPreferences? = null
    // 本地语法文件
    private var mLocalGrammar: String? = null
    // 本地词典
    private var mLocalLexicon: String? = null
    // 云端语法文件
    private var mCloudGrammar: String? = null

    private var mEngineType: String? = null
    // 语记安装助手类
    internal var mInstaller: ApkInstaller? = null

    @SuppressLint("ShowToast")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.isrdemo)
        initLayout()

        // 初始化识别对象
        mAsr = SpeechRecognizer.createRecognizer(this@AsrDemo, mInitListener)

        // 初始化语法、命令词
        mLocalLexicon = "张海羊\n刘婧\n王锋\n"
        mLocalGrammar = FucUtil.readFile(this, "call.bnf", "utf-8")
        mCloudGrammar = FucUtil.readFile(this, "grammar_sample.abnf", "utf-8")

        // 获取联系人，本地更新词典时使用
        val mgr = ContactManager.createManager(this@AsrDemo, mContactListener)
        mgr.asyncQueryAllContactsName()
        mSharedPreferences = getSharedPreferences(packageName, Context.MODE_PRIVATE)
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)

        mInstaller = ApkInstaller(this@AsrDemo)
    }

    /**
     * 初始化Layout。
     */
    private fun initLayout() {
        isr_recognize.setOnClickListener(this@AsrDemo)
        isr_grammar.setOnClickListener(this@AsrDemo)
        isr_lexcion.setOnClickListener(this@AsrDemo)
        isr_stop.setOnClickListener(this@AsrDemo)
        isr_cancel.setOnClickListener(this@AsrDemo)

        //选择云端or本地
        radioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            if (checkedId == R.id.radioCloud) {
                isr_text.setText(mCloudGrammar)
                isr_lexcion.isEnabled = false
                mEngineType = SpeechConstant.TYPE_CLOUD
            } else {
                isr_text.setText(mLocalGrammar)
                isr_lexcion.isEnabled = true
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
        }
    }

    // 语法、词典临时变量
    internal var mContent: String? = null
    // 函数调用返回值
    internal var ret = 0

    override fun onClick(view: View) {
        if (null == mAsr) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化")
            return
        }

        if (null == mEngineType) {
            showTip("请先选择识别引擎类型")
            return
        }
        when (view.id) {
            R.id.isr_grammar -> {
                showTip("上传预设关键词/语法文件")
                // 本地-构建语法文件，生成语法id
                if (mEngineType == SpeechConstant.TYPE_LOCAL) {
                    isr_text.setText(mLocalGrammar)
                    mContent = mLocalGrammar
                    mAsr!!.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8")
                    //指定引擎类型
                    mAsr!!.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType)
                    ret = mAsr!!.buildGrammar(GRAMMAR_TYPE_BNF, mContent, mLocalGrammarListener)
                    if (ret != ErrorCode.SUCCESS) {
                        if (ret == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                            //未安装则跳转到提示安装页面
                            mInstaller?.install()
                        } else {
                            showTip("语法构建失败,错误码：" + ret)
                        }
                    }
                } else {
                    isr_text.setText(mCloudGrammar)
                    mContent = mCloudGrammar
                    //指定引擎类型
                    mAsr!!.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType)
                    mAsr!!.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8")
                    ret = mAsr!!.buildGrammar(GRAMMAR_TYPE_ABNF, mContent, mCloudGrammarListener)
                    if (ret != ErrorCode.SUCCESS)
                        showTip("语法构建失败,错误码：" + ret)
                }// 在线-构建语法文件，生成语法id
            }
        // 本地-更新词典      注意:更新词典需要在接收到构建语法回调onBuildFinish之后进行，否则会导致错误。
            R.id.isr_lexcion -> {
                isr_text.setText(mLocalLexicon)
                mContent = mLocalLexicon
                //指定引擎类型
                mAsr!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL)
                mAsr!!.setParameter(SpeechConstant.GRAMMAR_LIST, "call")
                ret = mAsr!!.updateLexicon("<contact>", mContent, mLexiconListener)
                if (ret != ErrorCode.SUCCESS) {
                    if (ret == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                        //未安装则跳转到提示安装页面
                        mInstaller?.install()
                    } else {
                        showTip("更新词典失败,错误码：" + ret)
                    }
                }
            }
        // 开始识别
            R.id.isr_recognize -> {
                isr_text.text = null// 清空显示内容
                // 设置参数
                if (!setParam()) {
                    showTip("请先构建语法。")
                    return
                }

                ret = mAsr!!.startListening(mRecognizerListener)
                if (ret != ErrorCode.SUCCESS) {
                    if (ret == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                        //未安装则跳转到提示安装页面
                        mInstaller?.install()
                    } else {
                        showTip("识别失败,错误码: " + ret)
                    }
                }
            }
        // 停止识别
            R.id.isr_stop -> {
                mAsr!!.stopListening()
                showTip("停止识别")
            }
        // 取消识别
            R.id.isr_cancel -> {
                mAsr!!.cancel()
                showTip("取消识别")
            }
        }
    }

    /**
     * 初始化监听器。
     */
    private val mInitListener = InitListener { code ->
        Log.d(TAG, "SpeechRecognizer init() code = " + code)
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败,错误码：" + code)
        }
    }

    /**
     * 更新词典监听器。
     */
    private val mLexiconListener = LexiconListener { lexiconId, error ->
        if (error == null) {
            showTip("词典更新成功")
        } else {
            showTip("词典更新失败,错误码：" + error.errorCode)
        }
    }

    /**
     * 本地构建语法监听器。
     */
    private val mLocalGrammarListener = GrammarListener { grammarId, error ->
        if (error == null) {
            showTip("语法构建成功：" + grammarId)
        } else {
            showTip("语法构建失败,错误码：" + error.errorCode)
        }
    }
    /**
     * 云端构建语法监听器。
     */
    private val mCloudGrammarListener = GrammarListener { grammarId, error ->
        if (error == null) {
            val grammarID = grammarId
            val editor = mSharedPreferences!!.edit()
            if (!TextUtils.isEmpty(grammarId))
                editor.putString(KEY_GRAMMAR_ABNF_ID, grammarID)
            editor.commit()
            showTip("语法构建成功：" + grammarId)
        } else {
            showTip("语法构建失败,错误码：" + error.errorCode)
        }
    }
    /**
     * 获取联系人监听器。
     */
    private val mContactListener = ContactListener { contactInfos, changeFlag ->
        //获取联系人
        mLocalLexicon = contactInfos
    }
    /**
     * 识别监听器。
     */
    private val mRecognizerListener = object : RecognizerListener {

        override fun onVolumeChanged(volume: Int, data: ByteArray) {
            showTip("当前正在说话，音量大小：" + volume)
            Log.d(TAG, "返回音频数据：" + data.size)
        }

        override fun onResult(result: RecognizerResult?, isLast: Boolean) {
            if (null != result) {
                Log.d(TAG, "recognizer result：" + result.resultString)
                val text: String
                if ("cloud".equals(mEngineType!!, ignoreCase = true)) {
                    text = JsonParser.parseGrammarResult(result.resultString)
                } else {
                    text = JsonParser.parseLocalGrammarResult(result.resultString)
                }

                // 显示
                isr_text.setText(text)
            } else {
                Log.d(TAG, "recognizer result : null")
            }
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
            showTip("onError Code：" + error.errorCode)
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
        runOnUiThread {
            mToast!!.setText(str)
            mToast!!.show()
        }
    }

    /**
     * 参数设置
     * @param param
     * *
     * @return
     */
    fun setParam(): Boolean {
        var result = false
        //设置识别引擎
        mAsr!!.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType)
        //设置返回结果为json格式
        mAsr!!.setParameter(SpeechConstant.RESULT_TYPE, "json")

        if ("cloud".equals(mEngineType!!, ignoreCase = true)) {
            val grammarId = mSharedPreferences!!.getString(KEY_GRAMMAR_ABNF_ID, null)
            if (TextUtils.isEmpty(grammarId)) {
                result = false
            } else {
                //设置云端识别使用的语法id
                mAsr!!.setParameter(SpeechConstant.CLOUD_GRAMMAR, grammarId)
                result = true
            }
        } else {
            //设置本地识别使用语法id
            mAsr!!.setParameter(SpeechConstant.LOCAL_GRAMMAR, "call")
            //设置本地识别的门限值
            mAsr!!.setParameter(SpeechConstant.ASR_THRESHOLD, "30")
            result = true
        }

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mAsr!!.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")
        mAsr!!.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory().toString() + "/msc/asr.wav")
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        // 退出时释放连接
        mAsr?.cancel()
        mAsr?.destroy()

    }

    override fun onResume() {
        //移动数据统计分析
        FlowerCollector.onResume(this@AsrDemo)
        FlowerCollector.onPageStart(TAG)
        super.onResume()
    }

    override fun onPause() {
        //移动数据统计分析
        FlowerCollector.onPageEnd(TAG)
        FlowerCollector.onPause(this@AsrDemo)
        super.onPause()
    }

    companion object {
        private val TAG = AsrDemo::class.java.simpleName

        private val KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id"
        private val GRAMMAR_TYPE_ABNF = "abnf"
        private val GRAMMAR_TYPE_BNF = "bnf"
    }

}
