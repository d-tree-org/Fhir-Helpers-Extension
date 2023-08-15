import { TextDecoder } from "util";
import * as vscode from "vscode";
import { parseStructureMapFile } from "./parser";
import { TestCaseData } from "./parsers/types";

const textDecoder = new TextDecoder("utf-8");

export type StructureMapTestData = TestFile | TestCase;

export const testData = new WeakMap<vscode.TestItem, StructureMapTestData>();
let generationCounter = 0;

export const getContentFromFilesystem = async (uri: vscode.Uri) => {
  try {
    const rawContent = await vscode.workspace.fs.readFile(uri);
    return textDecoder.decode(rawContent);
  } catch (e) {
    console.warn(`Error providing tests for ${uri.fsPath}`, e);
    return "";
  }
};

export class TestFile {
  public didResolve = false;
  private isJson = false;

  constructor(isJson: boolean) {
    this.isJson = isJson;
  }

  public async updateFromDisk(
    controller: vscode.TestController,
    item: vscode.TestItem
  ) {
    try {
      const content = await getContentFromFilesystem(item.uri!);
      item.error = undefined;
      this.updateFromContents(controller, content, item);
    } catch (e) {
      item.error = (e as Error).stack;
    }
  }

  /**
   * Parses the tests from the input text, and updates the tests contained
   * by this file to be those from the text,
   */
  public updateFromContents(
    controller: vscode.TestController,
    content: string,
    item: vscode.TestItem
  ) {
    const ancestors = [{ item, children: [] as vscode.TestItem[] }];
    const thisGeneration = generationCounter++;
    this.didResolve = true;

    const ascend = (depth: number) => {
      while (ancestors.length > depth) {
        const finished = ancestors.pop()!;
        finished.item.children.replace(finished.children);
      }
    };

    parseStructureMapFile(content, false, {
      onTest: (range, td) => {
        const parent = ancestors[ancestors.length - 1];
        const data = new TestCase(td, thisGeneration);
        const id = `${item.uri}/${data.getLabel()}`;

        const tcase = controller.createTestItem(id, data.getLabel(), item.uri);
        console.log(data.getLabel(), tcase);
        testData.set(tcase, data);
        tcase.range = range;
        parent.children.push(tcase);
      },
    });

    ascend(0); // finish and assign children for all remaining items
  }
}

export class TestCase {
  constructor(private readonly data: TestCaseData, public generation: number) {}

  getLabel() {
    return `${this.data.path} ${this.data.value}}`;
  }

  async run(item: vscode.TestItem, options: vscode.TestRun): Promise<void> {
    console.log(item);
  }
}
