import { TextDocument } from "vscode";
import { JSONRPCClient } from "json-rpc-2.0";

export async function runConfMap(doc: TextDocument, server: JSONRPCClient) {
  try {
    const file = doc.uri.fsPath;
    const data = await server.request("parseTransformFromJson", {
      path: file,
    });
    return data;
  } catch (error) {
    throw Error(error);
  }
}
