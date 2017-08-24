package com.iflytek.speech.util

import java.io.ByteArrayInputStream
import java.io.InputStream

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Xml结果解析类
 */
object XmlParser {

    fun parseNluResult(xml: String): String {
        val buffer = StringBuffer()
        try {
            // DOM builder
            var domBuilder: DocumentBuilder? = null
            // DOM doc
            var domDoc: Document? = null

            // init DOM
            val domFact = DocumentBuilderFactory.newInstance()
            domBuilder = domFact.newDocumentBuilder()
            val `is` = ByteArrayInputStream(xml.toByteArray())
            domDoc = domBuilder!!.parse(`is`)

            // 获取根节点
            val root = domDoc!!.documentElement as Element

            val raw = root.getElementsByTagName("rawtext").item(0) as Element
            buffer.append("【识别结果】" + raw.firstChild.nodeValue)
            buffer.append("\n")

            val e = root.getElementsByTagName("result").item(0) as Element

            val focus = e.getElementsByTagName("focus").item(0) as Element
            buffer.append("【FOCUS】" + focus.firstChild.nodeValue)
            buffer.append("\n")

            val action = e.getElementsByTagName("action").item(0) as Element
            val operation = action.getElementsByTagName("operation").item(0) as Element
            buffer.append("【ACTION】" + operation.firstChild.nodeValue)
            buffer.append("\n")


        } catch (e: Exception) {
            e.printStackTrace()
        }

        buffer.append("【ALL】" + xml)
        return buffer.toString()
    }
}
