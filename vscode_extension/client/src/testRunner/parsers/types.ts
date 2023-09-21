export interface YamlData {
  tests: TestItem[];
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
  passed: boolean;
  value?: any;
  expected?: any;
  exception?: string;
  path?: string;
}
