import { TextDocument } from 'vscode';

export function isMapFile(document: TextDocument): boolean {
	return document.languageId === "map" && (document.uri.scheme === "file" || document.uri.scheme === "untitled");
}