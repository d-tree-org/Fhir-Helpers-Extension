import {
  commands,
  window,
} from "vscode";
import { Feature } from "../feature.type";
import { isTestMapFile } from "../utils";
import { AvailableCommands } from "../utils/constants";
import { runConfMap } from "../core/run";
import { IServerManager } from "../core/server";

export default class RunCode implements Feature {
  conf: IServerManager;

  constructor(serverConf: IServerManager) {
    this.conf = serverConf;
    this.conf.context.subscriptions.push(
      commands.registerCommand(AvailableCommands.runFile, () => {
        this.run();
      })
    );
  }

  private async run() {
    const doc = window.activeTextEditor.document;
    if (isTestMapFile(doc)) {
      const server = this.conf.server;
      if (server != null) {
        const data = await runConfMap(doc, server);
        console.log(data);
      }
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  dispose(): void {}
}
