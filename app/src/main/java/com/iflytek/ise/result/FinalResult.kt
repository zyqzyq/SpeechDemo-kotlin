/**

 */
package com.iflytek.ise.result

/**
 *
 * Title: FinalResult
 *
 * Description:
 *
 * Company: www.iflytek.com
 * @author iflytek
 * *
 * @date 2015年1月14日 上午11:12:58
 */
class FinalResult : Result() {

    var ret: Int = 0

    // var total_score: Float? = null

    override fun toString(): String {
        return "返回值：$ret，总分：$total_score"
    }
}
