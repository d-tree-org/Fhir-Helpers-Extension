import { TextDecoder } from "util";
import * as vscode from "vscode";
import { parseStructureMapFile } from "./parser";
import { TestCaseData, TestResult, TestStatus } from "./parsers/types";
import { JSONRPCClient } from "json-rpc-2.0";
import { sendRunTest } from "../core/rpc/run";
import { Diagnostic } from "vscode";

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

    parseStructureMapFile(content, this.isJson, {
      onTest: (range, td) => {
        const parent = ancestors[ancestors.length - 1];
        const data = new TestCase(td, thisGeneration);

        const tcase = controller.createTestItem(
          td.id,
          data.getLabel(),
          item.uri
        );
        // console.log(data.getLabel(), tcase);
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
    return `${this.data.parentTitle ? this.data.parentTitle + " - " : ""}${
      this.data.title ?? this.data.path
    }`;
  }

  async run(
    item: vscode.TestItem,
    options: vscode.TestRun,
    server: JSONRPCClient
  ): Promise<void> {
    console.log({
      i: "Runn this item",
      item,
      options,
      data: this.data,
      generation: this.generation,
    });
    const start = Date.now();

    const res = await sendRunTest(item, this.data, server);
    const duration = Date.now() - start;

    if (res.error) {
      options.errored(item, new vscode.TestMessage(res.error));
      return;
    }
    const result = res.result as TestResult;
    console.log(JSON.stringify(result));

    this.updateDiagnostics(item, result);
    if (result.status.passed) {
      options.passed(item, duration);
      return;
    } else {
      options.failed(
        item,
        new vscode.TestMessage(
          (result.status.exception as any)?.message ?? result.status.exception
        )
      );
      return;
    }
  }

  updateDiagnostics(item: vscode.TestItem, result: TestResult): void {
    const collection = vscode.languages.createDiagnosticCollection("test");
    if (
      result.defaultTestsResults !== undefined &&
      result.defaultTestsResults.length > 0
    ) {
      collection.clear();
      const results = result.defaultTestsResults?.[0].testResults ?? [];
      collection.set(
        item.uri,
        createDiag(
          item,
          results.filter((e) => !e.passed)
        )
      );
      collection.set(
        item.uri,
        createDiag(
          item,
          results.filter((e) => e.passed)
        )
      );
    }
  }
}

const createDiag = (
  item: vscode.TestItem,
  results: TestStatus[]
): vscode.Diagnostic[] => {
  return results.map((result) => ({
    code: result.path,
    message: result.exception.message,
    range: item.range,
    severity: result.passed
      ? vscode.DiagnosticSeverity.Warning
      : vscode.DiagnosticSeverity.Error,
    source: "",
    relatedInformation: [
      new vscode.DiagnosticRelatedInformation(
        new vscode.Location(item.uri, item.range),
        result.value
      ),
    ],
  }));
};
