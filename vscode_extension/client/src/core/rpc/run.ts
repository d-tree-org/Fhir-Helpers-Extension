import { TestItem, TextDocument } from "vscode";
import { JSONRPCClient } from "json-rpc-2.0";
import { TestCaseData } from "../../testRunner/parsers/types";
import { getFileWorkSpacePath } from "../../utils/workspace";

export async function runConfMap(doc: TextDocument, server: JSONRPCClient) {
  try {
    const file = doc.uri.fsPath;
    const res = await server.request("parseTransformFromJson", {
      path: file,
      projectRoot: getFileWorkSpacePath(file),
    });
    return res;
  } catch (error) {
    throw Error(error);
  }
}

export async function sendRunTest(
  item: TestItem,
  data: TestCaseData,
  server: JSONRPCClient
): Promise<RPCResponse> {
  try {
    const file = item.uri.fsPath;
    const newData = { ...data };
    delete newData.id;
    const res = await server.request("runTest", {
      path: file,
      data: newData,
      projectRoot: getFileWorkSpacePath(file),
    });
    console.log(file);
    return {
      result: res,
    };
  } catch (error) {
    console.log(error);

    return {
      error: error.toString(),
    };
  }
}

export interface RPCResponse {
  error?: string;
  result?: any;
}
