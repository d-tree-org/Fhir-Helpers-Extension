import { ExtensionContext } from "vscode";

import { Feature } from "./feature.type";
import { Formatter } from "./features/formatter";
import { LanguageServer } from "./features/lsp";
import Preview from "./features/preview";

let context: ExtensionContext;
let extensionFeatures: Feature[] = [];

export function activate(context: ExtensionContext) {
  this.context = context;

  extensionFeatures = [
    new Preview(context),
    new Formatter(context.subscriptions),
    new LanguageServer(context),
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
