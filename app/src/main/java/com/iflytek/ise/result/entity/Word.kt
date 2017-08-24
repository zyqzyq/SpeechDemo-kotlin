/**

 */
package com.iflytek.ise.result.entity

import java.util.ArrayList

/**
 *
 * Title: Word
 *
 * Description: 单词，对应于结果xml中的word标签
 *
 * Company: www.iflytek.com
 * @author iflytek
 * *
 * @date 2015年1月12日 下午3:29:30
 */
class Word {
    /**
     * 开始帧位置，每帧相当于10ms
     */
    var beg_pos: Int = 0
    /**
     * 结束帧位置
     */
    var end_pos: Int = 0
    /**
     * 单词内容
     */
    var content: String? = null
    /**
     * 增漏读信息：0（正确），16（漏读），32（增读），64（回读），128（替换）
     */
    var dp_message: Int = 0
    /**
     * 单词在全篇索引（en）
     */
    var global_index: Int = 0
    /**
     * 单词在句子中的索引（en）
     */
    var index: Int = 0
    /**
     * 拼音（cn），数字代表声调，5表示轻声，如fen1
     */
    var symbol: String? = null
    /**
     * 时长（单位：帧，每帧相当于10ms）（cn）
     */
    var time_len: Int = 0
    /**
     * 单词得分（en）
     */
    var total_score: Float = 0.toFloat()
    /**
     * Word包含的Syll
     */
    var sylls: ArrayList<Syll>? = null

}
