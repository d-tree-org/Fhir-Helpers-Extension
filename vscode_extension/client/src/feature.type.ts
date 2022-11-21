import { ExtensionContext, WorkspaceConfiguration } from "vscode";

export interface Feature {
  dispose(): void;
}

export interface ServerSetupParams {
  context: ExtensionContext;
  config: WorkspaceConfiguration;
}
