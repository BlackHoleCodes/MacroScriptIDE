# MacroScriptIDE

A custom compiler and IDE which allows for the creation of MacroDroid macros in a traditional text-based code environment.

You write a macro as a `.ms` file in **MacroScript**, a small Java-like language. The IDE
compiles it to the JSON that MacroDroid exports and imports. You copy that file to the phone
and import it in MacroDroid. The IDE can also turn an exported macro back into MacroScript.

```
@name("WiFi & Bluetooth Scheduler")
@description("Turns WiFi and Bluetooth on at 7am and off at 11pm, then vibrates")

triggers {
    timer(hour: 7, minute: 0, daysOfWeek: EVERY_DAY) as morning;
    timer(hour: 23, minute: 0, daysOfWeek: EVERY_DAY);
}

actions {
    if (triggeredBy(morning)) {
        setWifi(TURN_ON);
        setBluetooth(TURN_ON);
    } else {
        setWifi(TURN_OFF);
        setBluetooth(TURN_OFF);
    }
    vibrate(SHORT_BUZZ);
}
```

The full language is documented in [docs/MacroScript_Reference.html](docs/MacroScript_Reference.html)
(also in the IDE under **Help > MacroScript Reference**, F1). The `examples/` folder is a
small project you can open.

---

## Requirements

- **Java 21 or newer** (a JDK to build, a JRE is enough to run). Linux, Windows and macOS.
- Nothing else: the Maven wrapper (`mvnw`, `mvnw.cmd`) downloads Maven the first time.

## Build

```sh
./mvnw package          # Linux / macOS
mvnw.cmd package        # Windows
```

This runs the tests and writes one runnable file: `target/macroscript-ide-1.0.0.jar`.
Add `-DskipTests` to build faster.

The project also builds in an IDE (IntelliJ IDEA, Eclipse, VS Code) without running Maven
first: open `pom.xml` as a Maven project. Set the project JDK to 21 or newer.

## Run

- Double-click `target/macroscript-ide-1.0.0.jar`, or
- `./macroscript.sh` (Linux/macOS) or `macroscript.bat` (Windows), or
- `java -jar target/macroscript-ide-1.0.0.jar [project folder]`

The IDE reopens the last project.

## Using the IDE

| Task | How |
| --- | --- |
| Make a project | **File > New Project**, choose an empty folder. A starter macro is created. |
| Open a project | **File > Open Project** (Ctrl+Shift+O), or **File > Recent Projects**. |
| New macro / action block | **File > New Macro** (Ctrl+N) / **New Action Block** (Ctrl+Shift+N), or right-click a folder in the project tree. |
| See errors | Errors and warnings are underlined as you type. Hover for the message. All of them are in the **Problems** panel; double-click one to go there. |
| Complete a name | Ctrl+Space: commands (with their arguments), keywords, annotations, `sys.` values. |
| Build the current file | **Build > Build Current File** (Ctrl+Shift+F9). |
| Build every macro | **Build > Build Project** (Ctrl+F9). Action block files are built into the macros that call them. |
| Find the output | **Build > Open Output Folder**. Each macro becomes `out/<file name>.json`. |
| Decompile an export | **File > Decompile MacroDroid Export...**, or right-click a `.json`/`.macro` file in the tree. One `.ms` file is written for the macro and one per action block. |
| Find / replace | Ctrl+F / Ctrl+R. Go to line: Ctrl+G. Toggle comment: Ctrl+/. |
| Theme / font | **View > Toggle Dark Theme**, Ctrl+= and Ctrl+- for the font size. |

On macOS, Ctrl is Cmd.

A project is a folder. The IDE keeps its settings (open tabs, output folder) in the hidden
folder `.macroscript/` inside it. Builds save all open files first.

### Importing into MacroDroid

Copy the `.json` file to the phone, then in MacroDroid use **Import Macro** (in the Macros
screen menu). MacroDroid's own export files have the extension `.macro`; the content is the
same JSON, so rename the file if the import screen does not show it. Global variables are
never written to the file: create them on the phone first (see `global` in the reference).

## Command line

The same jar compiles and decompiles without opening a window:

```sh
java -jar target/macroscript-ide-1.0.0.jar compile examples                # every macro in a folder
java -jar target/macroscript-ide-1.0.0.jar compile my.ms -o build          # one file, chosen output folder
java -jar target/macroscript-ide-1.0.0.jar decompile export.json -o src    # export -> .ms files
```

Errors print as `file:line:column: error: message`. The exit code is 1 when a file failed.

---

## How the code is organised

```
src/main/antlr4/.../MacroScript.g4   The grammar. The only list of keywords.
src/main/antlr-generated/            The lexer and parser that ANTLR generates from the grammar.
                                      Committed so IDEs compile without Maven; do not edit by hand.
src/main/resources/.../macroscript-mapping.json
                                      The command table: all 419 triggers, constraints and actions.

com.blackholecodes.macroscript
├── Main                 Starts the IDE, or the command line for "compile"/"decompile".
├── mapping              Reads macroscript-mapping.json into CommandSpec / ParamSpec records.
├── parser               Source text -> syntax tree (ANTLR parse tree -> ast records).
├── ast                  The syntax tree: Expr, Stmt, Declaration, MacroFile ...
├── diagnostics          Errors and warnings with source positions.
├── compiler             Syntax tree -> MacroDroid JSON.
├── kinds                One handler per parameter kind (enum, duration, app, ...):
│                        MacroScript value -> JSON, and JSON -> MacroScript value.
├── decompiler           MacroDroid JSON -> MacroScript source.
├── build                Compiles files to disk and decompiles exports. Shared by IDE and CLI.
├── cli                  The command line.
└── ide                  The Swing/FlatLaf window.
    └── editor           Syntax colouring (TokenMaker), error underlines, completion, colours.
```

### The compiler, step by step

1. `SourceParser` parses the text. `AstBuilder` turns the ANTLR tree into the records in
   `ast`; `StringLiteralReader` splits strings at their `${...}` parts.
2. `Compiler` creates a `Compilation` (the state of one file) and runs `MacroCompiler`, which
   calls, in order: `AnnotationCompiler`, `VariableCompiler`, `TriggerCompiler`,
   `ConditionCompiler` (constraints block), `ActionCompiler` (actions block).
3. A call such as `setWifi(TURN_ON)` goes to `CommandEmitter`. It looks the command up in the
   mapping, matches the arguments to parameters, and asks the handler of each parameter's kind
   (`kinds` package) to write the JSON. It also checks required arguments and leaves out
   arguments that hold the default value.
4. Language syntax has its own classes: `AssignmentCompiler` (`x = ...` to SetVariableAction),
   `ConditionCompiler` (comparisons, `&&`, `||`, `!`), `ActionCompiler` (if, loops, for),
   `HelperCalls` (`wait`, `waitFor`, `confirm`, `raw`), `ActionBlockLinker` (`callBlock`, `block`).
5. `MagicText` writes MacroDroid's `{lv=name}` codes for `${name}`.

Every part reports problems to the `Compilation` and carries on, so one run finds all errors.

The decompiler mirrors this: `MacroDecompiler` writes the blocks, `ItemDecompiler` uses the
same mapping and kind handlers in reverse, `ConditionDecompiler`, `AssignmentDecompiler` and
`ActionDecompiler` undo the syntax. Anything it cannot write exactly becomes `raw(...)`, so
decompiling and compiling again gives the same macro.

### Changing things

- **A command is missing or wrong**: edit `macroscript-mapping.json`. The compiler, the
  decompiler, completion and the tests read it; no Java change is needed.
- **A new parameter kind**: add it to `ParamKind`, write a `ParamKindHandler` in `kinds`, and
  register it in `ParamKinds`. The program refuses to start if a kind has no handler.
- **A new helper like `wait(...)`**: add a method to `HelperCalls` and a case to
  `ActionCompiler.compileCall`; add the reverse to `ActionDecompiler`.
- **A change to the grammar**: edit `MacroScript.g4`, then run `./mvnw generate-sources` (any
  Maven build does it too) and commit the files it changes in `src/main/antlr-generated/`.
  `GeneratedParserTest` fails if you forget.
- **A new keyword**: add it to the grammar as above. The editor colours it automatically (it
  reads the keywords from the generated lexer). Add it to `reservedWords` in the mapping too;
  `KeywordsTest` fails if the two lists differ.

### Tests

`./mvnw test` runs about 480 tests:

- `GeneratedParserTest` checks that the committed parser matches the grammar.

- `AllCommandsTest` calls every command of the mapping with sample values, compiles it,
  decompiles it and compiles it again, and checks the JSON is the same.
- `LanguageFeaturesTest` and `ReferenceExamplesTest` check the rows and examples of the
  language reference, including the exact output the reference shows for the scheduler example.
- `CompileErrorsTest` checks the error messages.
- `DecompilerTest`, `BuildServiceTest`, `CommandLineTest`, `MacroScriptTokenMakerTest`,
  `KeywordsTest` and `MappingLoaderTest` cover the rest.

### Choices the reference leaves open

- **Optional arguments with their default value are left out** of the JSON, as the compiler
  rules say. The decompiler still shows a command's main argument: `airplaneMode(false)`.
- **Arguments that the mapping marks `requiredWhen`** give a warning, not an error, when they
  are missing. The mapping's conditions do not know every alternative a command has; for
  example the reference's own `playSound(staticFilename: false, useAllFilesAccess: true, ...)`
  example would otherwise fail. Arguments marked `required` are errors.
- **`callBlock` inputs and outputs** are written as `inputVarsMap: {blockVariable: value}`
  (value as magic text) and `outputVarsMap: {blockVariable: macroVariable}`. Dict entries add
  `inputDictionaryMap` / `outputDictionaryMap`.
- **A global variable used only inside text** is declared `global string` by the decompiler,
  with a comment, because an export does not say its type.
