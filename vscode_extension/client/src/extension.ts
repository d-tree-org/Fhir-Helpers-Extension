import { ExtensionContext, workspace } from "vscode";

import { Feature } from "./feature.type";
import { Formatter } from "./features/formatter";
import RunCode from "./features/run";
import { Server } from './features/server';

let context: ExtensionContext;
let extensionFeatures: Feature[] = [];

export function activate(context: ExtensionContext) {
  this.context = context;

  extensionFeatures = [
    new RunCode(context),
    new Formatter(context.subscriptions),
    new Server(context)
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
