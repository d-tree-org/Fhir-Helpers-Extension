
export interface YamlData {
  tests: TestItem[];
}

type Position = {
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
  value: string;
};

export interface TestItem {
  response: string;
  verify: VerifySection[];
}

export interface VerifySection {
  type: string;
  path: string;
  value?: string;
}
