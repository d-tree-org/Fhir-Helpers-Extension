# FHIR Helpers

This is a collection of useful features for working with some of the resources in the FHIR spec for example the Questionnaires and Structure Maps.

## Modules
- CLI
- Vs Code Extension

## CLI
The documentation for the CLI can be found [here](Fhir_Kotlin\README.md)
## Vs Code
The documentantion for the extension can be found here [here](vscode_extension\README.md)
## Structure map Transform
To run a structure map test you need to create a config file that
| Element  | Description  |
|---|---|
| `type`  | The type of functionality  |
| `map`|   The Structure map config |
| `response` | Array of questionare responses paths|

Structure map config
| Element  | Description  |
|---|---|
| `path` | The location to the file|
| `name?` | The Src name|

Example config

```json

{
  "type": "str_test",
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