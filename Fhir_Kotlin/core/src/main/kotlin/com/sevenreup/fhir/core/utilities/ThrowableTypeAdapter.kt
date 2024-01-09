package com.sevenreup.fhir.core.utilities

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class ThrowableTypeAdapter : TypeAdapter<Throwable>() {

    override fun write(out: JsonWriter, value: Throwable?) {
        out.beginObject()
        if (value != null) {
            out.name("type").value(value::class.qualifiedName)
            out.name("message").value(value.message)
            // Serialize other required fields if needed, like stackTrace, cause, etc.
        }
        out.endObject()
    }

    override fun read(reader: JsonReader): Throwable {
        var typeName: String? = null
        var message: String? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "type" -> typeName = reader.nextString()
                "message" -> message = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        // Creating a generic exception as we can't instantiate Throwable directly
        // You may need to handle specific types if needed
        return Exception("Deserialized: Type: $typeName, Message: $message")
    }
}