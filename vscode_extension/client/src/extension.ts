import { ExtensionContext, ProgressLocation, window } from "vscode";

import { Feature } from "./feature.type";
import { Formatter } from "./features/formatter";
import CompileStrMap from "./features/compile";
import RunCode from "./features/run";
import { ServerManager } from "./core/server";
import TestRunner from './testRunner';

let context: ExtensionContext;
let extensionFeatures: Feature[] = [];
let serverManager: ServerManager;

export function activate(context: ExtensionContext) {
  this.context = context;
  serverManager = new ServerManager(context);

  window.withProgress(
    {
      location: ProgressLocation.Notification,
      title: "Connecting to server",
      cancellable: false,
    },
    async (progress, token) => {
      progress.report({ increment: 20 });
      await serverManager.initServer();
      return null;
    }
  );

  serverManager.onConnection((state) => {
    if (state == "connected") {
      extensionFeatures = [
        new CompileStrMap(context),
        new RunCode(serverManager),
        new Formatter(context.subscriptions),
        new TestRunner(context,)
        // new Server(serverManager),
      ];
    } else if (state == "failed") {
      window.showInformationMessage("Failed to connect to server");
    }
  });
}

export function deactivate() {
  for (const feature of extensionFeatures) {
    feature.dispose();
  }

  for (const subscription of context.subscriptions) {
    subscription.dispose();
  }
}
