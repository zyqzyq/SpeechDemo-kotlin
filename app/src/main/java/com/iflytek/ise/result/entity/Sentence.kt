/**

 */
package com.iflytek.ise.result.entity

import java.util.ArrayList

/**
 *
 * Title: Sentence
 *
 * Description: 句子，对应于xml结果中的sentence标签
 *
 * Company: www.iflytek.com
 * @author iflytek
 * *
 * @date 2015年1月12日 下午4:10:09
 */
class Sentence {
    /**
     * 开始帧位置，每帧相当于10ms
     */
    var beg_pos: Int = 0
    /**
     * 结束帧位置
     */
    var end_pos: Int = 0
    /**
     * 句子内容
     */
    var content: String? = null
    /**
     * 总得分
     */
    var total_score: Float = 0.toFloat()
    /**
     * 时长（单位：帧，每帧相当于10ms）（cn）
     */
    var time_len: Int = 0
    /**
     * 句子的索引（en）
     */
    var index: Int = 0
    /**
     * 单词数（en）
     */
    var word_count: Int = 0
    /**
     * sentence包括的word
     */
    var words: ArrayList<Word>? = null
}
