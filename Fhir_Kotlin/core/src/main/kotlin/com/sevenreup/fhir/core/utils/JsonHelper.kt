package com.sevenreup.fhir.core.utils

import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant


fun createGson(): Gson {
    return GsonBuilder().registerTypeAdapter(Instant::class.java, InstantDeserializer()).registerTypeAdapter(Exception::class.java, ExceptionSerializer()).registerTypeAdapter(Exception::class.java, ExceptionDeserializer()).create()
}

class InstantDeserializer : JsonDeserializer<Instant?>, JsonSerializer<Instant?> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Instant? {
        return Instant.now()
    }

    override fun serialize(src: Instant?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return  JsonObject()
    }
}

class ExceptionDeserializer : JsonDeserializer<Exception?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Exception? {
        return null
    }
}

class ExceptionSerializer : JsonSerializer<Exception?> {
    override fun serialize(src: Exception?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.add("cause", JsonPrimitive(src?.cause.toString()))
        jsonObject.add("message", JsonPrimitive(src?.message ?: ""))
        return jsonObject
    }
}