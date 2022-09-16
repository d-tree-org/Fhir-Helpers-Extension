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

export class Formatter implements Feature {
  constructor(subscriptions: Disposable[]) {
    subscriptions.push(
      languages.registerDocumentFormattingEditProvider(
        {
          language: "map",
          scheme: "file",
        },
        {
          provideDocumentFormattingEdits(document: TextDocument): TextEdit[] {
            const formattedDocument: string | undefined = beautifyMap(
              document.getText()
            );
            if (formattedDocument) {
              return [TextEdit.replace(getRange(document), formattedDocument)];
            }

            return [TextEdit.replace(getRange(document), document.getText())];
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

  beautify(): any {
    const activeTextEditor: TextEditor | undefined = window.activeTextEditor;

    if (activeTextEditor && isMapFile(activeTextEditor.document)) {
      activeTextEditor.edit((editBuilder: TextEditorEdit) => {
        const formattedDocument: string | undefined = beautifyMap(
          activeTextEditor.document.getText()
        );

        if (formattedDocument) {
          editBuilder.replace(
            getRange(activeTextEditor.document),
            formattedDocument
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

function beautifyMap(data: string) {
  return "jeff";
}
