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
import { Feature } from "../feature.type";
import { isMapFile } from "../utils";
import { IServerManager } from "../core/server";
import { JSONRPCClient } from "json-rpc-2.0";
import { RPCResponse } from "../core/rpc/run";

export class Formatter implements Feature {
  private conf: IServerManager;

  constructor(subscriptions: Disposable[], serverConf: IServerManager) {
    this.conf = serverConf;
    subscriptions.push(
      languages.registerDocumentFormattingEditProvider(
        {
          language: "map",
          scheme: "file",
        },
        {
          provideDocumentFormattingEdits: async (
            document: TextDocument
          ): Promise<TextEdit[]> => {
            try {
              const response = await beautifyMap(
                document.uri.fsPath,
                this.conf.server
              );
              if (response.error) {
                window.showErrorMessage(response.error);
                return [];
              }
              if (response.result) {
                return [TextEdit.replace(getRange(document), response.result)];
              }

              return [TextEdit.replace(getRange(document), document.getText())];
            } catch (error) {
              console.error(error);

              return [];
            }
          },
        }
      ),
      commands.registerCommand("map.beautify", () => {
        return this.beautify();
      })
    );
  }

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  dispose(): void {}

  async beautify(): Promise<void> {
    const activeTextEditor: TextEditor | undefined = window.activeTextEditor;

    if (activeTextEditor && isMapFile(activeTextEditor.document)) {
      activeTextEditor.edit(async (editBuilder: TextEditorEdit) => {
        const response = await beautifyMap(
          activeTextEditor.document.uri.fsPath,
          this.conf.server
        );

        if (response.error) {
          window.showErrorMessage(response.error);
          return;
        }

        if (response.result) {
          editBuilder.replace(
            getRange(activeTextEditor.document),
            response.result
          );
        }
      });
    } else {
      window.showWarningMessage("This is not a MAP document!");
      return;
    }
  }
}

function getRange(document: TextDocument): Range {
  return new Range(
    new Position(0, 0),
    new Position(
      document.lineCount - 1,
      document.lineAt(document.lineCount - 1).text.length
    )
  );
}

async function beautifyMap(
  file: string,
  server: JSONRPCClient
): Promise<RPCResponse> {
  try {
    const res = await server.request("formatStructureMap", {
      path: file,
    });

    return { result: res as string };
  } catch (error) {
    return {
      error: error.toString(),
    };
  }
}
