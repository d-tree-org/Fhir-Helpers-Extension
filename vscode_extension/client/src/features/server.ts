import { commands, StatusBarAlignment, StatusBarItem, window } from "vscode";
import { Feature } from "../feature.type";
import { IServerManager } from "../core/server";

export class Server implements Feature {
  myStatusBarItem: StatusBarItem;
  state: IServerManager;

  constructor(serverConf: IServerManager) {
    this.state = serverConf;
    const { context } = serverConf;
    const myCommandId = "fhir-map.showSelectionCount";
    console.log(myCommandId);

    context.subscriptions.push(
      commands.registerCommand(myCommandId, () => {
        if (this.state.state !== "loading") {
          window.showInformationMessage(
            `Yeah, line(s) selected... Keep going!`
          );
          this.state.server
            .request("compileStructureMap", {
              path: "D:\\Work\\dev\\fhir-resources\\mwcore\\structure_map\\tracing\\phone_tracing\\phone_tracing_outcomes.map",
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
    context.subscriptions.push(this.myStatusBarItem);

    this._statusItemChange(false);
    this.initServer();
  }

  async initServer() {
    this.state.onConnection((state) => {
      this._statusItemChange(state === "loading");
    });
    await this.state.initServer();
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
