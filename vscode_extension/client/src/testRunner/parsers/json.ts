import { TestCaseData, YamlData } from "./types";
import { parseTree } from "jsonc-parser";
export const parseJson = (jsonContent: string): TestCaseData[] => {
  const jsonData: YamlData = JSON.parse(jsonContent);

  const tree = parseTree(jsonContent);
  console.log(tree);

  // Initialize an array to store extracted test cases
  const extractedTestCases: TestCaseData[] = [];

  // Process each test step
  jsonData.tests.forEach((testStep, stepIndex) => {
    testStep.verify.forEach((verifyItem) => {
      if (verifyItem.type && verifyItem.path) {
        const range = {
          start: { line: 0, character: 0 },
          end: {
            line: 0,
            character: 0,
          },
        };
        extractedTestCases.push({
          id: `${stepIndex},${verifyItem.path}`,
          range: range,
          title: verifyItem.title,
          parentTitle: testStep.title,
          response: testStep.response,
          path: verifyItem.path,
          value: verifyItem.value || "",
          valueRange: verifyItem.valueRange,
          type: verifyItem.type,
          defaultTests: jsonData.map.defaultTests,
        });
      }
    });
  });

  return extractedTestCases;
};
