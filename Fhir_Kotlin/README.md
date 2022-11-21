# FHIR Helpers VsCode Extension

This is a collection of useful features for working with some of the resources in the FHIR spec for example the Questionnaires and Structure Maps.

## Flags and optiones

- Compile structure map    
    - This expects a structure map and compiles it into the Json equivalent 
    ```bash 
    java exec.jar compile [path].map
    ```
- transform single
    - This expects a structure map and QuestionnaireResponse, then transforms it using the structure maps
    
    ```bash
    java exec.jar transform [path].map
    ```
- transform batch
    - This command expects Structure map transform Json
    ```bash
    java exec.jar transform_batch [path].json
    ```