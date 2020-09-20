# MIPS Assembler

This project is an implementation of a MIPS assembler, that is, a program taking MIPS assembly instructions from in mnemonic form and encoding them as hexadecimal values that can be recognized by the processor. The assembler takes one input file containing the assembly instructions and produces two output files, one listing file that has the contents of the original input files but to each line is added its program address and the encoded instruction. In addition it also contains the symbol table. The second output file contains only the encoded instruction. The exact formats of these input/output files as well as example files can be found later in this document.

## Supported MIPS instructions

This assembler only recognizes the a subset of the MIPS instruction set. These instructions and their arguments are listed in the following table:

| instruction | expected arguments |      |
| :---------: | :----------------: | ---- |
|     add     |     rd, rs, rt     |      |
|     sub     |     rd, rs, rt     |      |
|     and     |     rd, rs, rt     |      |
|     or      |     rd, rs, rt     |      |
|     nor     |     rd, rs, rt     |      |
|     slt     |     rd, rs, rt     |      |
|     lw      |   rt, immediate    |      |
|     sw      |   rt, immediate    |      |
|     beq     |  rs, rt, address   |      |
|    addi     | rt, rs, immediate  |      |
|     sll     |     rd, rt, sa     |      |
|      j      |      address       |      |
|     jr      |         rs         |      |
|     nop     |                    |      |



## Compiling

The compiling in terminal can be done with the following command

```bash
$	javac pathToSourceFilesDirectory/*.java -d pathForCompiledFiles
```

replace `pathToSourceFilesDirectory` with the actual path to the directory containing the project `.java` files, if the current working directory is where the source files are located leave only the `*.java`. Similarly replace the `pathForCompiledFiles` with a path to an existing directory that shall revive the compiled files. the option`-d ` with the file path can be omitted altogether, in that case the compiled files are placed in the same directory the source files are located in.  

## Usage

After having compiled the program, it can be executed by running one of the following commands in terminal from the directory containing the compiled program

1. ```bash
   $	java Main
   ```

   - The program will search for the input file with the default name `assembly.in` in the same directory it is running from, and output two files `listing.out` and `instructions.out`.

     

2. ```bash
   $	java Main inputFilePath
   ```

   - The program will search for the input file at the path specified. The two output file paths are the same as in the first case.

     

3. ```bash
   $	java Main pathToInputFile pathToOutputListingFile pathToOutputInstructionFile
   ```

   - All the i/o file paths are specified in the arguments to the program

All the file paths must already exist. The program cannot create new directories, only new files.

### Input file

The assembler recognizes three types of elements in the output file. On each line of the file can be a label. If present it needs to be at the first position on the line. A label can be composed of any alphanumerical character `a-Z 0-9` , dot  `.` , or underscore `__`, except it's first symbol which cannot be a numerical. The label must always end with a colon `:` , which is actually not part of the label itself. The following element that can be on a line of the input file is an instruction. It always have to be preceded by a whitespace and composes of the instruction name followed by another space and than the corresponding instruction arguments separated by commas. The last element on the line is a comment preceded by a hash-tag symbol `#`. 

A line of the input file can contain any combination of the three elements listed above provided that their respective positions are respected. A line can also be empty.

The arguments of an instruction can be either a register (represented by its name), a label, or a literal value (i.e. shift amount, address).

#### Example input file

```assembly
label1:
		nor $t1, $zero, $zero
        sub $t1, $zero, $t1	# This line has an instruction and a comment
label2:	add $t2, $t1, $t1	# This line has a label, instruction and a comment

# Here is just a comment
label2:
		add $t3, $t2, $t1
label3: # Label and a comment
```



### Output files

The program outputs two files: a listing file and an encoded instruction file.

The listing file is composed of two parts. The first part having one line for each line of the input file. Lines that did not contain any instruction in the input file, stay the same. If the line contains an instruction (possibly preceded by a label and/or followed by a comment) there will be two more elements preceding the original contents of that line, namely the address of the instruction and the encoded instruction itself, both in 32 bit hexadecimal format. If an error occurred while trying to parse the instruction (i.e. the instruction is was not recognized, argument missing ...)  the line will contain the respective error message in place of the encoded instruction. The second part of the listing file (after "symbols") contains the symbol table, that is, the list of labels and their corresponding instruction addresses, on each line one label and its address.

As the name suggests, the encoded instruction file only contains the encoded instructions in 32 bit hexadecimal form, on each line one instruction in order as they appear in the input.

#### Example output files

- Listing file:

```assembly
                        # This is an example
                        label0:
0x00000000  0x20090001  		addi  $t1, $zero, 1   # A comment
0x00000004  0x200a0002  label1: addi  $t2, $zero, 2
0x00000008  0x200b0003          addi  $t3, $zero, 3
0x0000000c  0x200cfffc          addi  $t4, $zero, -4

Symbols
label0   0x00000000
label1   0x00000004
```

- Instruction file:

```
0x20090001
0x200a0002
0x200b0003
0x0000000c
```

