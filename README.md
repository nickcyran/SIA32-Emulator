# SIA32-Emulator

This project is an emulator for a custom 32-bit computer architecture called SIA32. The emulator simulates the hardware components along with their interactions, including a CPU, main memory with caching mechanisms, and an Arithmetic Logic Unit (ALU). To facilitate interaction with the emulated hardware, a custom assembly language and an assembler were developed.

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
