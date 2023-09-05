export interface YamlData {
  tests: TestItem[];
}

export type Position = {
  line: number;
  character: number;
};

export type TestCaseData = {
  range: {
    start: Position;
    end: Position;
  };
  response: string;
  path: string;
  value?: string;
  valueRange?: ValueRange;
  type: string;
};

interface ValueRange {
  start: string;
  end: string;
}

export interface TestItem {
  response: string;
  verify: VerifySection[];
}

export interface VerifySection {
  type: string;
  path: string;
  value?: string;
  valueRange?: ValueRange;
}

export interface TestResult {
  passed: boolean;
  value?: any;
  expected?: any;
  exception?: string;
  path?: string;
}
