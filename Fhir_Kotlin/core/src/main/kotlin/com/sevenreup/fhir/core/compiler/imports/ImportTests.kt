package com.sevenreup.fhir.core.compiler.imports

import ca.uhn.fhir.parser.IParser
import com.sevenreup.fhir.core.compiler.parsing.ParseJsonCommands
import com.sevenreup.fhir.core.config.CompileMode
import com.sevenreup.fhir.core.config.ProjectConfig
import com.sevenreup.fhir.core.utils.getAbsolutePath
import com.sevenreup.fhir.core.utils.getParentPath
import com.sevenreup.fhir.core.utils.readFile
import org.hl7.fhir.r4.model.StructureMap
import org.hl7.fhir.r4.utils.StructureMapUtilities

private data class StructureHash(val mode: StructureMap.StructureMapModelMode, val url: String)

fun handleImports(
    path: String,
    iParser: IParser,
    scu: StructureMapUtilities,
    projectConfigs: ProjectConfig
): StructureMap? {
    if (projectConfigs.compileMode == CompileMode.Debug) {
        println("Import Data -${projectConfigs}_$path")
    }
    val main = scu.parse(path.readFile(), ParseJsonCommands.getSrcName(path))

    main?.let { structureMap ->
        structureMap.import?.let { imports ->
            imports.forEach { import ->
                val importedMap = handleImports(
                    resolvePath(import.valueAsString, path.getParentPath(), projectConfigs),
                    iParser,
                    scu,
                    projectConfigs
                )
                if (importedMap != null) {
                    main.structure = combineUses(main, importedMap)
                    main.group = combineGroups(main, importedMap)
                }
            }
        }
        main.import = null
        return main
    }
    return null
}

fun combineGroups(main: StructureMap, include: StructureMap): List<StructureMap.StructureMapGroupComponent> {
    val mainGroups = mutableMapOf(*main.group.map { group ->
        Pair(group.name, group)
    }.toTypedArray())

    include.group.forEach { group ->
        if (!mainGroups.containsKey(group.name)) {
            mainGroups[group.name] = group
        } else {
            // TODO: Flag error for including the same name, or check out how to handle alias
        }
    }

    return mainGroups.values.toList()
}

fun combineUses(main: StructureMap, include: StructureMap): List<StructureMap.StructureMapStructureComponent> {
    val mainStructures = mutableMapOf(*main.structure.map { structure ->
        Pair(StructureHash(structure.mode, structure.url), structure)
    }.toTypedArray())

    include.structure.forEach { structure ->
        val hashCheck = StructureHash(structure.mode, structure.url)

        if (!mainStructures.containsKey(hashCheck)) {
            mainStructures[hashCheck] = structure
        }
    }
    return mainStructures.values.toList()
}

fun resolvePath(path: String, parent: String, projectConfigs: ProjectConfig): String {
    return replaceAliases(path, projectConfigs.aliases).let {
        if (path == it) {
            it.getAbsolutePath(parent)
        } else {
            it.getAbsolutePath(projectConfigs.basePath ?: "")
        }
    }
}

fun replaceAliases(input: String, aliases: Map<String, String>): String {
    var output = input
    for ((alias, path) in aliases) {
        val regex = Regex(alias.replace("\\", "\\\\"))
        val split = input.split(regex)
        if (split.size <= 1) continue
        output = "$path/${split[1]}"
    }
    return output
}
