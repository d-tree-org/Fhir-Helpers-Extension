import * as yaml from "yaml";
import { YamlData, TestCaseData } from "./types";

export const parseYaml = (yamlContent: string): TestCaseData[] => {
  const parsedYaml: YamlData = yaml.parse(yamlContent, {
    prettyErrors: true,
    keepSourceTokens: true,
  });
  const lc = new yaml.LineCounter();
  const doc = yaml.parseDocument(yamlContent, {
    prettyErrors: true,
    keepSourceTokens: true,
    lineCounter: lc,
  });

  const extractedTestCases: TestCaseData[] = [];
  const testlists = (doc.get("tests") as any).items;
  const posMap = new Map<
    string,
    { startLine: number; startChar: number; endLine: number; endChar: number }
  >();
  for (let resIndex = 0; resIndex < testlists.length; resIndex++) {
    const response = testlists[resIndex];
    const list = response.items.find((item: any) => item.key.value === "verify")
      .value.items;
    for (let i = 0; i < list.length; i++) {
      const item = list[i];
      const startChar = item.range[0];
      const endChar = item.range[2];
      const startLine = lc.linePos(startChar);
      const endLine = lc.linePos(endChar);
      posMap.set(`${resIndex},${i}`, {
        startLine: startLine.line,
        startChar,
        endLine: endLine.line,
        endChar,
      });
      console.log({ item, lineNum: startLine });
    }
  }

  // Process each test step
  parsedYaml.tests.forEach((testStep, stepIndex) => {
    testStep.verify.forEach((verifyItem, verfyIndex) => {
      if (verifyItem.type && verifyItem.path) {
        const id = `${stepIndex},${verfyIndex}`;
        const pos = posMap.get(id);

        const range = {
          start: { line: pos.startLine - 1, character: pos.startChar },
          end: {
            line: pos.endLine - 1,
            character: pos.endChar,
          },
        };

        extractedTestCases.push({
          id: id,
          range,
          title: verifyItem.title,
          parentTitle: testStep.title,
          response: testStep.response,
          path: verifyItem.path,
          value: verifyItem.value,
          valueRange: verifyItem.valueRange,
          type: verifyItem.type,
          defaultTests: parsedYaml.map.defaultTests,
        });
      }
    });
  });

  return extractedTestCases;
};
