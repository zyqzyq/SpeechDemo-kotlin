/**

 */
package com.iflytek.ise.result

import com.iflytek.ise.result.util.ResultFormatUtil

/**
 *
 * Title: ReadSyllableResult
 *
 * Description: 中文单字评测结果
 *
 * Company: www.iflytek.com
 * @author iflytek
 * *
 * @date 2015年1月12日 下午5:03:14
 */
class ReadSyllableResult : Result() {
    init {
        language = "cn"
        category = "read_syllable"
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append("[总体结果]\n")
                .append("评测内容：" + content + "\n")
                .append("朗读时长：" + time_len + "\n")
                .append("总分：" + total_score + "\n\n")
                .append("[朗读详情]").append(ResultFormatUtil.formatDetails_CN(sentences))

        return buffer.toString()
    }
}
