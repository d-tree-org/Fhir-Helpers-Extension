import fs from "node:fs";
import fsAw from "fs/promises";
import path from "node:path";
import cryptoRandomString from "crypto-random-string";
import os from "node:os";
import { promisify } from "node:util";
import stream from "node:stream";

const pipeline = promisify(stream.pipeline);

const tempDir = fs.realpathSync(os.tmpdir());

const getPath = (prefix = "") =>
  path.join(tempDir, prefix + cryptoRandomString({ length: 32 }));

function temporaryDirectory({ prefix = "" } = {}) {
  const directory = getPath(prefix);
  fs.mkdirSync(directory);
  return directory;
}

function temporaryFile(name: string) {
  return path.join(temporaryDirectory(), name);
}

export async function temporaryWrite(fileContent: string, name: string) {
  const filename = temporaryFile(name);
  await fsAw.writeFile(filename, fileContent);
  return filename;
}
