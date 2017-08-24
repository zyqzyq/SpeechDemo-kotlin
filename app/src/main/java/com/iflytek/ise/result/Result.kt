/**

 */
package com.iflytek.ise.result

import java.util.ArrayList

import com.iflytek.ise.result.entity.Sentence

/**
 *
 * Title: Result
 *
 * Description: 评测结果
 *
 * Company: www.iflytek.com
 * @author iflytek
 * *
 * @date 2015年1月12日 下午4:58:38
 */
open class Result {
    /**
     * 评测语种：en（英文）、cn（中文）
     */
    var language: String? = null
    /**
     * 评测种类：read_syllable（cn单字）、read_word（词语）、read_sentence（句子）
     */
    var category: String? = null
    /**
     * 开始帧位置，每帧相当于10ms
     */
    var beg_pos: Int = 0
    /**
     * 结束帧位置
     */
    var end_pos: Int = 0
    /**
     * 评测内容
     */
    var content: String? = null
    /**
     * 总得分
     */
    var total_score: Float = 0.toFloat()
    /**
     * 时长（cn）
     */
    var time_len: Int = 0
    /**
     * 异常信息（en）
     */
    var except_info: String? = null
    /**
     * 是否乱读（cn）
     */
    var is_rejected: Boolean = false
    /**
     * xml结果中的sentence标签
     */
    var sentences: ArrayList<Sentence>? = null
}
