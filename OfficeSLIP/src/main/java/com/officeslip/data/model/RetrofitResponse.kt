package com.officeslip.data.model

import com.officeslip.Status
import com.officeslip.util.log.Logger


data class RetrofitResponse<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {
        fun <T> success(data: T? = null): AgentResponse<T> {
            return AgentResponse(
                Status.SUCCESS,
                data,
                null
            )
        }

        fun <T> error(msg: String, data: T? = null, e:Throwable? = null): AgentResponse<T> {
            Logger.error(msg, e)
            return AgentResponse(
                Status.ERROR,
                data,
                msg
            )
        }

        fun <T> loading(data: T? = null): AgentResponse<T> {
            return AgentResponse(
                Status.LOADING,
                data,
                null
            )
        }
    }
}

