/**

 */
package com.iflytek.ise.result.util

import java.util.HashMap

/**
 *
 * Title: ResultTranslateUtl
 *
 * Description:
 *
 * Company: www.iflytek.com
 * @author iflytek
 * *
 * @date 2015年1月13日 下午6:05:03
 */
object ResultTranslateUtil {

    private val dp_message_map = HashMap<Int, String>()
    private val special_content_map = HashMap<String, String>()

    init {
        dp_message_map.put(0, "正常")
        dp_message_map.put(16, "漏读")
        dp_message_map.put(32, "增读")
        dp_message_map.put(64, "回读")
        dp_message_map.put(128, "替换")

        special_content_map.put("sil", "静音")
        special_content_map.put("silv", "静音")
        special_content_map.put("fil", "噪音")
    }

    fun getDpMessageInfo(dp_message: Int): String {
        return dp_message_map[dp_message]!!
    }

    fun getContent(content: String): String {
        val `val` = special_content_map[content]
        return `val` ?: content
    }
}
