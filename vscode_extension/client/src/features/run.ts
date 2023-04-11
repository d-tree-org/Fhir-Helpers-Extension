import {
  commands,
  EventEmitter,
  ProviderResult,
  Tab,
  TabInputText,
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
import { format } from "prettier";
import { temporaryDirectory, temporaryWrite } from "../core/util/temp";
import { createHash } from "node:crypto";
import path from "node:path";
import fs from "node:fs";

export default class RunCode implements Feature {
  conf: IServerManager;
  data?: any = "";
  currentDir = "";

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
    try {
      const doc = window.activeTextEditor.document;
    if (isTestMapFile(doc)) {
      const server = this.conf.server;
      if (server != null) {
        const data = await runConfMap(doc, server);
        this.openDoc(data, doc.fileName);
      }
    }
    } catch (error) {
      console.log(error);
      
    }
    
  }
  private init(subscriptions) {
    const myScheme = "cowsay";
    this.currentDir = temporaryDirectory();
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
  private async openDoc(data: any, fileName: string) {
    this.data = data;

    const text = JSON.stringify(cleanData(data));
    const filePath = await temporaryWrite(
      format(text, { parser: "json" }),
      getFileName(fileName),
      {
        dir: this.currentDir,
      }
    );
    const doc = await workspace.openTextDocument(filePath);
    const editor = await window.showTextDocument(doc, {
      preview: false,
      viewColumn: ViewColumn.Two,
    });
  }

  dispose(): void {
    if (this.currentDir !== "") {
      closeFileIfOpen(this.currentDir);
      fs.rmSync(this.currentDir, { recursive: true, force: true });
    }
  }
}

function getFileName(file: string) {
  const has = createHash("MD5", {});
  has.update(file);
  return `${path.basename(path.dirname(file))}-${has.digest("hex")}.json`;
}

function cleanData(data: any) {
  const cleaned = {};
  for (const key in data) {
    if (Object.prototype.hasOwnProperty.call(data, key)) {
      const element = data[key];
      const obj = JSON.parse(element);
      cleaned[key] = [obj];
    }
  }
  return cleaned;
}

async function closeFileIfOpen(dir: string): Promise<void> {
  const tabs: Tab[] = window.tabGroups.all.map((tg) => tg.tabs).flat();
  const index = tabs.findIndex(
    (tab) =>
      tab.input instanceof TabInputText && tab.input.uri.fsPath.includes(dir)
  );
  if (index !== -1) {
    await window.tabGroups.close(tabs[index]);
  }
}
