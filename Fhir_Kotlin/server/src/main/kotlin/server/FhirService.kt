package server

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService
import com.sevenreup.fhir.core.compiler.ResourceParser
import com.sevenreup.fhir.core.tests.StructureMapTests

@JsonRpcService
class FhirService {
    private var parser = ResourceParser()

    @JsonRpcMethod("compileStructureMap")
    fun callCompileStructureMap(@JsonRpcParam("path") path: String): String {
        try {
            val res = parser.parseStructureMapFromMap(path)

            if (res.error != null) {
                throw Exception(res.error)
            }

            return res.data!!
        } catch (e: Exception) {
            throw Exception("Something went wrong")
        }
    }

    @JsonRpcMethod("parseTransformFromJson")
    fun callTransformBatch(@JsonRpcParam("path") path: String): Map<String, String> {
        try {
            val res = parser.parseTransformFromJson(path)

            if (res.error != null) {
                throw Exception(res.error)
            }

            return res.data!!
        } catch (e: Exception) {
            throw Exception("Something went wrong")
        }
    }
}