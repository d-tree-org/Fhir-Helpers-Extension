import {
  commands,
  Disposable,
  ExtensionContext,
  OutputChannel,
  ProgressLocation,
  TextDocument,
  TextDocumentChangeEvent,
  TextEditor,
  ViewColumn,
  WebviewPanel,
  window,
  workspace,
} from "vscode";
import { Feature } from "../feature.type";
import { isMapFile } from "../utils";
import { AvailableCommands } from "../utils/constants";
import { compileMap } from "../core/rpc/compile";
import { IServerManager } from "../core/server";
import { getFileWorkSpacePath } from "../utils/workspace";
import {
  closeFileIfOpen,
  getFileName,
  temporaryDirectory,
  temporaryWrite,
} from "../core/util/temp";
import { format } from "prettier";
import fs from "node:fs";

export default class CompileStrMap implements Feature {
  private openedDocuments: string[] = [];
  private previewOpen = false;
  private subscriptions: Disposable[];
  private webview: WebviewPanel | undefined;
  private context: ExtensionContext;
  private conf: IServerManager;
  currentDir = "";

  constructor(context: ExtensionContext, serverConf: IServerManager) {
    this.context = context;
    this.subscriptions = context.subscriptions;
    this.conf = serverConf;
    this.currentDir = temporaryDirectory();
    this.subscriptions.push(
      commands.registerCommand(AvailableCommands.compile, () => {
        window.showInformationMessage("Structure Map Compilation Started");
        getOutputChannel().clear();
        if (window.activeTextEditor) {
          this.previewOpen = true;
          window.withProgress(
            {
              location: ProgressLocation.Window,
              title: "Compiling Structure Map",
              cancellable: false,
            },
            (progress) => {
              progress.report({ increment: 20 });
              return this.displayWebView(window.activeTextEditor.document);
            }
          );
        } else {
          window.showErrorMessage("Active editor doesn't show a Map document.");
        }
      })
    );

  }

  listenToChanges() {
    this.subscriptions.push(
      workspace.onDidOpenTextDocument((document?: TextDocument) => {
        if (
          document &&
          this.previewOpen &&
          workspace.getConfiguration("map").autoPreview
        ) {
          this.displayWebView(document);
        }
      }),

      window.onDidChangeActiveTextEditor((editor?: TextEditor) => {
        if (
          editor &&
          this.previewOpen &&
          workspace.getConfiguration("map").autoPreview
        ) {
          this.displayWebView(editor.document);
        }
      }),

      workspace.onDidChangeTextDocument((event?: TextDocumentChangeEvent) => {
        if (
          event &&
          this.previewOpen &&
          workspace.getConfiguration("map").updateWhenTyping
        ) {
          this.displayWebView(event.document);
        }
      }),

      workspace.onDidSaveTextDocument((document?: TextDocument) => {
        if (document && this.previewOpen) {
          this.displayWebView(document);
        }
      }),

      workspace.onDidCloseTextDocument((document?: TextDocument) => {
        if (document && this.previewOpen && this.webview) {
          this.removeDocument(document.fileName);

          if (
            this.openedDocuments.length === 0 &&
            workspace.getConfiguration("map").autoClosePreview
          ) {
            this.dispose();
          }
        }
      })
    );
  }

  dispose(): void {
    if (this.webview !== undefined) {
      this.webview.dispose();
      closeFileIfOpen(this.currentDir);
      fs.rmSync(this.currentDir, { recursive: true, force: true });
    }
  }

  private async displayWebView(document: TextDocument): Promise<void> {
    if (!isMapFile(document)) {
      return;
    }

    const activeTextEditor: TextEditor | undefined = window.activeTextEditor;
    if (!activeTextEditor || !activeTextEditor.document) {
      return;
    }
    try {
      const content = await this.getContent(document);
      const filePath = await temporaryWrite(
        format(content, { parser: "json" }),
        getFileName(activeTextEditor.document.fileName),
        {
          dir: this.currentDir,
        }
      );
      // const textDocument = await workspace.openTextDocument({
      //   language: "json",
      //   content: content,
      // });
      const doc = await workspace.openTextDocument(filePath);
      const editor = await window.showTextDocument(doc, {
        preview: false,
        viewColumn: ViewColumn.Two,
      });

      commands.executeCommand("editor.action.formatDocument");
    } catch (e) {
      getOutputChannel().appendLine(e);
      getOutputChannel().show(true);
      window.showErrorMessage("Failed to compile Structure Map Compilation");
    }
  }

  private async getContent(document: TextDocument): Promise<string> {
    const path = document.uri.fsPath;
    const html: string = await compileMap(
      path,
      getFileWorkSpacePath(path),
      this.conf.server
    );

    if (html) {
      this.addDocument(document.fileName);
      return html;
    }

    return this.error("Active editor doesn't show a Map document.");
  }

  private error(error: string): string {
    return `<body>${error}</body>`;
  }

  private addDocument(fileName: string): void {
    if (this.openedDocuments.indexOf(fileName) === -1) {
      this.openedDocuments.push(fileName);
    }
  }

  private removeDocument(fileName: string): void {
    this.openedDocuments = this.openedDocuments.filter(
      (file: string) => file !== fileName
    );
  }
}

let _channel: OutputChannel;
function getOutputChannel(): OutputChannel {
  if (!_channel) {
    _channel = window.createOutputChannel("Structure Map Compiler");
  }
  return _channel;
}
