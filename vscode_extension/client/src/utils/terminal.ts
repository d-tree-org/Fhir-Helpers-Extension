import util from "util";
import { exec as execNonPromise } from "child_process";

const exec = util.promisify(execNonPromise);

export const execShell = async (cmd: string) => {
  return await exec(cmd);
};
