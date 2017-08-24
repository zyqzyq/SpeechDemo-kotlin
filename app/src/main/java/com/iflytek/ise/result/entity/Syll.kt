/**

 */
package com.iflytek.ise.result.entity

import java.util.ArrayList

/**
 *
 * Title: Syll
 *
 * Description: 音节，对应于结果xml中的Syll标签
 *
 * Company: www.iflytek.com
 * @author iflytek
 * *
 * @date 2015年1月12日 下午3:49:51
 */
class Syll {
    /**
     * 开始帧位置，每帧相当于10ms
     */
    var beg_pos: Int = 0
    /**
     * 结束帧位置
     */
    var end_pos: Int = 0
    /**
     * 音节内容
     */
    var content: String? = null
    /**
     * 拼音（cn），数字代表声调，5表示轻声，如fen1
     */
    var symbol: String? = null
    /**
     * 增漏读信息：0（正确），16（漏读），32（增读），64（回读），128（替换）
     */
    var dp_message: Int = 0
    /**
     * 时长（单位：帧，每帧相当于10ms）（cn）
     */
    var time_len: Int = 0
    /**
     * Syll包含的音节
     */
    var phones: ArrayList<Phone>? = null

    /**
     * 获取音节的标准音标（en）

     * @return 标准音标
     */
    val stdSymbol: String
        get() {
            var stdSymbol = ""
            val symbols = content!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (i in symbols.indices) {
                stdSymbol += Phone.getStdSymbol(symbols[i])
            }

            return stdSymbol
        }
}
