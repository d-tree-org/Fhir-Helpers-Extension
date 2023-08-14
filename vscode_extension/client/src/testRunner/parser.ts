import * as vscode from "vscode";
import { parseYaml } from "./parsers/yaml";
import { TestCaseData } from "./parsers/types";

export const parseStructureMapFile = (
  text: string,
  isJson: boolean,
  events: {
    onTest(range: vscode.Range, data: TestCaseData): void;
  }
) => {
  if (!isJson) {
    parseYaml(text).map((testCase) => {
      const range = new vscode.Range(
        new vscode.Position(
          testCase.range.start.line,
          testCase.range.start.character
        ),
        new vscode.Position(
          testCase.range.end.line,
          testCase.range.end.character
        )
      );
      events.onTest(range, testCase);
    });
  }
};
