import fs from "node:fs";
import fsAw from "fs/promises";
import path from "node:path";
import cryptoRandomString from "crypto-random-string";
import os from "node:os";

const tempDir = fs.realpathSync(os.tmpdir());

const getPath = (prefix = "") =>
  path.join(tempDir, prefix + cryptoRandomString({ length: 32 }));

export function temporaryDirectory({ prefix = "" } = {}) {
  const directory = getPath(prefix);
  fs.mkdirSync(directory);
  return directory;
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
