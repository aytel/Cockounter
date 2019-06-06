package com.example.cockounter.network

import com.example.cockounter.core.GameState
import com.example.cockounter.core.StateCapture
import com.example.cockounter.core.StateCaptureConverter
import org.junit.Assert.*
import org.junit.Test

class NetworkHandlerTest {
    //@Test
    fun testCreate() {
        val capture = StateCaptureConverter.gson.fromJson(
            "{\"date\":\"May 30, 2019 01:32:47\",\"name\":\"xd\",\"players\":[{\"name\":\"ооо\",\"role\":\"r\"}],\"preset\":{\"actionsStubs\":[{\"context\":\"ALL\",\"functionName\":\"aa\",\"script\":\"if saved.x \\u003d\\u003d nil then\\nsaved.x \\u003d 1\\nend\\ntoast(saved.x)\\nsaved.x \\u003d saved.x + 1\",\"visibleName\":\"aa\"}],\"globalParameters\":{\"d\":{\"type\":\"com.example.cockounter.core.IntegerParameter\",\"data\":{\"actionsStubs\":[{\"context\":\"NONE\",\"functionName\":\"k\",\"script\":\"toast(1)\",\"visibleName\":\"k\"}],\"initialValue\":0,\"name\":\"d\",\"visibleName\":\"d\"}}},\"libraries\":[],\"roles\":{\"r\":{\"actionsStubs\":[],\"name\":\"r\",\"privateParameters\":{},\"sharedParameters\":{}}}},\"state\":{\"globalParameters\":{\"d\":{\"type\":\"com.example.cockounter.core.IntegerGameParameter\",\"data\":{\"name\":\"d\",\"value\":0,\"visibleName\":\"d\"}}},\"roles\":{\"r\":{\"name\":\"r\",\"players\":{\"ооо\":{\"name\":\"ооо\",\"privateParameters\":{}}},\"sharedParameters\":{}}},\"version\":0},\"uuid\":\"00000000-0000-0001-0000-000000000001\"}",
            StateCapture::class.java)
        val result = NetworkHandler.createGame(capture)
        print(result)
    }
}