import * as path from "path";

import {
  LanguageClient,
  LanguageClientOptions,
  ServerOptions,
  TransportKind,
} from "vscode-languageclient/node";
import { Feature, ServerSetupParams } from "../feature.type";

export class LanguageServer implements Feature {
  params: ServerSetupParams;
  client: LanguageClient;

  constructor(params: ServerSetupParams) {
    this.lsp(params);
  }

  dispose(): void {
    this.client?.stop();
  }

  async lsp({ context, config }: ServerSetupParams) {
    // The server is implemented in node
    const serverModule = context.asAbsolutePath(
      path.join("server", "out", "server.js")
    );
    // The debug options for the server
    // --inspect=6009: runs the server in Node's Inspector mode so VS Code can attach to the server for debugging
    const debugOptions = { execArgv: ["--nolazy", "--inspect=6009"] };

    // If the extension is launched in debug mode then the debug server options are used
    // Otherwise the run options are used
    const serverOptions: ServerOptions = {
      run: { module: serverModule, transport: TransportKind.ipc },
      debug: {
        module: serverModule,
        transport: TransportKind.ipc,
        options: debugOptions,
      },
    };

    // Options to control the language client
    const clientOptions: LanguageClientOptions = {
      // Register the server for plain text documents
      documentSelector: [{ scheme: "file", language: "map" }],
    };

    // Create the language client and start the client.
    this.client = new LanguageClient(
      "FmlSever",
      "Fhir Mapping Language Server",
      serverOptions,
      clientOptions
    );

    // Start the client. This will also launch the server
    this.client.start();
  }
}
