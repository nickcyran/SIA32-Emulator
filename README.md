# SIA32-Emulator

<p align="center">
  <img src="https://github.com/user-attachments/assets/6075443c-dd63-4864-a842-122e2dc6e252" alt="sia32" width="300" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Project%20Status-Completed-brightgreen" alt="Project Status: Completed">
</p>

**Dive into the world of computer architecture with the SIA32-Emulator!**

SIA32-Emulator is a comprehensive simulation of a custom 32-bit computer system. This project was developed as a final project for a university computer architecture course to provide a hands-on learning experience with the fundamental concepts of computer design and operation.

It allows you to explore everything from the CPU's fetch-decode-execute cycle and memory hierarchies (including L1 and L2 caching) to writing and running programs in a unique assembly language. Perfect for students, hobbyists, and anyone curious about how computers work at a fundamental level.


## Motivation & Purpose

The primary motivation for this project was to apply theoretical knowledge of computer architecture in a practical setting. It served as a final project, aiming to build a working emulator for a custom 32-bit instruction set architecture (ISA), thereby solidifying understanding of CPU design, memory systems, and assembly language.

## Overview of Features

* **Full 32-bit Architecture Simulation:** Emulates a custom SIA32 architecture.
* **CPU Emulation:** Simulates CPU cycles (Fetch, Decode, Execute, Store).
* **Memory System:** Includes Main Memory (DRAM), L2 Cache, and Instruction Cache.
* **ALU Operations:** Performs arithmetic and logical operations.
* **Custom Assembly Language:** Comes with its own assembler to translate assembly to machine code.
* **Comprehensive Instruction Set:** Supports Math, Branch, Call, Memory, and Control Flow operations.
* **JUnit Testing:** Thoroughly tested components.

## Table of Contents
* [Key Features](#key-features)
* [Operation Codes](#operation-codes)
* [Instruction Breakdown](#instruction-breakdown)


## Key Features:

* **32-bit Architecture:** Emulator is built around a 32-bit word size.
* **CPU Emulation:** A `Processor` class simulates the CPU, handling the fetch-decode-execute-store cycle. It includes a Program Counter (PC), Stack Pointer (SP), and a set of 32 general-purpose registers.
* **Memory System:**
    * **Main Memory (DRAM):** Simulates 4KB of main memory (1024 Words). The `MainMemory` class provides functionality to read, write, and load data from files into memory.
    * **L2 Cache:** An `L2Cache` is implemented to improve memory access times. It's a 4-way set-associative cache with 8 words per block. It handles read, write, and block-filling operations.
    * **Instruction Cache:** A dedicated `InstructionCache` (8 words) works with the L2 cache to speed up instruction fetching.
* **Arithmetic Logic Unit (ALU):** The `ALU` class performs various arithmetic and logical operations on 32-bit words, such as addition (2-input and 4-input), subtraction, multiplication, NOT, AND, OR, XOR, and bitwise shifts.
* **Custom Assembly Language & Assembler:**
    * An assembler (`Assembler` class located in the `assembler` package) translates assembly code written in a custom language into machine-readable binary instructions for the SIA32 architecture.
    * The assembler includes a `Lexer` for tokenizing the assembly code and a `Parser` to generate the corresponding binary instructions based on a defined instruction set architecture.
* **Instruction Set:** The emulator supports a range of instructions including:
    * **Math Operations:** ADD, SUBTRACT, MULTIPLY, AND, OR, XOR, NOT, SHIFT (LEFT/RIGHT), COPY (immediate to register).
    * **Branch Operations:** Conditional and unconditional jumps (JUMPTO, JUMPBY).
    * **Call Operations:** Procedure calls with stack management.
    * **Memory Operations:** PUSH, POP (with PEEK functionality), LOAD, STORE.
    * **Control Flow:** HALT, RETURN.
* **Testing:** The project includes comprehensive JUnit tests for all hardware components ensuring functionalities, as well as assembler tests and integrated tests for assembled code.

## Operation Codes

|                  | **3R (11)** | **2R (10)** | **Dest only (01)** | **No R (00)** |
| :--------------- | :------------------------------------------ | :-------------------------------------- | :---------------------- | :------------------------------------ |
| **Math (000)** | Rd <- Rs1 MOP Rs2                           | Rd <-  Rd MOP Rs                        | COPY: Rd <-  imm        | HALT                                  |
| **Branch (001)** | pc <-  Rs1 BOP Rs2 ? pc + imm : pc          | pc <- Rs BOP Rd? pc + imm : pc          | JUMP: pc <- pc + imm    | JUMP: pc  <- imm                      |
| **Call (010)** | pc <-  Rs1 BOP Rs2 ? push pc; Rd + imm : pc | pc <- Rs BOP Rd? push pc; pc + imm : pc | push pc; pc <- Rd + imm | push pc; pc <- imm                    |
| **Push (011)** | mem[--sp] <- Rs1 MOP Rs2                    | mem[--sp] <- Rd MOP Rs                  | mem[--sp] <- Rd MOP imm | UNUSED                                |
| **Load (100)** | Rd <- mem [Rs1+ Rs2]                        | Rd <- mem[Rs + imm]                     | Rd <- mem [Rd + imm]    | RETURN (pc <- pop)                    |
| **Store (101)** | Mem[Rd + Rs1] <- Rs2                        | mem[Rd + imm] <- Rs                     | Mem[Rd] <- imm          | UNUSED                                |
| **Pop (110)** | PEEK: Rd <- mem [sp – (Rs1+ Rs2)]           | PEEK: Rd <- mem[sp – (Rs +  imm)]       | POP: Rd  <- mem[sp++]   | INTERRUPT: Push pc; pc <- intvec[imm] |

## Instruction Breakdown

**3 Register (3R) - 11**
| Immediate (8) | Rs1 (5) | Rs2 (5) | Function (4) | Rd (5) | Opcode (5) |
| :------------ | :------ | :------ | :----------- | :----- | :--------- |

**2 Register (2R) - 10**
| Immediate (13) | Rs (5) | Function (4) | Rd (5) | Opcode (5) |
| :------------- | :----- | :----------- | :----- | :--------- |

**Dest Only (1R) - 01**
| Immediate (18) | Function (4) | Rd (5) | Opcode (5) |
| :------------- | :----------- | :----- | :--------- |

**No Register (0R) - 00**
| Immediate (27) | Opcode (5) |
| :------------- | :--------- |


