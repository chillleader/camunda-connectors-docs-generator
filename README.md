# Documentation generator for Camunda Connectors

Automate documentation generation with AI.

## Build & run

1. Build the project
```
mvn clean install
```

2. Use the `target/docs-generator.jar` as an executable
3. Set your OpenAI API key environment variable

```
export OPENAI_KEY=<your key>
```
5. Run

```
java -jar docs-generator.jar <path-to-your-element-template>
```

The result will be stored in `README.md` file in the working directory.
