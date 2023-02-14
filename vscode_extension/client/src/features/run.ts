import {
  commands,
  EventEmitter,
  ProviderResult,
  TextDocumentContentProvider,
  Uri,
  ViewColumn,
  window,
  workspace,
} from "vscode";
import { Feature } from "../feature.type";
import { isTestMapFile } from "../utils";
import { AvailableCommands } from "../utils/constants";
import { runConfMap } from "../core/run";
import { IServerManager } from "../core/server";
import { temporaryWrite } from "../core/util/temp";

export default class RunCode implements Feature {
  conf: IServerManager;
  data?: any = "";

  constructor(serverConf: IServerManager) {
    this.conf = serverConf;
    this.conf.context.subscriptions.push(
      commands.registerCommand(AvailableCommands.runFile, () => {
        this.run();
      })
    );
    this.init(this.conf.context.subscriptions);
  }

  private async run() {
    const doc = window.activeTextEditor.document;
    if (isTestMapFile(doc)) {
      const server = this.conf.server;
      if (server != null) {
        const data = await runConfMap(doc, server);
        this.openDoc(data);
      }
    }
  }
  private init(subscriptions) {
    const myScheme = "cowsay";
    const myProvider = new (class implements TextDocumentContentProvider {
      onDidChangeEmitter = new EventEmitter<Uri>();
      onDidChange = this.onDidChangeEmitter.event;

      provideTextDocumentContent(uri: Uri): ProviderResult<string> {
        return new Promise<string>((resolve, reject) => {
          workspace.fs.readFile(Uri.file(uri.fsPath)).then(
            (data) => {
              const sampleText = new TextDecoder("utf-8").decode(data);
              resolve(sampleText);
            },
            (reason) => {
              reject(reason);
            }
          );
        });
      }
    })();
    subscriptions.push(
      workspace.registerTextDocumentContentProvider(myScheme, myProvider)
    );
  }
  private async openDoc(data: any) {
    this.data = data;
    const path = await temporaryWrite(
      JSON.stringify(cleanData(data), null, 0),
      "temp.json"
    );
    const uri = Uri.parse(`cowsay:${path}`);
    const doc = await workspace.openTextDocument(uri);
    await window.showTextDocument(doc, {
      preview: false,
      viewColumn: ViewColumn.Two,
    });
  }

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  dispose(): void {}
}

function cleanData(data: any) {
  for (const key in data) {
    if (Object.prototype.hasOwnProperty.call(data, key)) {
      const element = data[key];
      const obj = JSON.parse(element);
      data[key] = obj;
    }
  }
  return data;
}
