
public class Main {

    public static void main(String[] args) {
        // by default, if no arguments are given, these are the file paths of the i/o files:
        String inputFilePath = "assembly.in";
        String listingFilePath = "listing.out";
        String outputFilePath = "instructions.out";

        if(args.length == 1){ // if only one argument is passed to the program, it is considered to be the input file
            inputFilePath = args[0];
        }else if(args.length >= 3){ // if three or more arguments are passed, the first three are considered to be the input, listing, and output respectively, the rest is ignored
            inputFilePath = args[0];
            listingFilePath = args[1];
            outputFilePath = args[2];
        }

        try( MIPSAssembler asm = new MIPSAssembler(inputFilePath,listingFilePath,outputFilePath) ) {
            asm.assemble();
        } catch (Exception ignored) {

        }
    }

}
