export interface YamlData {
  map: TestDataConfig;
  tests: TestItem[];
}

export interface TestDataConfig {
  path: string;
  name: string;
  defaultTests: DefaultTests[];
}
export interface DefaultTests {
  type: string;
}

export type Position = {
  line: number;
  character: number;
};

export type TestCaseData = {
  id: string;
  range: {
    start: Position;
    end: Position;
  };
  response: string;
  path: string;
  value?: string;
  valueRange?: ValueRange;
  type: string;
  title?: string;
  parentTitle?: string;
  defaultTests: DefaultTests[];
};

interface ValueRange {
  start: string;
  end: string;
}

export interface TestItem {
  response: string;
  title?: string;
  verify: VerifySection[];
}

export interface VerifySection {
  type: string;
  path: string;
  value?: string;
  valueRange?: ValueRange;
  title?: string;
}

export interface TestResult {
  status: TestStatus;
  defaultTestsResults: DefaultTestResult[];
}

export interface DefaultTestResult {
  passed: boolean;
  testResults: TestStatus[];
}

export interface TestStatus {
  passed: boolean;
  value?: any;
  expected?: any;
  exception?: {
    cause?: any;
    stackTrace?: any;
    message?: string;
    localizedMessage?: any;
  };
  path?: string;
}
