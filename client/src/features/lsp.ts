import * as path from "path";
import * as child_process from "child_process";
import * as net from "net";
import { workspace, window, OutputChannel } from "vscode";
import * as fs from "fs";

import {
  LanguageClient,
  LanguageClientOptions,
  RevealOutputChannelOn,
  ServerOptions,
  StreamInfo,
} from "vscode-languageclient/node";
import { Feature, ServerSetupParams } from "../feature.type";
import { LOG } from "../utils/logger";

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
    const langServerInstallDir = path.join(
      context.globalStorageUri.fsPath,
      "langServerInstall"
    );
    const outputChannel = window.createOutputChannel("Kotlin");
    const transportLayer = config.get("languageServer.transport");
    let tcpPort: number = null;
    const env: any = { ...process.env };

    if (transportLayer == "tcp") {
      tcpPort = config.get("languageServer.port");

      LOG.info(`Connecting via TCP, port: ${tcpPort}`);
    } else if (transportLayer == "stdio") {
      LOG.info("Connecting via Stdio.");
    } else {
      LOG.info(`Unknown transport layer: ${transportLayer}`);
    }

    const startScriptPath = context.extensionPath;

    const options = {
      outputChannel,
      startScriptPath,
      tcpPort,
      env,
      storagePath: workspace.workspaceFolders?.[0]?.uri?.fsPath,
    };
    this.client = this.createLanguageClient(options);
    this.client.start();
  }

  private createLanguageClient(options: {
    outputChannel: OutputChannel;
    startScriptPath: string;
    storagePath: string;
    tcpPort?: number;
    env?: any;
  }): LanguageClient {
    const clientOptions: LanguageClientOptions = {
      // Register the server for Kotlin documents
      documentSelector: [{ language: "map", scheme: "file" }],
      synchronize: {
        configurationSection: "fml",
        fileEvents: [workspace.createFileSystemWatcher("**/*.map")],
      },
      progressOnInitialization: true,
      outputChannel: options.outputChannel,
      revealOutputChannelOn: RevealOutputChannelOn.Never,
    };

    let serverOptions: ServerOptions;

    if (options.tcpPort) {
      serverOptions = () => spawnLanguageServerProcessAndConnectViaTcp(options);
    } else {
      serverOptions = {
        command: "java -jar " + options.startScriptPath,
        args: [],
        options: {
          cwd: workspace.workspaceFolders?.[0]?.uri?.fsPath,
          env: options.env,
        },
      };
      LOG.info("Creating client at {}", options.startScriptPath);
    }

    return new LanguageClient(
      "FmlSever",
      "Fhir Mapping Language Server",
      serverOptions,
      clientOptions
    );
  }
}

function spawnLanguageServerProcessAndConnectViaTcp(options: {
  outputChannel: OutputChannel;
  storagePath: string;
  startScriptPath: string;
  tcpPort?: number;
}): Promise<StreamInfo> {
  return new Promise((resolve, reject) => {
    LOG.info("Creating server.");
    const server = net.createServer((socket) => {
      // LOG.info("Closing server since client has connected.");
      // server.close();
      // resolve({ reader: socket, writer: socket });
    });
    // Wait for the first client to connect
    server.listen(options.tcpPort, () => {
      const tcpPort = (server.address() as net.AddressInfo).port.toString();
      const command = path.resolve(
        options.startScriptPath,
        "Fhir_Kotlin",
        "build",
        "libs",
        "FHIRCompiler-1.0-SNAPSHOT-all.jar"
      );
      const proc = child_process.spawn(
        "java -jar " + command,
        [],
        {
          cwd: workspace.rootPath,
        }
      );
      LOG.info(
        "Creating client at {} via TCP port {} {}",
        command,
        tcpPort,
        proc.spawnargs
      );
      const logFile =
        options.storagePath + "/vscode-languageserver-java-example.log";
      const logStream = fs.createWriteStream(logFile, { flags: "w" });
      proc.stdout.pipe(logStream);
      proc.stderr.pipe(logStream);
      console.log(`Storing log in '${logFile}'`);
    });
    server.on("error", (e) => reject(e));
  });
}
