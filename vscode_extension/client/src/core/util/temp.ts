import fs from "node:fs";
import fsAw from "fs/promises";
import path from "node:path";
import os from "node:os";
import { createHash } from "node:crypto";
import { Tab, TabInputText, window } from "vscode";

const tempDir = fs.realpathSync(os.tmpdir());

const getPath = (prefix = "") =>
  path.join(tempDir, prefix + "fhir-extension-vscode");

export function temporaryDirectory({ prefix = "" } = {}) {
  const directory = getPath(prefix);
  try {
    fs.mkdirSync(directory);
    return directory;
  } catch (error) {
    return directory;
  }
}

function temporaryFile(name: string) {
  return path.join(temporaryDirectory(), name);
}

export async function temporaryWrite(
  fileContent: string,
  name: string,
  opts?: { dir: string }
) {
  let filename: fs.PathLike | fs.promises.FileHandle;
  if (opts) {
    filename = path.join(opts.dir, name);
  } else {
    filename = temporaryFile(name);
  }
  await fsAw.writeFile(filename, fileContent);
  return filename;
}

export function getFileName(file: string) {
  const has = createHash("MD5", {});
  has.update(file);
  return `${path.basename(path.dirname(file))}-${path.basename(file)}.json`;
}

export async function closeFileIfOpen(dir: string): Promise<void> {
  const tabs: Tab[] = window.tabGroups.all.map((tg) => tg.tabs).flat();
  const index = tabs.findIndex(
    (tab) =>
      tab.input instanceof TabInputText && tab.input.uri.fsPath.includes(dir)
  );
  if (index !== -1) {
    await window.tabGroups.close(tabs[index]);
  }
}
