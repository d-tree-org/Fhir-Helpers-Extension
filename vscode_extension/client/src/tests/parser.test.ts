// import { describe, it, assert } from "vitest";
// import { parseYaml } from "../testRunner/parsers/yaml";

// const content = `
// type: str_test
// map:
//   path: "./patient_demographic_registration_emr_dataclerk.map"
//   name: demo
// tests:
//   - response: "./res/exposed_infant_response.json"
//     verify:
//       - type: notNull
//         path: $.entry[?(@.resource.resourceType == "Patient")]
//       - type: eq
//         path: $.entry[?(@.resource.resourceType == "Patient")].resource.identifier[0].value
//         value: fake-id
//   - response: "./res/already_on_art.json"
//     verify:
//       - type: notNull
//         path: $.entry[?(@.resource.resourceType == "Patient")]
//       - type: eq
//         path: $.entry[?(@.resource.resourceType == "Patient")].resource.identifier[0].value
//         value: "1"

// `;

// describe("parser", () => {
//   it("should parse a simple expression", () => {
//     const response = parseYaml(content);
//     assert.equal(response.length, 4);
//   });
// });
