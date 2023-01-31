package server

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService
import com.sevenreup.fhir.core.compiler.ResourceParser

@JsonRpcService
class FhirService {
    private var parser = ResourceParser()
    @JsonRpcMethod("compileStructureMap")
    fun callCompileStructureMap(@JsonRpcParam("path") path: String): String {
        val res = parser.parseStructureMapFromMap(path)

        if (res.error != null) {
            throw Exception(res.error)
        }

        return res.data!!
    }

    @JsonRpcMethod("parseTransformFromJson")
    fun callTransformBatch(@JsonRpcParam("path") path: String): Map<String, String> {
        val res = parser.parseTransformFromJson(path)

        if (res.error != null) {
            throw Exception(res.error)
        }

        return res.data!!
    }
}
