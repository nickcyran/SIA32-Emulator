# SIA32-Processor Emulator

<p align="center">
  <img src="https://github.com/user-attachments/assets/6075443c-dd63-4864-a842-122e2dc6e252" alt="sia32" width="250" />
</p>
<p align="center">
  <img src="https://img.shields.io/badge/Project%20Status-Completed-brightgreen" alt="Project Status: Completed">
</p>

This SIA32 Emulation program serves as a comprehensive simulation of a custom 32-bit computer system. The project incorporates different computer hardware components and their interactions; including a CPU, main memory with caching mechanizms, and an Arithmetic Logic Unit (ALU). Adittionaly, in order to facilitate interaction with the emulated hardware, a custom assembly language and assembler were developed.

## Motivation & Purpose
This project serves to deepen my understanding of computer architecture by applying theoretical knowledge in a practicle, hands-on way. Built from the ground up as a final project for my University's computer architecture class, this program aided my comprehension of CPU design, memory systems, caching strategies, and assembly language programming.

## Table of Contents
* [Key Features](#key-features)
* [Operation Codes](#operation-codes)
* [Instruction Breakdown](#instruction-breakdown)
* [Performance Highlights & Cache Impact](#performance-highlights--cache-impact)
* [Project Learnings and Conclusion](#project-learnings-and-conclusion)

## Key Features

* **32-bit SIA32 Architecture:** Emulates a custom 32-bit processor.
* **CPU Emulation (`Processor.java`):** Simulates the fetch-decode-execute-store cycle with PC, SP, and 32 registers.
* **Hierarchical Memory System:**
    * **Main Memory (`MainMemory.java`):** 4KB DRAM for program data.
    * **L2 Cache (`L2Cache.java`):** 4-way set-associative cache (8 words/block) for faster memory access.
    * **Instruction Cache (`InstructionCache.java`):** 8-word cache dedicated to speeding up instruction fetching.
* **Arithmetic Logic Unit (`ALU.java`):** Performs 32-bit arithmetic (add, subtract, multiply) and logical (NOT, AND, OR, XOR, shifts) operations.
* **Custom Assembler (`assembler` package):** Translates SIA32 assembly into binary, featuring a `Lexer.java` and `Parser.java`.
* **Rich Instruction Set:** Includes math, branch (conditional/unconditional), call, stack (push/pop/peek), memory (load/store), and control flow (halt/return) instructions.
* **Comprehensive JUnit Testing:** Includes unit tests for components, assembler accuracy, and integrated tests for end-to-end functionality.

## Operation Codes
The SIA32 instruction set uses a 5-bit opcode to define the main operation, with the two least significant bits of the field indicating the register addressing mode.

| Opcode Bits (Instruction Type) | **3R (reg-mode `11`)** | **2R (reg-mode `10`)** | **Dest only (reg-mode `01`)** | **No R (reg-mode `00`)** |
| :----------------------------- | :------------------------------------------ | :---------------------------------------------- | :------------------------ | :------------------------------------ |
| **Math (`000`)** | `Rd <- Rs1 MOP Rs2`                         | `Rd <-  Rd MOP Rs`                              | `COPY: Rd <-  imm`        | `HALT`                                |
| **Branch (`001`)** | `pc <-  Rs1 BOP Rs2 ? pc + imm : pc`        | `pc <- Rs BOP Rd ? pc + imm : pc`               | `JUMPBY: pc <- pc + imm`  | `JUMPTO: pc  <- imm`                  |
| **Call (`010`)** | `pc <-  Rs1 BOP Rs2 ? (push pc; Rd + imm) : pc` | `pc <- Rs BOP Rd ? (push pc; pc + imm) : pc`    | `push pc; pc <- Rd + imm` | `push pc; pc <- imm`                  |
| **Push (`011`)** | `mem[--sp] <- Rs1 MOP Rs2`                  | `mem[--sp] <- Rd MOP Rs`                        | `mem[--sp] <- Rd MOP imm` | UNUSED                                |
| **Load (`100`)** | `Rd <- mem[Rs1 + Rs2]`                      | `Rd <- mem[Rs + imm]`                           | `Rd <- mem[Rd + imm]`     | `RETURN (pc <- pop)`                  |
| **Store (`101`)** | `Mem[Rd + Rs1] <- Rs2`                      | `mem[Rd + imm] <- Rs`                           | `Mem[Rd] <- imm`          | UNUSED                                |
| **Pop (`110`)** | `PEEK: Rd <- mem[sp – (Rs1 + Rs2)]`         | `PEEK: Rd <- mem[sp – (Rs +  imm)]`             | `POP: Rd  <- mem[sp++]`   | `INTERRUPT: Push pc; pc <- intvec[imm]` |

*MOP (Math Operation), BOP (Boolean/Branch Operation), `imm` (Immediate Value), `pc` (Program Counter), `sp` (Stack Pointer)*,
*`Rd` (Destination Register), `Rs` / `Rs1` / `Rs2` (Source Registers)*\*

## Instruction Breakdown

SIA32 instructions are 32-bit words whose internal layout is determined by the register addressing mode.

**3 Register (3R) - `11`**
| Immediate (8 bits) | Rs1 (5 bits) | Rs2 (5 bits) | Function (4 bits) | Rd (5 bits) | Opcode (3 bits) + `11` (2 bits) |
| :----------------- | :----------- | :----------- | :---------------- | :---------- | :------------------------------------- |

**2 Register (2R) - `10`**
| Immediate (13 bits) | Rs (5 bits) | Function (4 bits) | Rd (5 bits) | Opcode (3 bits) + `10` (2 bits) |
| :------------------ | :---------- | :---------------- | :---------- | :------------------------------------- |

**Destination Only (1R) - `01`**
| Immediate (18 bits) | Function (4 bits) | Rd (5 bits) | Opcode (3 bits) + `01` (2 bits) |
| :------------------ | :---------------- | :---------- | :------------------------------------- |

**No Register (0R) - `00`**
| Immediate (27 bits) | Opcode (3 bits) +`00` (2 bits) |
| :------------------ | :------------------------------------- |

*The "Function" field specifies the arithmetic/logical operation (e.g., ADD, XOR) or branch condition (e.g., EQ, LT).*

## Performance Highlights & Cache Impact

The implementation of caching mechanisms was a critical part of this project, demonstrating substantial performance improvements. The table below, based on metrics collected from the emulator, shows the clock cycles for various programs at different stages of cache implementation:

| Task | Pre-Cache (Cycles) | Instruction-Cache Only (Cycles) | With L2-Cache (Cycles) | Access from L2 (Cycles) |
| :--- | :----------------- | :-------------------------------| :-----------------------------| :---------------------- |
| Summing 20 Array Entries              | 50606 | 16756 | 18966 | 10966 |
| Summing 20 Linked List Entries        | 86198 | 29238 | 30488 | 24168 |
| Summing 20 Array Entries (Reverse)    | 51814 | 17094 | 19424 | 14554 |

**Cache Performance Analysis:**
* **Pre-Cache:** No caching, established a baseline with the highest cycle count.
* **Instruction-Cache Only:** Clock cycles reduced to ~1/3 of pre-cache, highlighting single-level caching benefits for instruction fetching.
* **L2-Cache Implemented (Instruction Cache Accessing L2):** Slight cycle increase versus instruction-cache-only due to smaller instruction cache underutilizing L2 memory.
* **Access from L2 (Operations Accessing L2):** Best performance achieved; operations directly accessing L2 for reads/writes allowed proper cache utilization.

 **Overall Impact:** Optimized L2 access dramatically cut clock cycles to ~1/4 of initial values, underscoring caching's importance. Linked lists were slowest (non-contiguous access); reverse array summation was second slowest (less effective forward caching).

## Project Learnings and Conclusion
**Key takeaways:**
* **The Power of Caching:** Caching was essential for performance, reducing clock cycles to ~1/4 of pre-cache. Iterative cache implementation (instruction cache, L2 cache, optimized L2 access) showed how memory hierarchies reduce clock cycles.
* **Impact of Memory Access Patterns:**   Performance varied depending on data structures and access patterns, highlighting where the caching mechanism is strongest. For instance, non-sequential access in linked lists and reverse traversal of arrays showed less cache efficiency.
* **Complexity of Hardware-Software Interface:** Implementing the ISA and assembler demonstrated the complexities between hardware and software, where ISA prompts directly impacted assembler complexity and program efficiency.
* **Importance of Modular Design and Testing:** A component-based architecture (ALU, Memory, Processor, Caches) simplified development and allowed for crucial unit testing before system integration.

In conclusion, the SIA32-Emulator project successfully created a functional 32-bit system simulation. It proved to be a valuable endeavor, emphasizing concepts of CPU operation, memory hierarchy, and the performance impact of architectural choices.
