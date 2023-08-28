import { JSONRPCClient } from "json-rpc-2.0";

function getJsonOutput(data: string) {
  const splitData = data.split("MAP_OUTPUT_STARTS_HERE");
  return splitData[1].trim();
}

export async function compileMap(filePath: string, server: JSONRPCClient) {
  try {
    const res = await server.request("compileStructureMap", {
      path: filePath,
    });
    console.log(res);
    return res;
  } catch (error) {
    throw Error(error);
  }
}
