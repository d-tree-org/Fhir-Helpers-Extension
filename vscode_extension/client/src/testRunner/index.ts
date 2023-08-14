import * as vscode from "vscode";
import { log } from "console";
import { Feature } from "../feature.type";
import { TestFile, testData } from "./testTree";
// import { TestFile } from "./testTree";

export default class TestRunner implements Feature {
  private context: vscode.ExtensionContext;

  constructor(context: vscode.ExtensionContext) {
    log("TestRunner activated");
    this.context = context;
    const ctrl = vscode.tests.createTestController("testRunner", "Test Runner");
    context.subscriptions.push(ctrl);

    const fileChangedEmitter = new vscode.EventEmitter<vscode.Uri>();
    const runHandler = (
      request: vscode.TestRunRequest,
      cancellation: vscode.CancellationToken
    ) => {
      console.log("runHandler", request, cancellation);
    };

    // const startTestRun = (request: vscode.TestRunRequest) => {};

    ctrl.refreshHandler = async () => {
      console.log("refreshHandler");
      // await Promise.all(
      //   getWorkspaceTestPatterns().map(({ pattern }) =>
      //     findInitialFiles(ctrl, pattern)
      //   )
      // );
    };

    ctrl.createRunProfile(
      "Run Tests",
      vscode.TestRunProfileKind.Run,
      runHandler,
      true,
      undefined
    );

    ctrl.resolveHandler = async (item) => {
      console.log("resolveHandler", item);

      // if (!item) {
      //   context.subscriptions.push(
      //     ...startWatchingWorkspace(ctrl, fileChangedEmitter)
      //   );
      //   return;
      // }

      // const data = testData.get(item);
      // if (data instanceof TestFile) {
      //   await data.updateFromDisk(ctrl, item);
      // }
    };

    function updateNodeForDocument(e: vscode.TextDocument) {
      console.log("updateNodeForDocument", e.uri.path);

      if (e.uri.scheme !== "file") {
        return;
      }

      if (
        !e.uri.path.endsWith(".map.test.yaml") ||
        !!e.uri.path.endsWith(".map.test.json")
      ) {
        return;
      }

      const { file, data } = getOrCreateFile(ctrl, e.uri);
      data.updateFromContents(ctrl, e.getText(), file);
    }

    for (const document of vscode.workspace.textDocuments) {
      updateNodeForDocument(document);
    }

    context.subscriptions.push(
      vscode.workspace.onDidOpenTextDocument(updateNodeForDocument),
      vscode.workspace.onDidChangeTextDocument((e) =>
        updateNodeForDocument(e.document)
      )
    );
  }

  dispose(): void {
    log("TestRunner disposed");
  }
}

function getOrCreateFile(controller: vscode.TestController, uri: vscode.Uri) {
  const existing = controller.items.get(uri.toString());
  if (existing) {
    return { file: existing, data: testData.get(existing) as TestFile };
  }

  const file = controller.createTestItem(
    uri.toString(),
    uri.path.split("/").pop()!,
    uri
  );
  controller.items.add(file);

  const data = new TestFile(uri.path.endsWith(".json"));
  testData.set(file, data);

  file.canResolveChildren = true;
  return { file, data };
}
