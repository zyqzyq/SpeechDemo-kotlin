package com.iflytek.voicedemo

import com.iflytek.sunflower.FlowerCollector
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.Window
import android.widget.BaseAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.list_items.view.*
import kotlinx.android.synthetic.main.main.*
import org.jetbrains.anko.startActivity

class MainActivity : Activity(), OnClickListener {
    private var mToast: Toast? = null

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.main)
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        val listitemAdapter = SimpleAdapter()
        listview_main.adapter = listitemAdapter
    }

    override fun onClick(view: View) {
        val tag = Integer.parseInt(view.tag.toString())
        when (tag) {
            0 ->
                // 语音转写
                startActivity<IatDemo>()
            1 ->
                // 语法识别
                startActivity<AsrDemo>()
            2 ->
                // 语义理解
                startActivity<UnderstanderDemo>()
            3 ->
                // 语音合成
                startActivity<TtsDemo>()
            4 ->
                // 语音评测
                startActivity<IseDemo>()
            5 ->
                // 唤醒
                showTip("请登录：http://www.xfyun.cn/ 下载体验吧！")

                // 声纹
            else -> showTip("在IsvDemo中哦，为了代码简洁，就不放在一起啦，^_^")
        }
    }

    // Menu 列表
    internal var items = arrayOf("立刻体验语音听写", "立刻体验语法识别", "立刻体验语义理解", "立刻体验语音合成", "立刻体验语音评测", "立刻体验语音唤醒", "立刻体验声纹密码")

    private inner class SimpleAdapter : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (null == convertView) {
                val factory = LayoutInflater.from(this@MainActivity)
                val mView = factory.inflate(R.layout.list_items, null)
                convertView = mView
            }

            val btn = convertView!!.btn
            btn.setOnClickListener(this@MainActivity)
            btn.tag = position
            btn.text = items[position]

            return convertView
        }

        override fun getCount(): Int {
            return items.size
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }
    }

    private fun showTip(str: String) {
        mToast!!.setText(str)
        mToast!!.show()
    }

    override fun onResume() {
        // 开放统计 移动数据统计分析
        FlowerCollector.onResume(this@MainActivity)
        FlowerCollector.onPageStart(TAG)
        super.onResume()
    }

    override fun onPause() {
        // 开放统计 移动数据统计分析
        FlowerCollector.onPageEnd(TAG)
        FlowerCollector.onPause(this@MainActivity)
        super.onPause()
    }

    companion object {

        private val TAG = MainActivity::class.java.simpleName
    }
}
