import * as vscode from "vscode";
import { log } from "console";
import { Feature } from "../feature.type";
import { TestCase, TestFile, testData } from "./testTree";
import { IServerManager } from "../core/server";

export default class TestRunner implements Feature {
  private server: IServerManager;

  constructor(context: vscode.ExtensionContext, serverConf: IServerManager) {
    log("TestRunner activated");
    this.server = serverConf;
    const ctrl = vscode.tests.createTestController("testRunner", "Test Runner");
    context.subscriptions.push(ctrl);

    const fileChangedEmitter = new vscode.EventEmitter<vscode.Uri>();
    const runHandler = (
      request: vscode.TestRunRequest,
      cancellation: vscode.CancellationToken
    ) => {
      console.log("runHandler", request, cancellation);
      return startTestRun(request, cancellation);
    };

    const startTestRun = (
      request: vscode.TestRunRequest,
      token: vscode.CancellationToken
    ) => {
      console.log("Here");
      console.log(request);
      const queue: { test: vscode.TestItem; data: TestCase }[] = [];
      const run = ctrl.createTestRun(request);

      const discoverTests = async (tests: Iterable<vscode.TestItem>) => {
        for (const test of tests) {
          if (request.exclude?.includes(test)) {
            continue;
          }

          const data = testData.get(test);
          if (data instanceof TestCase) {
            run.enqueued(test);
            queue.push({ test, data });
          } else {
            if (data instanceof TestFile && !data.didResolve) {
              await data.updateFromDisk(ctrl, test);
            }

            await discoverTests(gatherTestItems(test.children));
          }
        }
      };

      const runTestQueue = async () => {
        for (const { test, data } of queue) {
          run.appendOutput(`Running ${test.id}\r\n`);
          if (run.token.isCancellationRequested) {
            run.skipped(test);
          } else {
            run.started(test);
            await data.run(test, run, this.server.server);
          }

          // TODO: add coverage

          run.appendOutput(`Completed ${test.id}\r\n`);
        }

        run.end();
      };

      discoverTests(request.include ?? gatherTestItems(ctrl.items)).then(
        runTestQueue
      );
    };

    ctrl.refreshHandler = async () => {
      console.log("refreshHandler");
      await Promise.all(
        getWorkspaceTestPatterns().map(({ pattern }) =>
          findInitialFiles(ctrl, pattern)
        )
      );
    };

    ctrl.createRunProfile(
      "Run Tests",
      vscode.TestRunProfileKind.Run,
      runHandler,
      true,
      undefined
    );

    ctrl.resolveHandler = async (item) => {
      console.log(item);
      if (!item) {
        context.subscriptions.push(
          ...startWatchingWorkspace(ctrl, fileChangedEmitter)
        );
        return;
      }

      const data = testData.get(item);
      if (data instanceof TestFile) {
        await data.updateFromDisk(ctrl, item);
      }
    };

    function updateNodeForDocument(e: vscode.TextDocument) {
      if (e.uri.scheme !== "file") {
        return;
      }

      if (
        !e.uri.path.endsWith(".map.test.yaml") ||
        !!e.uri.path.endsWith(".map.test.json")
      ) {
        return;
      }

      log("updateNodeForDocument", e.uri.path);

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

    getWorkspaceTestPatterns().map(({ pattern }) =>
      findInitialFiles(ctrl, pattern)
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

  const path = uri.path.split("/");
  const name = path[path.length - 2];

  const file = controller.createTestItem(uri.toString(), name, uri);
  controller.items.add(file);

  const data = new TestFile(uri.path.endsWith(".json"));
  testData.set(file, data);

  file.canResolveChildren = true;
  return { file, data };
}

async function findInitialFiles(
  controller: vscode.TestController,
  pattern: vscode.GlobPattern
) {
  const files = await vscode.workspace.findFiles(pattern);
  console.log({ type: "findInitialFiles", files, pattern });
  for (const file of files) {
    if (file.scheme !== "file") {
      return;
    }
    const isTestFile =
      file.path.endsWith(".map.test.yaml") ||
      file.path.endsWith(".map.test.json");
    if (!isTestFile) {
      return;
    }

    getOrCreateFile(controller, file);
  }
}

function getWorkspaceTestPatterns() {
  if (!vscode.workspace.workspaceFolders) {
    return [];
  }

  const extensions = ["json", "yaml"]; // Add more extensions if needed
  const pattern = `**/*.map.test.{${extensions.join(",")}}`;

  return vscode.workspace.workspaceFolders.map((workspaceFolder) => ({
    workspaceFolder,
    pattern: new vscode.RelativePattern(workspaceFolder, pattern),
  }));
}

function startWatchingWorkspace(
  controller: vscode.TestController,
  fileChangedEmitter: vscode.EventEmitter<vscode.Uri>
) {
  return getWorkspaceTestPatterns().map(({ workspaceFolder, pattern }) => {
    const watcher = vscode.workspace.createFileSystemWatcher(pattern);

    watcher.onDidCreate((uri) => {
      getOrCreateFile(controller, uri);
      fileChangedEmitter.fire(uri);
    });
    watcher.onDidChange(async (uri) => {
      const { file, data } = getOrCreateFile(controller, uri);
      if (data.didResolve) {
        await data.updateFromDisk(controller, file);
      }
      fileChangedEmitter.fire(uri);
    });
    watcher.onDidDelete((uri) => controller.items.delete(uri.toString()));

    findInitialFiles(controller, pattern);

    return watcher;
  });
}

function gatherTestItems(collection: vscode.TestItemCollection) {
  const items: vscode.TestItem[] = [];
  collection.forEach((item) => items.push(item));
  return items;
}
