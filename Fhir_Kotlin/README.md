# FHIR Helpers VsCode Extension

This is a collection of useful features for working with some of the resources in the FHIR spec for example the Questionnaires and Structure Maps.

## Flags and optiones

- Compile structure map
  - This expects a structure map and compiles it into the Json equivalent
  ```bash
  java -jar exec.jar compile [path].map
  ```
- Transform questionnare response
  - This expects a structure map and QuestionnaireResponse, then transforms it using the structure maps
  ```bash
  java -jar exec.jar transform [path].map
  ```
- Transform questionnare responses (batch)

  - This command expects a Json file that has data used to transform

  ```bash
  java -jar exec.jar transform_batch [path].json
  ```

- Test structure map
  - This command expects a Json file
  ```bash
  java -jar exec.jar test [path].json
  ```

## Structure map Transform

To transform QuestionnareResponses with a structure map you need to create a config a json config file that has the following properties
| Element | Description |
|---|---|
| `type` | The type of functionality |
| `map`| The Structure map config |
| `response` | Array of questionare responses paths|

Structure map config
| Element | Description |
|---|---|
| `path` | The location to the file|
| `name?` | The Src name|

Example file

```json
{
  "type": "transform",
  "map": {
    "path": "./art_client_viral_load_test_results.map",
    "name": "VL_test"
  },
  "response": [
    "./response/invalid.json",
    "./response/missing.json",
    "./response/not-available.json"
  ]
}
```

# Structure map test

To test a structure map you need to create a config a json config file that has the following properties

| Property | Description                                                                                           |
| -------- | ----------------------------------------------------------------------------------------------------- |
| `type`   | The type of transformation you are currently doing str_test for tests                                 |
| `map`    | Information about the actual FML file you want to test. All you need is the path and an optional name |
| `tests`  | An array of the questionnare reponses you want to tests with this structure map                       |

Structure map config
| Element | Description |
|---|---|
| `path` | The location to the file|
| `name?` | The Src name|

The actual test array needs the following structure

| Property   | Description                                      |
| ---------- | ------------------------------------------------ |
| `response` | The path to the questinare response              |
| `verify`   | An array of the actual tests and required values |

The test section of the file needs the following

| Property      | Description                                      |
| ------------- | ------------------------------------------------ |
| `type`        | The type of assertion test you are trying to run |
| `path`        | The JSONPath to run to get the actual data       |
| `value?`      | The value to compare with                        |
| `valueRange?` | A range of values to compare with                |

Value Range

| Property | Description                                    |
| -------- | ---------------------------------------------- |
| `start`  | Starting point of the range `date` or `number` |
| `end`    | Ending point of the range `date` or `number`   |

Example file

```json
{
  "type": "str_test",
  "map": {
    "path": "./exposed_infant_hiv_test_and_results.map",
    "name": "tracing"
  },
  "tests": [
    {
      "response": "./response/positive.json",
      "verify": [
        {
          "type": "eq",
          "path": "$.resourceType",
          "value": "Bundle"
        },
        {
          "type": "lte",
          "path": "$.entry[?(@.resource.resourceType == \"Task\")].resource.code.coding[0].code",
          "value": "225368008"
        },
        {
          "type": "eq",
          "path": "$.resourceType",
          "value": "jeff"
        }
      ]
    }
  ]
}
```
### Available Tests

The following operators are available:

| Operator        | Description                              |
| --------------- | ---------------------------------------- |
| `$eq`           | Equal                                    |
| `$eqi`          | Equal (case-insensitive)                 |
| `$ne`           | Not equal                                |
| `$lt`           | Less than                                |
| `$lte`          | Less than or equal to                    |
| `$gt`           | Greater than                             |
| `$gte`          | Greater than or equal to                 |
| `$in`           | Included in an array                     |
| `$notIn`        | Not included in an array                 |
| `$contains`     | Contains                                 |
| `$notContains`  | Does not contain                         |
| `$containsi`    | Contains (case-insensitive)              |
| `$notContainsi` | Does not contain (case-insensitive)      |
| `$null`         | Is null                                  |
| `$notNull`      | Is not null                              |
| `$between`      | Is between                               |
| `$startsWith`   | Starts with                              |
| `$startsWithi`  | Starts with (case-insensitive)           |
| `$endsWith`     | Ends with                                |
| `$endsWithi`    | Ends with (case-insensitive)             |
| `$or`           | Joins the filters in an "or" expression  |
| `$and`          | Joins the filters in an "and" expression |
| `$not`          | Joins the filters in an "not" expression | 