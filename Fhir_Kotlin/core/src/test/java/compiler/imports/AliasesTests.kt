package compiler.imports

import com.sevenreup.fhir.core.compiler.imports.replaceAliases
import kotlin.test.Test
import kotlin.test.assertEquals

class AliasesTests {
    private val aliases = mapOf(
        "@/*" to "./structure_map",
        "@util/*" to "./utilities",
        "~/*" to "./structure_map",
        "~util/*" to "./utilities"
    )

    @Test
    fun replaceAliasesTestRelative() {
        val inputs = "./file1.txt"

        val output = replaceAliases(inputs, aliases)
        assertEquals("./file1.txt", output)
    }

    @Test
    fun replaceAliasesTestSymbol() {
        val inputs = "@/file1.txt"

        val output = replaceAliases(inputs, aliases)
        assertEquals("./structure_map/file1.txt", output)
    }

    @Test
    fun replaceAliasesTestSymbolWithText() {
        val inputs = "@util/some_util.kt"

        val output = replaceAliases(inputs, aliases)
        assertEquals("./utilities/some_util.kt", output)
    }

    @Test
    fun replaceAliasesTestDifferentSymbol() {
        val inputs = "~/file1.txt"

        val output = replaceAliases(inputs, aliases)
        assertEquals("./structure_map/file1.txt", output)
    }
}