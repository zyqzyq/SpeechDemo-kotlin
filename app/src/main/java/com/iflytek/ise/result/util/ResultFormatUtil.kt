/**

 */
package com.iflytek.ise.result.util

import java.util.ArrayList

import com.iflytek.ise.result.entity.Phone
import com.iflytek.ise.result.entity.Sentence
import com.iflytek.ise.result.entity.Syll
import com.iflytek.ise.result.entity.Word

/**
 *
 * Title: ResultFormatUtl
 *
 * Description:
 *
 * Company: www.iflytek.com
 * @author iflytek
 * *
 * @date 2015年1月19日 上午10:01:14
 */
object ResultFormatUtil {

    /**
     * 将英语评测详情按格式输出

     * @param sentences
     * *
     * @return 英语评测详情
     */
    fun formatDetails_EN(sentences: ArrayList<Sentence>?): String {
        val buffer = StringBuffer()
        if (null == sentences) {
            return buffer.toString()
        }

        for (sentence in sentences) {
            if ("噪音" == ResultTranslateUtil.getContent(sentence.content!!) || "静音" == ResultTranslateUtil.getContent(sentence.content!!)) {
                continue
            }

            if (null == sentence.words) {
                continue
            }
            for (word in sentence.words!!) {
                if ("噪音" == ResultTranslateUtil.getContent(word.content!!) || "静音" == ResultTranslateUtil.getContent(word.content!!)) {
                    continue
                }

                buffer.append("\n单词[" + ResultTranslateUtil.getContent(word.content!!) + "] ")
                        .append("朗读：" + ResultTranslateUtil.getDpMessageInfo(word.dp_message))
                        .append(" 得分：" + word.total_score)
                if (null == word.sylls) {
                    buffer.append("\n")
                    continue
                }

                for (syll in word.sylls!!) {
                    buffer.append("\n└音节[" + ResultTranslateUtil.getContent(syll.stdSymbol) + "] ")
                    if (null == syll.phones) {
                        continue
                    }

                    for (phone in syll.phones!!) {
                        buffer.append("\n\t└音素[" + ResultTranslateUtil.getContent(phone.stdSymbol) + "] ")
                                .append(" 朗读：" + ResultTranslateUtil.getDpMessageInfo(phone.dp_message))
                    }

                }
                buffer.append("\n")
            }
        }

        return buffer.toString()
    }

    /**
     * 将汉语评测详情按格式输出

     * @param sentences
     * *
     * @return 汉语评测详情
     */
    fun formatDetails_CN(sentences: ArrayList<Sentence>?): String {
        val buffer = StringBuffer()
        if (null == sentences) {
            return buffer.toString()
        }

        for (sentence in sentences) {
            if (null == sentence.words) {
                continue
            }

            for (word in sentence.words!!) {
                buffer.append("\n词语[" + ResultTranslateUtil.getContent(word.content!!) + "] " + word.symbol + " 时长：" + word.time_len)
                if (null == word.sylls) {
                    continue
                }

                for (syll in word.sylls!!) {
                    if ("噪音" == ResultTranslateUtil.getContent(syll.content!!) || "静音" == ResultTranslateUtil.getContent(syll.content!!)) {
                        continue
                    }

                    buffer.append("\n└音节[" + ResultTranslateUtil.getContent(syll.content!!) + "] " + syll.symbol + " 时长：" + syll.time_len)
                    if (null == syll.phones) {
                        continue
                    }

                    for (phone in syll.phones!!) {
                        buffer.append("\n\t└音素[" + ResultTranslateUtil.getContent(phone.content!!) + "] " + "时长：" + phone.time_len)
                                .append(" 朗读：" + ResultTranslateUtil.getDpMessageInfo(phone.dp_message))
                    }

                }
                buffer.append("\n")
            }
        }

        return buffer.toString()
    }
}
