import { TestCaseData, YamlData } from "./types";

export const parseJson = (jsonContent: string): TestCaseData[] => {
  const jsonData: YamlData = JSON.parse(jsonContent);

  // Initialize an array to store extracted test cases
  const extractedTestCases: TestCaseData[] = [];

  // Process each test step
  jsonData.tests.forEach((testStep, stepIndex) => {
    testStep.verify.forEach((verifyItem) => {
      if (verifyItem.type && verifyItem.path) {
        const rangeStart = jsonContent
          .split("\n")
          .findIndex((line) => line.includes(`"type":`));
        const rangeEnd = jsonContent.indexOf(
          `"path": "${verifyItem.path}"`,
          rangeStart
        );

        if (rangeStart !== -1 && rangeEnd !== -1) {
          const range = {
            start: { line: rangeStart, character: 0 },
            end: {
              line: rangeEnd,
              character: jsonContent.split("\n")[rangeEnd].length,
            },
          };
          extractedTestCases.push({
            range: range,
            response: testStep.response,
            path: verifyItem.path,
            value: verifyItem.value || "",
            valueRange: verifyItem.valueRange,
            type: verifyItem.type,
          });
        }
      }
    });
  });

  return extractedTestCases;
};
