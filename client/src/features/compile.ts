import { execShell } from "../utils/terminal";
import * as path from "path";
import { ExtensionContext } from "vscode";

function getJsonOutput(data: string) {
  const splitData = data.split("MAP_OUTPUT_STARTS_HERE");
  return splitData[1].trim();
}

export async function compileMap(
  data: string,
  filePath: string,
  context: ExtensionContext
): Promise<string> {
  try {
    const compilerPath = context.asAbsolutePath(
      path.join("bin", "compile", "compile.jar")
    );

    const process = await execShell(`java -jar ${compilerPath} ${filePath}`);
    console.log(process);

    return getJsonOutput(process);
  } catch (error) {
    throw Error(error);
  }
}
