import {
  commands,
  Disposable,
  languages,
  Position,
  Range,
  TextDocument,
  TextEdit,
  TextEditor,
  TextEditorEdit,
  window,
} from "vscode";
import { JSONRPCClient } from "json-rpc-2.0";
import { Feature } from "../feature.type";

export class Server implements Feature {
  constructor(subscriptions: Disposable[]) {
    subscriptions.push();
  }

  stuff() {
    // const server = new JSONRPCClient();
  }
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  dispose(): void {}
}
