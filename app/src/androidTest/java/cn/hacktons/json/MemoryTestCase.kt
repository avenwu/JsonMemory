package cn.hacktons.json

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.alibaba.fastjson.JSON.DEFAULT_GENERATE_FEATURE
import org.json.JSONStringer
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MemoryTestCase {


    private fun largeData(): String {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val stream = appContext.resources.assets.open("sample.txt")
        val bytes = stream.readBytes()
        assertTrue("sample json is less than 20MB", bytes.size > 20 * 1024 * 1024)
        val content = String(bytes)
        assertNotNull(content)
        return content
    }

    @Test
    fun orgDotJson() {
        val content = largeData()
        val jsonObject = org.json.JSONObject()
        jsonObject.put("data", content)
        val stringer = JSONStringer()
        stringer.`object`()
        jsonObject.keys().forEach { key ->
            stringer.key(key).value(jsonObject.get(key))
        }
        stringer.endObject()
        stringer.toString()
        stringer.javaClass
        val outField = JSONStringer::class.java.getDeclaredField("out")
        outField.isAccessible = true
        val builder = Reflect.from(stringer).valueOf<StringBuilder>("out")
        printUsage("org json", content.length, builder.count(), builder.capacity())
        assertTrue(builder.count() > content.length)
        assertTrue(builder.capacity() > content.length * 1.6)
        builder.clear()
    }

    @Test
    fun fastjson() {
        val content = largeData()
        val jsonObject = com.alibaba.fastjson.JSONObject()
        jsonObject["data"] = content
        val out = com.alibaba.fastjson.serializer.SerializeWriter(null, DEFAULT_GENERATE_FEATURE)
        val serializer = com.alibaba.fastjson.serializer.JSONSerializer(
            out,
            com.alibaba.fastjson.serializer.SerializeConfig.globalInstance
        )
        serializer.write(jsonObject)
        out.toString()
        val count = Reflect.from(out).valueOf<Int>("count")
        val capacity = Reflect.from(out).valueOf<CharArray>("buf").size
        printUsage("fast json", content.length, count, capacity)
        assertTrue(count > content.length)
        assertTrue(capacity > content.length * 2)
        out.close()
    }

    private fun printUsage(tag: String, length: Int, decoded: Int, capacity: Int) {
        Log.i("JSON", "data length=$length")
        Log.i(
            "JSON",
            "$tag count=" + decoded + ", ratio=" + (decoded * 1.0f / decoded)
        )
        Log.i(
            "JSON",
            "$tag capacity=" + capacity + ", ratio=" + (capacity * 1.0f / length)
        )
    }
}