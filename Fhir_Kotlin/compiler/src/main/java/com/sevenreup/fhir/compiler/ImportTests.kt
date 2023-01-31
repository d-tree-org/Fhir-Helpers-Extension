package com.sevenreup.fhir.compiler

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import org.hl7.fhir.r4.model.StructureMap
import com.sevenreup.fhir.compiler.structure_maps.createStructureMapFromFile
import com.sevenreup.fhir.compiler.utils.getAbsolutePath
import com.sevenreup.fhir.compiler.utils.getParentPath
import com.sevenreup.fhir.compiler.utils.readFile

private data class StructureHash(val mode: StructureMap.StructureMapModelMode, val url: String)

fun importTests(path: String) {
    val parentPath = path.getParentPath()
    val main = createStructureMapFromFile(path, "Main")

    main?.let { structureMap ->
        val iParser: IParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
        println("Original")
        println(iParser.encodeResourceToString(main))
        println("-------------")
        structureMap.import?.let { imports ->
            imports.forEach { import ->
                val importedMap = handleImports(parentPath,import.value)
                if (importedMap != null) {
                    main.structure = combineUses(main, importedMap)
                    main.group = combineGroups(main, importedMap)
                }
            }
        }
        main.import = null
        println(iParser.encodeResourceToString(main))
        println("\n\n")
        println(org.hl7.fhir.r4.utils.StructureMapUtilities.render(main))
    }
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

fun handleImports(parentPath: String,path: String): StructureMap? {
    val actualPath = path.getAbsolutePath(parentPath)
    return createStructureMapFromFile(actualPath, "Child")
}
