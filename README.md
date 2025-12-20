
## Xyn Compiler

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

## Lexical Analysis (aka Lexer)

Tokens store source indices (start & end) instead of copied strings

All token text is derived lazily from the original source

Enables:

Zero-copy lexing

Accurate error reporting

Precise syntax highlighting



Special care is taken when lexing:

String literals

Keywords vs identifiers

Error recovery



---

Parsing

Produces a clean Abstract Syntax Tree (AST)

AST nodes are lightweight and explicit

Syntax errors are reported with:

Source ranges

Colored diagnostics

Human-readable explanations




---

Intermediate Representation (IR)

Xyn uses a three-address code–style IR inspired by real-world compilers.

Example:

t0 = const 12
t1 = const 12.3
t2 = const 11
t3 = itof t2
t4 = mul t1 t3
t5 = itof t0
t6 = add t5 t4
store a t6

Characteristics:

Explicit temporaries

Explicit conversions (e.g. itof)

No hidden operations


This IR serves as a bridge between high-level syntax and low-level reasoning.


---

SSA (Static Single Assignment)

The IR is progressively transformed into SSA form:

Each variable is assigned exactly once

New versions are created on reassignment

Enables powerful optimizations


Key concepts implemented or explored:

SSA renaming

Basic blocks

Control-flow graph (CFG)

Φ (phi) nodes for merging control flow


SSA is treated not as magic, but as a mechanical transformation of IR.


---

Memory Management Strategy

Uses arena allocators internally

Fast allocation, cheap deallocation (bulk-free)

Especially suited for:

AST nodes

IR instructions

Temporary compiler data



This mirrors techniques used in real compilers (LLVM, GCC).


---

Error Reporting & Diagnostics

A major focus of Xyn is high-quality diagnostics:

Colored error output

Exact source highlighting

Multiple severity levels

Clear messages aimed at humans, not machines


The goal is for error messages to teach, not merely report failure.


---

Planned & Ongoing Work

Control-flow–aware optimizations

Dead code elimination

Constant folding & propagation

Backend design (bytecode or native)

Runtime model

Garbage collection strategy

Self-hosting experiments (long-term)



---

Inspirations & References

Xyn is inspired by:

LLVM IR & SSA design

JVM and bytecode-based architectures

ML-family compiler theory (conceptually)

Classic compiler literature (Dragon Book–style pipelines)


While inspired by many systems, Xyn deliberately avoids copying any single one.


---

Contribution & Collaboration

This project is open to critique, discussion, and collaboration.

Design criticism is welcome

Alternative approaches are encouraged

Discussions about trade-offs are valued


If you are interested in:

Compiler design

Programming language theory

IR / SSA / optimizations


Feel free to open issues, propose changes, or start discussions.


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