# SIA32-Emulator

|              | 3R (11)                                     | 2R (10)                                 | Dest only (01)          | No R (00)                             |
| ------------ | ------------------------------------------- | --------------------------------------- | ----------------------- | ------------------------------------- |
| Math (000)   | Rd <- Rs1 MOP Rs2                           | Rd <-  Rd MOP Rs                        | COPY: Rd <-  imm        | HALT                                  |
| Branch (001) | pc <-  Rs1 BOP Rs2 ? pc + imm : pc          | pc <- Rs BOP Rd? pc + imm : pc          | JUMP: pc <- pc + imm    | JUMP: pc  <- imm                      |
| Call (010)   | pc <-  Rs1 BOP Rs2 ? push pc; Rd + imm : pc | pc <- Rs BOP Rd? push pc; pc + imm : pc | push pc; pc <- Rd + imm | push pc; pc <- imm                    |
| Push (011)   | mem[--sp] <- Rs1 MOP Rs2                    | mem[--sp] <- Rd MOP Rs                  | mem[--sp] <- Rd MOP imm | UNUSED                                |
| Load (100)   | Rd <- mem [Rs1+ Rs2]                        | Rd <- mem[Rs + imm]                     | Rd <- mem [Rd + imm]    | RETURN (pc <- pop)                    |
| Store (101)  | Mem[Rd + Rs1] <- Rs2                        | mem[Rd + imm] <- Rs                     | Mem[Rd] <- imm          | UNUSED                                |
| Pop (110)    | PEEK: Rd <- mem [sp – (Rs1+ Rs2)]           | PEEK: Rd <- mem[sp – (Rs +  imm)]       | POP: Rd  <- mem[sp++]   | INTERRUPT: Push pc; pc <- intvec[imm] |

