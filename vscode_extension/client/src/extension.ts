import { ExtensionContext, window } from "vscode";

import { Feature } from "./feature.type";
import { Formatter } from "./features/formatter";
import CompileStrMap from "./features/compile";
import RunCode from "./features/run";
import { ServerManager } from "./core/server";
import TestRunner from "./testRunner";

let context: ExtensionContext;
let extensionFeatures: Feature[] = [];
let serverManager: ServerManager;

export function activate(context: ExtensionContext) {
  this.context = context;
  serverManager = new ServerManager(context);

  serverManager.onConnection((state) => {
    if (state == "connected") {
      extensionFeatures = [
        new CompileStrMap(context, serverManager),
        new RunCode(serverManager),
        new Formatter(context.subscriptions, serverManager),
        new TestRunner(context, serverManager),
      ];
    } else if (state == "failed") {
      window.showInformationMessage("Failed to connect to server");
    }
  });

  serverManager.initServer();
}

export function deactivate() {
  for (const feature of extensionFeatures) {
    feature.dispose();
  }

  for (const subscription of context.subscriptions) {
    subscription.dispose();
  }

  serverManager.dispose();
}
