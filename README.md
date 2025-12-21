
# Xyn Compiler

## Introduction

Xyn is an **experimental**, **self-designed programming language**
and compiler project focused on ***clarity***, ***control***,
and ***explicitness***, while still being grounded in solid compiler
theory and modern implementation techniques.

The project is developed entirely in **Java**, with the
explicit goal of being ***understandable***, ***extensible***,
and ***maintainable*** **over time**.

This repository documents the **design decisions**,
**internal architecture**, and **theoretical foundations** of the
Xyn compiler.

> Xyn is **not designed to compete** with mainstream languages.
> It is a **learning-driven** and **research-oriented project**
> that prioritizes understanding how compilers really work.

## Preview

### Basic Types

#### There are **5 basic types** in Xyn, and **2 condition** that can be applied to all type.

- The **five basic types** in Xyn are **str** (string), **int** (integer), **float** (decimal numbers), **char** (character), and **bool** (boolean).\
  These types on **default** cannot be a **null**. But, **what is a** ***null***?

- A **null** is a condition where in case of variable, that variable **holds a state** where it's value was **nothing or void**.\
  But on case of **literal values**, **null** means a value of **nothing or void** that have **no real meaning**.\
  You **can't do operation** with it **nor use it in your program**.

- So to prevent that, Xyn's **on default not allowed** a value to be a **nullable**.\
  But at some point, void can show up from **hardware** or **program's** state randomly. Making a value **must be a nullable** to accept that void.

- For that problem, Xyn then use the same idea from **C# nullability**. On default, types cannot be nullable.\
  But with "**?**" **operator**, it states that the types **can be nullable**.

### Examples:
```rs

let age: int = 12;
// this is not allowed because `age` is not nullable
age = null;

// but this is allowed
let name: str? = "John";
name = null;

let johnMoney: float = 200f;
let bank: List<float> = new List<float>();
// this is allowed because `bank` is a list of not nullable float
bank.add(johnMoney);
// so you can't write this
bank.add(null);


```

### Type Inference and Static Typing

Xyn provides **type inference** for statement declarations such as **variables**, **functions**, and more.  
Xyn also supports **static typing**, which offers its own advantages compared to **type inference**.

#### Benefits of Type Inference

- **Shorter and less boilerplate** declarations for **long** and **repetitive** types.  
  Write **what matters**, not **redundant** information  
  (e.g. *SystemInformerInterface*, *List<Map<Str, Set<Integer>>>*, etc.).

- **Improved local reasoning**.  
  Code becomes easier to read from top to bottom without **mentally parsing** explicit type declarations.  
  Especially powerful for **expressions**, **closures / lambdas**, and **temporary values**.

- **Safer and faster refactoring**.  
  Changing a single definition is easier when types are inferred, because the compiler **automatically propagates type changes**.

- Enables powerful **abstractions**.  
  Type inference makes **generics**, **pattern matching**, and other advanced features much easier to write without *explicitly* specifying type parameters.

### Examples

```rs
// before
let list: List<Integer?> = new List<Integer?>();

// after (note: `int` is lowered to `Integer` by the compiler)
let list = new List<int?>();


// before
let type: enumType = enumType.A;

// after
let type = enumType.A;


// before
func put(x: List<Integer>, y: int) -> List<Integer> {
  x.add(y);
  return x;
}

// after
func put(x: List<int>, y: int) {
  x.add(y);
  return x;
}
```
The examples above demonstrate how type inference works in Xyn.
However, some developers prefer **explicit static typing** for **clarity** and **intent**.
Xyn **fully supports** static typing, enhanced with **additional syntactic sugar** to keep code ***concise*** and ***expressive***.
### Benefits of Static Typing:

- Extra **clarity** and **safety**.\
  These are especially important in large projects with **multiple contributors**, where developers **cannot always infer** each other’s **intentions**. Explicit types      serve as a **clear contract** and **documentation** for shared code. Bugs and errors are caught earlier, before the program runs.

- With **static typing**, the compiler can detect **invalid type usage** at **compile time**.

- **Simpler** and **faster** runtime **execution**.\
  Since types are **fully known** at **compile time**, the runtime can **be simpler**, **more predictable**, and **more optimized** without ***dynamic dispatch*** or        other ***complex runtime mechanisms***.

### Examples
```rs
// before
let john: Person = new Person("John");

// after
let john: Person("John");


// before
let personJob: JobType = JobType.None;

// after
let personJob: JobType = None; // resolved to JobType.None at compile time


// before
let name = null; // unknown type

// after
let name: str? = null; // nullable string
name = "John";
```

---

## Project Goals
####
- **Design and implement** a full compiler pipeline **from scratch**.


- **Prioritize imperative semantics** and **explicit control flow**.


- Keep abstractions **minimal and understandable**.


- Avoid unnecessary **magic or hidden behavior**.


- **Learn and apply** real-world compiler techniques
  (used in **LLVM**, **JVM**, and modern **PLs**)


---

## Language Philosophy
####
**Xyn is intentionally designed around these principles**:

1. **Imperative-first**: You **explicitly describe** ***how***
   things happen, not just ***what*** happens.

2. **Predictable semantics**: Language behavior should be
   **obvious** from reading the code.

3. **Minimal core**: The language starts small and grows
   deliberately.

4. **Bootstrappable**: Even fundamental concepts (like strings)
   are not assumed ***magically***.

**The language intentionally avoids overly abstract or
purely functional paradigms, favoring transparency and control**.


---

## Implementation Language

**Xyn made from purely all Java ecosystem and tooling**.

### Java was chosen because:

1. **Strong tooling** and **debuggability**.

2. **Explicit memory** and **object model**.

3. **Easier long-term maintenance**.

4. **Better alignment** with **imperative reasoning**.




---

### Compiler Architecture Overview

**The Xyn compiler follows a traditional but
carefully designed pipeline**:

> **Source Code** → **Lexer** (Lexical Analysis) → **Parser**
> (Syntax Analysis) → **AST** (Abstract Syntax Tree)
> → **Type Checker** (Semantic Analysis) → **IR**
> (Intermediate Representation with Three-Address Code)
> → **SSA Transformation** → **Optimizations**
> → **Backend** / **Code Generation** (planned).

**Each stage is intentionally isolated and inspectable**.

---

### Memory Management Strategy

> Fast allocation, cheap deallocation (bulk-free)

#### Especially suited for:

- AST nodes

- IR instructions

- Temporary compiler data


**This mirrors techniques used in real compilers** (**LLVM**, **GCC**).


---

## Error Reporting & Diagnostics

### A major focus of Xyn is high-quality diagnostics:

- **Colored error output**.\
  **Error message** colored **white**, **Error location** with **red**, and others with **green**.

- **Exact** source highlighting.\
  Xyn's Error Engine use **span location** from **every Error class** and then display it to the screen.

- **Multiple severity levels**.\
  Xyn's errors divided **line by line**, start from **one** and **so on** orderly.

- **Clear messages** specified to aimed at **humans**, **not machines**.


**The goal is for error messages to teach, not merely report failure**.

---


### What are done for now:

#### Features:
**[version 0.01]**
- **Basic variable declaration**.
- **Variable declaration supports static typing and type inference**.
- **Supports three types** (i.e, String, Integer, Float)

#### Technical:
**[version 0.01]**
- **Lexer**, supports assignment operator (e.g., ident, equal sign, let keyword, etc.), three values (i.e, int, float, string), and operation operators.
- **Parser**, supports basic declarations and error recovery with maximum lookahead are 5.
- **Semantic**, have a type checking task that check expression type and compare it with it's declared type (if declared).
- **HIR (High IR) generator**, can generate basic declarations IR based on three address code, do type conversion, and do optimization to the generated IR.
- **HIR Pass** (subset of IR generator), do optimization on the generated IR for multiple pass.
- **Error Engine**, stores all error. When reporting, it separates all the errors line by line orderly from least to greatest, and display the errors with pretty output.

---

### Planned & Ongoing Work

> Disclaimer: **Feature or technical stuff maybe appear not as the expected version**.

#### Feature:
**[for version 0.05]**
- **Simple control flow statement** (e.g., if and else statement).

#### Technical:
**[for version 0.02]**
- **Completing the HIR Pass and makes the optimization works**.
- **Clean the HIR code and make it more modular**

**[for version 0.03]**
- **Make a LIR (Low IR) Generator that generate VM-ready instruction from the HIR**.

**[for version 0.04]**
- **Make a LIR storer that will store the LIR code into a .xir file**
- **Make a VM that used the LIR and execute it**.
---

## Inspirations & References

### Xyn is inspired by:

- **LLVM IR** & **SSA design**.

- **JVM** and **bytecode-based architectures**.

- **ML-family compiler theory** (conceptually).

- **Classic compiler literature** (Dragon Book–style pipelines).

- And some features from other programming languages.


**While inspired by many systems, Xyn deliberately avoids copying any single one**.


---

## Contribution & Collaboration

#### This project is open to critique, discussion, and collaboration.

#### Design criticism is welcome.

#### Alternative approaches are encouraged.

#### Discussions about trade-offs are valued.


#### If you are interested in:

- **Compiler design**.

- **Programming language theory**.

- **IR** / **SSA** / **optimizations**.


**Feel free to open issues, propose changes, or start discussions**.


---

### Status

#### Xyn is an active experimental project.

### Expect:

* Breaking changes

* Refactors

* Design evolution


**The primary goal is deep understanding, not short-term stability.**


---

#### Xyn exists to explore how programming languages are built — deliberately, transparently, and thoughtfully.
