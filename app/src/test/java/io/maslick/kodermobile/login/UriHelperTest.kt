package io.maslick.kodermobile.login

import io.maslick.kodermobile.helper.UriHelper
import org.junit.Assert
import org.junit.Test

class UriHelperTest {
    @Test
    fun splitQuery() {
        val splitMap = UriHelper.splitQuery("www.maslick.io/index.php?param1=value1&param2=value2&param3=value3")
        Assert.assertEquals("value1", splitMap["param1"])
        Assert.assertEquals("value2", splitMap["param2"])
        Assert.assertEquals("value3", splitMap["param3"])
    }
}