/**

 */
package com.iflytek.ise.result

import com.iflytek.ise.result.util.ResultFormatUtil

/**
 *
 * Title: ReadWordResult
 *
 * Description:
 *
 * Company: www.iflytek.com
 * @author iflytek
 * *
 * @date 2015年1月12日 下午5:03:50
 */
class ReadWordResult : Result() {
    init {
        category = "read_word"
    }

    override fun toString(): String {
        val buffer = StringBuffer()

        if ("cn" == language) {
            buffer.append("[总体结果]\n")
                    .append("评测内容：" + content + "\n")
                    .append("朗读时长：" + time_len + "\n")
                    .append("总分：" + total_score + "\n\n")
                    .append("[朗读详情]")
                    .append(ResultFormatUtil.formatDetails_CN(sentences))
        } else {
            if (is_rejected) {
                buffer.append("检测到乱读，")
                        .append("except_info:" + except_info + "\n\n")    // except_info代码说明详见《语音评测参数、结果说明文档》
            }

            buffer.append("[总体结果]\n")
                    .append("评测内容：" + content + "\n")
                    .append("总分：" + total_score + "\n\n")
                    .append("[朗读详情]")
                    .append(ResultFormatUtil.formatDetails_EN(sentences))
        }

        return buffer.toString()
    }
}
