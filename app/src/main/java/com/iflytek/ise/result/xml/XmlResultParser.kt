/**

 */
package com.iflytek.ise.result.xml

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

import android.text.TextUtils
import android.util.Xml

import com.iflytek.ise.result.FinalResult
import com.iflytek.ise.result.ReadSentenceResult
import com.iflytek.ise.result.ReadSyllableResult
import com.iflytek.ise.result.ReadWordResult
import com.iflytek.ise.result.Result
import com.iflytek.ise.result.entity.Phone
import com.iflytek.ise.result.entity.Sentence
import com.iflytek.ise.result.entity.Syll
import com.iflytek.ise.result.entity.Word

/**
 *
 * Title: XmlResultParser
 *
 * Description:
 *
 * Company: www.iflytek.com
 * @author iflytek
 * *
 * @date 2015年1月12日 下午5:21:53
 */
class XmlResultParser {

    fun parse(xml: String?): Result? {
        if (TextUtils.isEmpty(xml)) {
            return null
        }

        val pullParser = Xml.newPullParser()

        try {
            val ins = ByteArrayInputStream(xml?.toByteArray())
            pullParser.setInput(ins, "utf-8")
            var finalResult: FinalResult? = null

            var eventType = pullParser.eventType
            while (XmlPullParser.END_DOCUMENT != eventType) {
                when (eventType) {
                    XmlPullParser.START_TAG -> if ("FinalResult" == pullParser.name) {
                        // 只有一个总分的结果
                        finalResult = FinalResult()
                    } else if ("ret" == pullParser.name) {
                        finalResult?.ret = getInt(pullParser, "value")
                    } else if ("total_score" == pullParser.name) {
                        finalResult?.total_score = getFloat(pullParser, "value")
                    } else if ("xml_result" == pullParser.name) {
                        // 详细结果
                        return parseResult(pullParser)
                    }
                    XmlPullParser.END_TAG -> if ("FinalResult" == pullParser.name) {
                        return finalResult
                    }

                    else -> {
                    }
                }
                eventType = pullParser.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    private fun parseResult(pullParser: XmlPullParser): Result {
        var result: Result? = null
        // <rec_paper>标签是否已扫描到
        var rec_paperPassed = false
        var sentence: Sentence? = null
        var word: Word? = null
        var syll: Syll? = null
        var phone: Phone? = null

        var eventType: Int
        try {
            eventType = pullParser.eventType
            while (XmlPullParser.END_DOCUMENT != eventType) {
                when (eventType) {
                    XmlPullParser.START_TAG -> if ("rec_paper" == pullParser.name) {
                        rec_paperPassed = true
                    } else if ("read_syllable" == pullParser.name) {
                        if (!rec_paperPassed) {
                            result = ReadSyllableResult()
                        } else {
                            readTotalResult(result!!, pullParser)
                        }
                    } else if ("read_word" == pullParser.name) {
                        if (!rec_paperPassed) {
                            result = ReadWordResult()
                            val lan = getLanguage(pullParser)
                            result.language = lan ?: "cn"
                        } else {
                            readTotalResult(result!!, pullParser)
                        }
                    } else if ("read_sentence" == pullParser.name || "read_chapter" == pullParser.name) {
                        if (!rec_paperPassed) {
                            result = ReadSentenceResult()
                            val lan = getLanguage(pullParser)
                            result.language = lan ?: "cn"
                        } else {
                            readTotalResult(result!!, pullParser)
                        }
                    } else if ("sentence" == pullParser.name) {
                        if (null == result?.sentences) {
                            result?.sentences = ArrayList<Sentence>()
                        }
                        sentence = createSentence(pullParser)
                    } else if ("word" == pullParser.name) {
                        if (null != sentence && null == sentence.words) {
                            sentence.words = ArrayList<Word>()
                        }
                        word = createWord(pullParser)
                    } else if ("syll" == pullParser.name) {
                        if (null != word && null == word.sylls) {
                            word.sylls = ArrayList<Syll>()
                        }
                        syll = createSyll(pullParser)
                    } else if ("phone" == pullParser.name) {
                        if (null != syll && null == syll.phones) {
                            syll.phones = ArrayList<Phone>()
                        }
                        phone = createPhone(pullParser)
                    }
                    XmlPullParser.END_TAG -> if ("phone" == pullParser.name) {
                        syll?.phones?.add(phone!!)
                    } else if ("syll" == pullParser.name) {
                        word?.sylls?.add(syll!!)
                    } else if ("word" == pullParser.name) {
                        sentence?.words?.add(word!!)
                    } else if ("sentence" == pullParser.name) {
                        result?.sentences?.add(sentence!!)
                    } else if ("read_syllable" == pullParser.name
                            || "read_word" == pullParser.name
                            || "read_sentence" == pullParser.name) {
                        return result!!
                    }

                    else -> {
                    }
                }

                eventType = pullParser.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result!!
    }

    private fun readTotalResult(result: Result, pullParser: XmlPullParser) {
        result.beg_pos = getInt(pullParser, "beg_pos")
        result.end_pos = getInt(pullParser, "end_pos")
        result.content = getContent(pullParser)
        result.total_score = getFloat(pullParser, "total_score")
        result.time_len = getInt(pullParser, "time_len")
        result.except_info = getExceptInfo(pullParser)
        result.is_rejected = getIsRejected(pullParser)
    }

    private fun createPhone(pullParser: XmlPullParser): Phone {
        val phone: Phone
        phone = Phone()
        phone.beg_pos = getInt(pullParser, "beg_pos")
        phone.end_pos = getInt(pullParser, "end_pos")
        phone.content = getContent(pullParser)
        phone.dp_message = getInt(pullParser, "dp_message")
        phone.time_len = getInt(pullParser, "time_len")
        return phone
    }

    private fun createSyll(pullParser: XmlPullParser): Syll {
        var syll: Syll
        syll = Syll()
        syll.beg_pos = getInt(pullParser, "beg_pos")
        syll.end_pos = getInt(pullParser, "end_pos")
        syll.content = getContent(pullParser)
        syll.symbol = getSymbol(pullParser)
        syll.dp_message = getInt(pullParser, "dp_message")
        syll.time_len = getInt(pullParser, "time_len")
        return syll
    }

    private fun createWord(pullParser: XmlPullParser): Word {
        val word: Word
        word = Word()
        word.beg_pos = getInt(pullParser, "beg_pos")
        word.end_pos = getInt(pullParser, "end_pos")
        word.content = getContent(pullParser)
        word.symbol = getSymbol(pullParser)
        word.time_len = getInt(pullParser, "time_len")
        word.dp_message = getInt(pullParser, "dp_message")
        word.total_score = getFloat(pullParser, "total_score")
        word.global_index = getInt(pullParser, "global_index")
        word.index = getInt(pullParser, "index")
        return word
    }

    private fun createSentence(pullParser: XmlPullParser): Sentence {
        val sentence: Sentence
        sentence = Sentence()
        sentence.beg_pos = getInt(pullParser, "beg_pos")
        sentence.end_pos = getInt(pullParser, "end_pos")
        sentence.content = getContent(pullParser)
        sentence.time_len = getInt(pullParser, "time_len")
        sentence.index = getInt(pullParser, "index")
        sentence.word_count = getInt(pullParser, "word_count")
        return sentence
    }

    private fun getLanguage(pullParser: XmlPullParser): String? {
        return pullParser.getAttributeValue(null, "lan")
    }

    private fun getExceptInfo(pullParser: XmlPullParser): String? {
        return pullParser.getAttributeValue(null, "except_info")
    }

    private fun getIsRejected(pullParser: XmlPullParser): Boolean {
        val isRejected = pullParser.getAttributeValue(null, "is_rejected") ?: return false

        return java.lang.Boolean.parseBoolean(isRejected)
    }

    private fun getSymbol(pullParser: XmlPullParser) : String?{
        return pullParser.getAttributeValue(null, "symbol")
    }

    private fun getFloat(pullParser: XmlPullParser, attrName: String): Float {
        val `val` = pullParser.getAttributeValue(null, attrName) ?: return 0f
        return java.lang.Float.parseFloat(`val`)
    }

    private fun getContent(pullParser: XmlPullParser): String? {
        return pullParser.getAttributeValue(null, "content")
    }

    private fun getInt(pullParser: XmlPullParser, attrName: String): Int {
        val `val` = pullParser.getAttributeValue(null, attrName) ?: return 0
        return Integer.parseInt(`val`)
    }
}
