package com.bastarst.lexisrs.data.csv

import java.io.InputStream

object CsvParser {
    fun parse(input: InputStream): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        input.bufferedReader(Charsets.UTF_8).use { reader ->
            var line = reader.readLine()

            while (line != null) {
                if (line.isBlank()) {
                    line = reader.readLine()
                    continue
                }

                val cells = line.split(";", limit = 2)

                if (cells.size == 2) {
                    val en = cells[0].trim()
                    val ru = cells[1].trim()
                    if (en.isNotEmpty() && ru.isNotEmpty()) {
                        result.add(en to ru)
                    }
                }

                line = reader.readLine()
            }
        }
        return result
    }
}