package com.sevenreup.fhir.lisp.semantictokens

import org.eclipse.lsp4j.SemanticTokenModifiers
import org.eclipse.lsp4j.SemanticTokenTypes
import org.eclipse.lsp4j.SemanticTokensLegend

enum class SemanticTokenType(val typeName: String) {
    KEYWORD(SemanticTokenTypes.Keyword),
    VARIABLE(SemanticTokenTypes.Variable),
    FUNCTION(SemanticTokenTypes.Function),
    PROPERTY(SemanticTokenTypes.Property),
    PARAMETER(SemanticTokenTypes.Parameter),
    ENUM_MEMBER(SemanticTokenTypes.EnumMember),
    CLASS(SemanticTokenTypes.Class),
    INTERFACE(SemanticTokenTypes.Interface),
    ENUM(SemanticTokenTypes.Enum),
    TYPE(SemanticTokenTypes.Type),
    STRING(SemanticTokenTypes.String),
    NUMBER(SemanticTokenTypes.Number),
    // Since LSP does not provide a token type for string interpolation
    // entries, we use Variable as a fallback here for now
    INTERPOLATION_ENTRY(SemanticTokenTypes.Variable)
}

enum class SemanticTokenModifier(val modifierName: String) {
    DECLARATION(SemanticTokenModifiers.Declaration),
    DEFINITION(SemanticTokenModifiers.Definition),
    ABSTRACT(SemanticTokenModifiers.Abstract),
    READONLY(SemanticTokenModifiers.Readonly)
}

val semanticTokensLegend = SemanticTokensLegend(
    SemanticTokenType.values().map { it.typeName },
    SemanticTokenModifier.values().map { it.modifierName }
)