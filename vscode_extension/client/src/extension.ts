import { ExtensionContext } from "vscode";

import { Feature } from "./feature.type";
import { Formatter } from "./features/formatter";
import CompileStrMap from "./features/compile";
import { Server } from "./features/server";
import RunCode from "./features/run";
import { ServerManager } from "./core/server";

let context: ExtensionContext;
let extensionFeatures: Feature[] = [];
let serverManager: ServerManager;

export function activate(context: ExtensionContext) {
  this.context = context;
  serverManager = new ServerManager(context);

  extensionFeatures = [
    new CompileStrMap(context),
    new RunCode(serverManager),
    new Formatter(context.subscriptions),
    new Server(serverManager),
  ];
}

export function deactivate() {
  for (const feature of extensionFeatures) {
    feature.dispose();
  }

  for (const subscription of context.subscriptions) {
    subscription.dispose();
  }
}
