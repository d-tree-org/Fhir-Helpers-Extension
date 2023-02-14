import axios from "axios";
import { JSONRPCClient } from "json-rpc-2.0";
import { EventEmitter, ExtensionContext } from "vscode";
import WebSocket = require("ws");

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
    this.webSocket = new WebSocket("ws://localhost:8080");
    this.webSocket.on("error", (e) => {
      console.log(e);
      this.state = "connected";
      this.connectionEvent.fire(this.state);
    });
    this.webSocket.on("open", () => {
      this.state = "connected";
      console.log("connected to server");
      this.connectionEvent.fire(this.state);
    });
    this.server = new JSONRPCClient((request) => {
      return axios("http://localhost:8080/", {
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
  }
}
