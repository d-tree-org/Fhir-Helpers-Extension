import * as vscode from "vscode";
import * as yaml from "yaml";
import { YamlData, TestCaseData } from "./types";

export const parseYaml = (yamlContent: string) => {
  const parsedYaml: YamlData = yaml.parse(yamlContent);

  const extractedTestCases: TestCaseData[] = [];

  // Process each test step
  parsedYaml.tests.forEach((testStep, stepIndex) => {
    testStep.verify.forEach((verifyItem) => {
      if (verifyItem.type === "notNull" && verifyItem.path) {
        // Extract range start and end for the "notNull" test
        const rangeStartLine = yamlContent
          .split("\n")
          .findIndex((line) => line.includes("- type: notNull"));
        const rangeEndLine = yamlContent
          .split("\n")
          .findIndex(
            (line, index) => index > rangeStartLine && !line.startsWith(" ")
          );

        if (rangeStartLine !== -1 && rangeEndLine !== -1) {
          const range = new vscode.Range(
            new vscode.Position(rangeStartLine, 0),
            new vscode.Position(
              rangeEndLine,
              yamlContent.split("\n")[rangeEndLine].length
            )
          );

          extractedTestCases.push({
            range,
            response: testStep.response,
            path: verifyItem.path,
            value: verifyItem.value || "",
          });
        }
      }
    });
  });

  return extractedTestCases;
};
