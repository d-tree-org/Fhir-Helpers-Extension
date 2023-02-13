import {
  commands,
  ExtensionContext,
  StatusBarAlignment,
  StatusBarItem,
  window,
} from "vscode";
import { JSONRPCClient } from "json-rpc-2.0";
import { Feature } from "../feature.type";
import { WebSocket } from "ws";
import axios from "axios";

interface ServerState {
  state: "loading" | "connected" | "failed";
  webSocket?: WebSocket;
  server?: JSONRPCClient;
}

export class Server implements Feature {
  myStatusBarItem: StatusBarItem;
  state: ServerState = {
    state: "connected",
  };

  constructor({ subscriptions }: ExtensionContext) {
    const myCommandId = "fhir-map.showSelectionCount";
    console.log(myCommandId);

    subscriptions.push(
      commands.registerCommand(myCommandId, () => {
        if (this.state.state !== "loading") {
          window.showInformationMessage(
            `Yeah, line(s) selected... Keep going!`
          );
          this.state.server
            .request("compileStructureMap", {
              path: "p",
            })
            .then((data) => {
              console.log(data);
            });
        }
      })
    );

    // create a new status bar item that we can now manage
    this.myStatusBarItem = window.createStatusBarItem(
      StatusBarAlignment.Right,
      100
    );
    this.myStatusBarItem.command = myCommandId;
    this.myStatusBarItem.tooltip = "Keep on going";
    subscriptions.push(this.myStatusBarItem);

    this._statusItemChange(false);
    this.initServer();
  }

  initServer() {
    this._statusItemChange(true);
    this.state.webSocket = new WebSocket("ws://localhost:8080");
    this.state.webSocket.on("error", (e) => {
      console.log(e);
    });
    this.state.webSocket.on("open", () => {
      console.log("connected to server");
    });
    this.state.server = new JSONRPCClient((request) => {
      return axios("http://localhost:8080/", {
        method: "GET",
        headers: {
          "content-type": "application/json",
        },
        data: request,
      }).then((response) => {
        if (response.status === 200) {
          return this.state.server.receive(response.data);
        } else if (request.id !== undefined) {
          return Promise.reject(new Error(response.statusText));
        }
      });
    });
    this.state.webSocket.on("message", (data) => {
      console.log(data.toString());
    });
    this._statusItemChange(false);
  }

  _statusItemChange(loading: boolean) {
    if (loading) {
      this.myStatusBarItem.text = `$(loading~spin) loading server`;
    } else {
      this.myStatusBarItem.text = `$(megaphone) line(s) selected`;
    }

    this.myStatusBarItem.show();
  }
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  dispose(): void {}
}
