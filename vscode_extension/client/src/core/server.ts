import axios from "axios";
import { JSONRPCClient } from "json-rpc-2.0";
import {
  EventEmitter,
  ExtensionContext,
  ProgressLocation,
  window,
} from "vscode";
import WebSocket = require("ws");
import * as path from "path";
import { execShell } from "../utils/terminal";
import portfinder from "portfinder";

type ServerState = "loading" | "connected" | "failed";

export interface IServerManager {
  state: ServerState;
  webSocket?: WebSocket;
  server?: JSONRPCClient;
  context: ExtensionContext;

  initServer: () => Promise<void>;

  onConnection: (listener: (state: ServerState) => void) => void;
}

export class ServerManager implements IServerManager {
  state: ServerState;
  webSocket?: WebSocket;
  server?: JSONRPCClient<void>;
  context: ExtensionContext;
  private connectionEvent = new EventEmitter<ServerState>();

  constructor(context: ExtensionContext) {
    this.state = "loading";
    this.context = context;
  }
  onConnection(listener: (s: ServerState) => void) {
    this.connectionEvent.event(listener);
  }

  async initServer() {
    window.withProgress(
      {
        location: ProgressLocation.Notification,
        title: "Connecting to server",
        cancellable: false,
      },
      async (progress, token) => {
        progress.report({ increment: 20 });
        await this.startServer();
        return null;
      }
    );
  }

  private async startServer() {
    try {
      const port = 9090;
      // const port = await portfinder.getPortPromise({
      //   startPort: 9080,
      //   stopPort: 65535,
      // });
      // this.controller = await runServer(this.context, port);
      this.webSocket = new WebSocket("ws://localhost:" + port);
      this.webSocket.on("error", (e) => {
        console.log(e);
        this.state = "failed";
        this.connectionEvent.fire(this.state);
      });
      this.webSocket.on("open", () => {
        this.state = "connected";
        console.log("connected to server");
        this.connectionEvent.fire(this.state);
      });
      this.server = new JSONRPCClient((request) => {
        return axios(`http://localhost:${port}/`, {
          method: "GET",
          headers: {
            "content-type": "application/json",
          },
          data: request,
        }).then((response) => {
          if (response.status === 200) {
            return this.server.receive(response.data);
          } else if (request.id !== undefined) {
            return Promise.reject(new Error(response.statusText));
          }
        });
      });
      this.webSocket.on("message", (data) => {
        console.log(data.toString());
      });
    } catch (error) {
      console.log(error);
    }
  }

  dispose() {
    this.webSocket?.close();
    // this.controller?.abort();
  }
}

async function runServer(context: ExtensionContext, port: number) {
  const compilerPath = context.asAbsolutePath(
    path.join("bin", "server", "server.jar")
  );
  const controller = new AbortController();
  const data = await execShell(
    `java -jar ${compilerPath} --port=${port}`,
    controller
  );
  console.log(data.stdout);
  return controller;
}
