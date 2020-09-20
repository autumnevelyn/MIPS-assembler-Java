import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.*;
import java.util.*;


public class MIPSAssembler implements Closeable {

    File file;
    private Parser parser;

    private HashMap<String, Symbol> symbolTable;

    private Writer listFileWriter;
    private Writer instructionFileWriter;

    public MIPSAssembler(String inputFilePath,String outputListingFilePath, String outputInstructionFilePath) throws IOException {
        file = new File(inputFilePath);
        parser = new Parser(inputFilePath);
        symbolTable = new HashMap<>();
        File listingFile = new File(outputListingFilePath);
        File instructionFile = new File(outputInstructionFilePath);
        this.listFileWriter = new BufferedWriter(new FileWriter(listingFile));
        this.instructionFileWriter = new BufferedWriter((new FileWriter(instructionFile)));
    }

    /**
     * Main assembler function. Produces the output and list files by parsing the input file and encoding the
     * instructions.
     * Execute two passes on the input file: the first one to build the symbol table, and the second one to actually
     * parse the instruction statements.
     * @return true if the assembly was a success, false otherwise.
     * @throws IOException
     */
    public boolean assemble() throws IOException {
        boolean ret = true;

        if( ret = buildSymbolTable() ) {
            try {
                int address = -4;
                Instruction instruction;

                while (parser.parseAllTokens()) {
                    instruction = null;

                    if (parser.parseIsValid()) {

                        if (parser.getOperation() != null) {
                            address += 4;
                            ArrayList<String> args = parser.getInstructionAsList(symbolTable);
                            instruction = new Instruction(address,args);
                        }
                        outputCurrentLine(address, instruction, null);

                    } else {
                        throw new InvalidLineException("Line not valid \"" + parser.getLine() + "\"");
                    }
                }
                outputSymbolTable();

            } catch (InvalidLineException | UnknownInstructionException | UnknownRegisterException | InstructionFormatException | UndefinedSymbolException | NumberFormatException e) {
                outputCurrentLine(null, null, e.getMessage());
                return false;
            }
        }

        return ret;
    }

    private boolean buildSymbolTable() throws IOException {
        boolean ret = true;

        try {
            int address = -4;
            HashMap<String, Symbol> symbolsWaitList = new HashMap<>();

            while( parser.parseAllTokens() ) {
                if (parser.parseIsValid()) {

                    String token;
                    if ((token = parser.getLabel()) != null) {
                        Symbol duplicate;
                        if ((duplicate = symbolTable.get(token)) != null)
                            throw new SymbolAlreadyDefinedException( "Symbol \"" + token + "\" on line " + parser.getLineNumber() + " is already defined on line " + duplicate.getLine() );
                        else if ((duplicate = symbolsWaitList.get(token)) != null) {
                            throw new SymbolAlreadyDefinedException( "Symbol \"" + token + "\" on line " + parser.getLineNumber() + " is already defined on line " + duplicate.getLine() );
                        } else {
                            symbolsWaitList.put(token, new Symbol(token, -1, parser.getLineNumber()));
                        }
                    }

                    if (parser.getOperation() != null) {
                        address += 4;

                        for (Symbol s : symbolsWaitList.values())
                            s.setAddress(address);

                        symbolTable.putAll(symbolsWaitList);
                        symbolsWaitList.clear();
                    }
                }
            }
        } catch (SymbolAlreadyDefinedException e) {
            outputCurrentLine(null, null, e.getMessage());
            ret = false;
        }

        parser.reset();

        return ret;
    }

    /**
     * Writes a line corresponding to the current line parsed into the listing file.
     * If an instruction is on the current line, its 32 bit hexadecimal encoding is written in the instruction output file.
     * @param address The address of the current instruction parsed.
     * @param instruction Instruction object corresponding to the instruction on the current line.
     *                    If non is present (i.e. comment line) then expecting null
     * @param errorMessage If an exception is caught, an error is passed to the method to be printed in the listing file. Null expected if all went nominally.
     * @throws IOException Exception raised by the writer
     */
    private void outputCurrentLine(Integer address,Instruction instruction, String errorMessage) throws IOException {
        String listLine = "";
        if(errorMessage == null) {
            if (instruction != null) { // there is an instruction at this line, and we want to print the address and instruction hexadecimal at the beginning of the listing file line
                try {
                    listLine = String.format("%#010x  %#010x",
                            address,
                            instruction.toInt()
                    );

                    this.instructionFileWriter.write(String.format("%#010x%n", instruction.toInt())); // OUTPUT: the instruction 32 bit hexadecimal encoding is written in the instruction output file
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }else{
            listLine = errorMessage;
        }
        // all arguments present are collected
        String arguments = String.format("%s%s%s",
                this.parser.getArgument1() == null ? "" : this.parser.getArgument1(),
                this.parser.getArgument2() == null ? "" : ", "+this.parser.getArgument2(),
                this.parser.getArgument3() == null ? "" : ", "+this.parser.getArgument3()
        );

        // The line of the listing file is finally built with the previously acquired address and instruction encoding (empty space left if not present), and arguments
        listLine = String.format("%-22s  %10s  %-3s  %-15s  %s%n",
                listLine,
                this.parser.getLabel() == null ? "" : this.parser.getLabel() + ":",
                this.parser.getOperation() == null ? "" : this.parser.getOperation(),
                arguments,
                this.parser.getComment() == null ? "" : this.parser.getComment()
        );

        this.listFileWriter.write(listLine); // The line is written in the listing output file

    }

    private void outputSymbolTable() throws IOException {
        TreeMap<String, Symbol> sortedSymbolTable = new TreeMap<>(symbolTable);

        this.listFileWriter.write(String.format("%n%n%s%n","Symbols:"));
        for (Map.Entry<String, Symbol> s : sortedSymbolTable.entrySet()) {
            this.listFileWriter.write(String.format("%-10s\t%#010X%n", s.getKey(), s.getValue().getAddress()));
        }
    }

    @Override
    public void close() throws IOException {
        try {
            parser.close();
        } finally {
            parser = null;
        }
        try {
            instructionFileWriter.close();
        } finally {
            instructionFileWriter = null;
        }
        try {
            listFileWriter.close();
        } finally {
            listFileWriter = null;
        }
    }
}

class Symbol {
    private String name;
    private int address;
    private int line;

    Symbol(String name, int address, int line) {
        this.name = name;
        this.address = address;
        this.line = line;
    }

    public void setAddress(int address) { this.address = address; }

    public String getName() {return name; }
    public int getAddress() { return address; }
    public int getLine() { return line; }
}
