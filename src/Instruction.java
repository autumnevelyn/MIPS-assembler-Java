import java.util.ArrayList;
import java.util.HashMap;

/**
 * Structure holding all the information about an instruction, as well as static information about the MIPS isa.
 */
public class Instruction {
	private String name;
	private ArrayList<Field> fields;
	private Integer address;

    private static HashMap<String,Integer> funcCodes; // The list of function codes of special instructions recognized by the assembler
    static {
        funcCodes = new HashMap<>();
        funcCodes.put("add",32);
        funcCodes.put("sub",34);
        funcCodes.put("and",36);
        funcCodes.put("or",37);
        funcCodes.put("nor",39);
        funcCodes.put("slt",42);
        funcCodes.put("sll",0);
        funcCodes.put("jr",8);
        funcCodes.put("nop",0);
    }
    private static HashMap<String,Integer> opCodes; // The list of opcodes of the instructions recognized by the assembler
    static {
        opCodes = new HashMap<>();
        opCodes.put("add",0);
        opCodes.put("sub",0);
        opCodes.put("and",0);
        opCodes.put("or",0);
        opCodes.put("nor",0);
        opCodes.put("slt",0);
        opCodes.put("sll",0);
        opCodes.put("jr",0);
        opCodes.put("lw",35); // signed offset
        opCodes.put("sw",43); // signed offset
        opCodes.put("beq",4); // signed offset calculated from an immediate address, and shifted to the right by 2 bits (divided by four = offset in no of instructions)
        opCodes.put("addi",8); // signed immediate
        opCodes.put("j",2); // address in argument must be shifted to the right by 2 bits (divided by four = instruction count)
        opCodes.put("nop",0); //Stands for sll r0,r0,0, meaning: Logically shift register 0 zero bits to the left and store the result in register 0
    }
    private static ArrayList<String> registers; // List of all existing registers of the MIPS isa. The register number corresponds to the index of its name in this list
    static {
        registers = new ArrayList<>();
        registers.add("$zero"); // 0
        registers.add("$at"); // 1
        registers.add("$v0"); // 2
        registers.add("$v1"); // 3
        registers.add("$a0"); // 4
        registers.add("$a1"); // 5
        registers.add("$a2"); // 6
        registers.add("$a3"); // 7
        registers.add("$t0"); // 8
        registers.add("$t1"); // 9
        registers.add("$t2"); // 10
        registers.add("$t3"); // 11
        registers.add("$t4"); // 12
        registers.add("$t5"); // 13
        registers.add("$t6"); // 14
        registers.add("$t7"); // 15
        registers.add("$s0"); // 16
        registers.add("$s1"); // 17
        registers.add("$s2"); // 18
        registers.add("$s3"); // 19
        registers.add("$s4"); // 20
        registers.add("$s5"); // 21
        registers.add("$s6"); // 22
        registers.add("$s7"); // 23
        registers.add("$t8"); // 24
        registers.add("$t9"); // 25
        registers.add("$k0"); // 26
        registers.add("$k1"); // 27
        registers.add("$gp"); // 28
        registers.add("$sp"); // 29
        registers.add("$fp"); // 30
        registers.add("$ra"); // 31
    }

    /**
     * Instruction constructor. Initializes the instruction according to the argument list passed as parameter.
     * @param rawFields An Arraylist containing the parsed fields of an instruction.
     *                  The instruction name is expected on index 0. Indexes 1-3 are optional instruction arguments.
     *                  The number and order of arguments must be in accordance with the MIPS assembly syntax.
     * @throws InstructionFormatException Signals that the number or order of arguments does not correspond to the ones expected for the instruction in question.
     * @throws NumberFormatException Signals that an immediate value field in the arguments for the instruction is  not a decimal or hexadecimal number.
     * @throws UnknownInstructionException The instruction specified at index 0 of the argument list is not known by the assembler.
     * @throws UnknownRegisterException One of the registers specified in the argument list is not known by the assembler.
     */
    public Instruction(Integer address, ArrayList<String> rawFields) throws InstructionFormatException, NumberFormatException, UnknownInstructionException, UnknownRegisterException {
        try{
            this.address = address;
            this.name = rawFields.get(0);
            this.fields = new ArrayList<>();
            assignFields(this.name, rawFields);

        }catch(IndexOutOfBoundsException e) {
            // the instruction format is not correct (too many or too little arguments)
            throw new InstructionFormatException("Incorrect instruction format: Instruction has too few arguments");

        }catch (NumberFormatException e){
            // incorrect value in the immediate field
            throw new NumberFormatException("Immediate field value is not a decimal or hexadecimal");

        }
    }

    // called by the constructor to initialize the appropriate number of arguments for the particular instruction with the correct values
    private void assignFields(String name,ArrayList<String> rawFields) throws UnknownRegisterException,UnknownInstructionException,InstructionFormatException{
        // assign the opcode according to the instruction name, if not know op=null
        this.fields.add(new Field("op", opCodes.get(name)));
        // assign the remaining fields according to the particular instruction format
        switch(name) {
            // R-format
            case "add":
            case "sub":
            case "and":
            case "or":
            case "slt":
            case "nor":
                this.fields.add(new Field("rs",registers.indexOf(rawFields.get(2))));
                this.fields.add(new Field("rt",registers.indexOf(rawFields.get(3))));
                this.fields.add(new Field("rd",registers.indexOf(rawFields.get(1))));
                this.fields.add(new Field("sa",0));
                this.fields.add(new Field("func",funcCodes.get(name)));
                break;
            case "sll":
                this.fields.add(new Field("rs",0));
                this.fields.add(new Field("rt",registers.indexOf(rawFields.get(2))));
                this.fields.add(new Field("rd",registers.indexOf(rawFields.get(1))));
                this.fields.add(new Field("sa",Integer.decode(rawFields.get(3))));
                this.fields.add(new Field("func",funcCodes.get(name)));
                break;
            case "jr":
                this.fields.add(new Field("rs",registers.indexOf(rawFields.get(1))));
                this.fields.add(new Field("rt",0));
                this.fields.add(new Field("rd",0));
                this.fields.add(new Field("sa",0));
                this.fields.add(new Field("func",funcCodes.get(name)));
                break;
            case "nop":
                this.fields.add(new Field("rs",0));
                this.fields.add(new Field("rt",0));
                this.fields.add(new Field("rd",0));
                this.fields.add(new Field("sa",0));
                this.fields.add(new Field("func",funcCodes.get(name)));
                break;

            // I-format
            case "lw":
            case "sw":
                this.fields.add(new Field("rs",registers.indexOf(rawFields.get(3))));
                this.fields.add(new Field("rt",registers.indexOf(rawFields.get(1))));
                this.fields.add(new Field("immediate",Integer.decode(rawFields.get(2)))); // signed offset
                break;
            case "beq":
                this.fields.add(new Field("rs",registers.indexOf(rawFields.get(1))));
                this.fields.add(new Field("rt",registers.indexOf(rawFields.get(2))));
                int offset = Integer.decode(rawFields.get(3)) - (address + 4);
                this.fields.add(new Field("immediate",offset>>2)); // signed offset calculated from an immediate address, and shifted to the right by 2 bits (divided by four = offset in no of instructions)
                break;
            case "addi":
                this.fields.add(new Field("rs",registers.indexOf(rawFields.get(2))));
                this.fields.add(new Field("rt",registers.indexOf(rawFields.get(1))));
                this.fields.add(new Field("immediate",Integer.decode(rawFields.get(3)))); // signed immediate
                break;

            // J-format
            case "j":
                this.fields.add(new Field("address",Integer.parseInt(rawFields.get(1))>>2)); // address in argument must be shifted to the right by 2 bits (divided by four = instruction count)
                break;
        }

        // EXCEPTIONS
        // op = null or func = null --> unknown instruction (UnknownInstructionException)
        // register field value = -1 --> unknown register (UnknownRegisterException)
        // |rawFields|>|fields| --> too many arguments (IndexOutOfBound)
        // exception raised by .get() --> too little arguments (IndexOutOfBound)
        // exception raised by .decode() --> wrong immediate field value (NumberFormatException)
        for (Field field: this.fields) {
            switch (field.getName()){
                case "op":
                case "func":
                    if(field.getValue() == null){
                        throw new UnknownInstructionException("Unknown instruction");
                    }
                case "rs":
                case "rd":
                case "rt":
                    if(field.getValue() == -1){
                        throw new UnknownRegisterException("Unknown register");
                    }
            }
        }
        if(rawFields.size()>fields.size()){
            throw new InstructionFormatException("Incorrect instruction format: Instruction has too many arguments");
        }
    }

    /**
     * Method used for encoding the instruction into an integer, that when written in binary corresponds to how the MIPS processor receives the instruction
     * @return The instruction encoded into an integer
     */
    public int toInt(){
        Field current;
        int position = 0;
        int result = 0;
        int bitmask;
        // go through all the instruction fields in reversed order
        for(int i = this.fields.size()-1; i >= 0;i--){
            current = this.fields.get(i);
            bitmask = (int)Math.pow(2,current.getSize())-1;
            if(current.getName().equals("immediate") && !((current.getValue()<Math.pow(2,current.getSize()-1)) && (current.getValue()>=(-1)*Math.pow(2,current.getSize()-1)))){ // the value of the field is not overflowing
                throw new RuntimeException("Immediate or address field overflow"); // TODO: handle exception
            }else{
                result = result | ((bitmask & current.getValue()) << position); // add the current value masked to the required size of the field, shifted left by as many bits as the current bit size of the result string (position) to the result with a bitwise OR
                position += current.getSize();
            }

        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Field field : this.fields) {
            result.append(field.getName()).append(":").append(field.getValue()).append(" ");
        }
        return result.toString();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Field> getFields() {
        return fields;
    }

    //    Not required anymore, the correct hexadecimal representation is obtained with formatted strings
//    public String intTo8byteHexString(Integer i){
//        StringBuilder hexString = new StringBuilder(Integer.toHexString(i));
//        while(hexString.length() < 8){
//            hexString.insert(0, "0");
//        }
//        if(hexString.length()>8){
//            throw new RuntimeException("Integer hexadecimal representation overflow");
//        }else{
//            hexString.insert(0, "0x");
//            return hexString.toString();
//        }
//    }

}


