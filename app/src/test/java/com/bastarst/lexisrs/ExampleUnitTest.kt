package com.bastarst.lexisrs

import com.bastarst.lexisrs.data.csv.CsvParser
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val path = "src/main/assets/Oxford_3000_English_Russian.csv"
        val list = CsvParser.parse(java.io.File(path).inputStream())
        for (i in 1..10) {
            println(list[i].first + ";" + list[i].second)
        }
    }
}