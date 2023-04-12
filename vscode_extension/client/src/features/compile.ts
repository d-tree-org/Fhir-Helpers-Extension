import {
  commands,
  Disposable,
  ExtensionContext,
  OutputChannel,
  Position,
  ProgressLocation,
  Range,
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
import { compileMap } from "../core/compile";

export default class CompileStrMap implements Feature {
  private openedDocuments: string[] = [];
  private previewOpen = false;
  private subscriptions: Disposable[];
  private webview: WebviewPanel | undefined;
  private context: ExtensionContext;

  constructor(context: ExtensionContext) {
    this.context = context;
    this.subscriptions = context.subscriptions;
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
            (progress, token) => {
              progress.report({ increment: 20 });
              return this.displayWebView(window.activeTextEditor.document);
            }
          );
        } else {
          window.showErrorMessage("Active editor doesn't show a Map document.");
        }
      }),

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

      const textDocument = await workspace.openTextDocument({
        language: "json",
        content: content,
      });

      const textEditor = await window.showTextDocument(textDocument, {
        preview: false,
        preserveFocus: false,
        viewColumn: ViewColumn.Two,
        selection: new Range(new Position(0, 0), new Position(0, 0)),
      });

      commands.executeCommand("editor.action.formatDocument");
    } catch (e) {
      getOutputChannel().appendLine(e);
      getOutputChannel().show(true);
      window.showErrorMessage("Failed to compile Structure Map Compilation");
    }
  }

  private async getContent(document: TextDocument): Promise<string> {
    const html: string = await compileMap(
      document.getText(),
      document.uri.fsPath,
      this.context
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
