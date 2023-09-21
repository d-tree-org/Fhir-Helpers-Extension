import * as vscode from "vscode";
import * as path from "path";

export function getFileWorkSpacePath(file: string): string | undefined {
  return vscode.workspace.workspaceFolders?.find((wsFolder) => {
    const relative = path.relative(wsFolder.uri.fsPath, file);
    return relative && !relative.startsWith("..") && !path.isAbsolute(relative);
  }).uri.fsPath;
}
