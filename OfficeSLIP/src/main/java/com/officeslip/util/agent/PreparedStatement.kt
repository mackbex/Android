package com.officeslip.util.agent

import com.officeslip.CONNECTION_DB
import com.officeslip.SysInfo
import com.officeslip.util.Common
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList


/**
 * Preparestatement for Agent.
 * @author Administrator
 *
 */

class PreparedStatement {

    private var query:String? = null
    private var listVal:ArrayList<Any> = ArrayList()
    private var nQMCnt = 0
    private var matcher:Matcher? = null
    private var m_C = Common()


    /**
     * Get query for prepareStatement.
     * @param strQuery
     */
//
    constructor(query:String){
        this.query = query

        val regEx = "[?]"

        val pat = Pattern.compile(regEx)
        matcher = pat.matcher(query)

        matcher?.let {
            while (it.find()) {
                nQMCnt++
            }

            it.reset()
        }
    }

    /**
     * Convert ? to actual value with index (the index is starting from 0)
     * @param nIdx
     * @param strValue
     */
    fun setColumnName(idx:Int, value:String) {
        listVal.add(idx, value)
    }

    fun setNull(idx:Int) {
        listVal.add(idx, "NULL")
    }

    fun setString(idx:Int, _value:String) {
        var value = _value
        if(m_C.isBlank(value)) value = ""
        listVal.add(idx, "'$value'")
    }

    fun setFunction(idx:Int, value:String) {
        listVal.add(idx, value)
    }

    fun setInt(idx:Int, value:Int) {
        listVal.add(idx, value)
    }

    fun setProcArray(idx:Int, _value:ArrayList<Any>) {

        var strValue = ""

        for(i in 0 until _value.size) {

            if(m_C.isBlank(strValue)) {
                strValue = "'${(_value[i] as String)}'"
            }
            else {
                strValue = "$strValue , '${(_value[i] as String)}'"
            }

            if(i >= _value.size - 1) {
                strValue += "'"
            }

        }

        listVal.add(idx, strValue)
    }

    fun setArray(idx:Int, _value: ArrayList<String>) {

        var strValue = ""

        for(i in 0 until _value.size) {

            if(_value[i] is String) {
                if(m_C.isBlank(strValue)) {
                    strValue = "'${(_value[i] as String)}'"
                }
                else {
                    strValue = "$strValue , '${(_value[i] as String)}'"
                }
            }
            else {
                if(m_C.isBlank(strValue)) {
                    strValue = (_value[i] as String)
                }
                else {
                    strValue = "$strValue , ${(_value[i] as String)}"
                }
            }
        }

        listVal.add(idx, strValue)
    }

    fun setDBDate(idx:Int) {

        when(CONNECTION_DB) {
            SysInfo.DataBase.ORACLE -> {
                listVal.add(idx,"CURRENT_TIMESTAMP")
            }
            else -> {
                listVal.add(idx,"getDate()")
            }
        }
    }

    class QuestionMarkNotMatchedException(msg:String) : Exception(msg)

    /**
     * Get result query. returns null if query parameter and value count is not matched.
     * @return
     * @throws QuestionMarkNotMatchedException
     */

    @Throws(QuestionMarkNotMatchedException::class)
    fun getQuery():String? {

        var res:String? = null

        if(listVal.size == nQMCnt) {

            res = query
            val replacedString = StringBuffer()

            var nIdx = 0

            matcher?.let {

                while(it.find()) {

                    var replaced:String? = null

                    if(listVal[nIdx] is String) {
                        replaced = listVal[nIdx] as String
                    }
                    else if(listVal[nIdx] is Int) {
                        replaced = "${listVal[nIdx] as Int}"
                    }
                    it.appendReplacement(replacedString, replaced)

                    nIdx ++
                }

                it.appendTail(replacedString)
                res = replacedString.toString()
            } ?: run {
                throw QuestionMarkNotMatchedException(
                    "Expected mark is : $nQMCnt, but received parameter count is ${listVal.size}"
                )
            }

        }
        else {
            throw QuestionMarkNotMatchedException(
                "Expected mark is : $nQMCnt, but received parameter count is ${listVal.size}"
            )
        }

        return res
    }

}
