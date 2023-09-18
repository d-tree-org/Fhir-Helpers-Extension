import * as yaml from "yaml";
import { YamlData, TestCaseData } from "./types";

export const parseYaml = (yamlContent: string): TestCaseData[] => {
  const parsedYaml: YamlData = yaml.parse(yamlContent);

  const extractedTestCases: TestCaseData[] = [];
  console.log(parsedYaml);

  // Process each test step
  parsedYaml.tests.forEach((testStep, stepIndex) => {
    testStep.verify.forEach((verifyItem) => {
      if (verifyItem.type && verifyItem.path) {
        let allLines = yamlContent.split("\n");
        const rangeStartLine = yamlContent
          .split("\n")
          .findIndex((line) => line.includes("- type:"));
        const rangeEndLine = yamlContent
          .split("\n")
          .findIndex(
            (line, index) => index > rangeStartLine && !line.startsWith(" ")
          );

        if (rangeStartLine !== -1 && rangeEndLine !== -1) {
          const range = {
            start: { line: rangeStartLine, character: 0 },
            end: {
              line: rangeEndLine,
              character: yamlContent.split("\n")[rangeEndLine].length,
            },
          };

          extractedTestCases.push({
            range,
            response: testStep.response,
            path: verifyItem.path,
            value: verifyItem.value,
            valueRange: verifyItem.valueRange,
            type: verifyItem.type,
          });
        }
      }
    });
  });

  return extractedTestCases;
};

