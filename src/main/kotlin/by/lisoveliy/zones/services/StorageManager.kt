package by.lisoveliy.zones.services

import by.lisoveliy.zones.models.jsonSerializable.Config
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class StorageManager(private val file: File) {
    var config: Config? = null
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun read() {
        val reader = InputStreamReader(FileInputStream(file), "UTF-8") //Because we all know how windows like UTF
        try {
            val data = JsonParser.parseReader(reader)
            val config = gson.fromJson(data, Config::class.java)
            this.config = config
        } catch (e: Exception) {
            when (e) {
                is JsonParseException -> {
                    logger.error("Error while reading JSON: $e")
                    throw e
                }

                else -> throw e
            }
        } finally {
            reader.close()
        }
    }

    fun write() {
        val writer = file.writer()
        val data = gson.toJson(config)
        writer.write(data)
        writer.close()
    }
}