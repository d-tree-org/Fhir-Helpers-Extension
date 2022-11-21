# FHIR Helpers VsCode Extension

This is a collection of useful features for working with some of the resources in the FHIR spec for example the Questionnaires and Structure Maps.

## Flags and optiones

- Compile structure map
    ```bash 
    java exec.jar compile [path].map
    ```
- transform single
    ```dotnetcli
    java exec.jar transform [path].map
    ```
- transform batch
    ```bash
    java exec.jar transform_batch [path].json
    ```