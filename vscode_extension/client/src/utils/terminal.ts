import * as execa from "execa";

export const execShell = async (cmd: string, controller: AbortController) => {
  return execa.commandSync(cmd);
};
