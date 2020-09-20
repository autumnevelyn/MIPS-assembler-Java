import java.io.BufferedReader;
import java.io.Closeable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Parser implements Closeable {

    private String line;
    private int lineCnt;

    private Path filePath;
    private BufferedReader reader;

    // Matchers for the regex patterns
    private boolean parseIsValid;
    private Matcher allTokensMatcher;
    private boolean allTokensMatched;
//    private Matcher lineLevelMatcher;
//    private boolean lineLevelMatched;

    public enum TokenType {
        LABEL_DEF("label"),
        INSTRUCTION("instruction"),
        OPERATION("operation"),
        ARGUMENT1("argument1"),
        ARGUMENT2("argument2"),
        ARGUMENT3("argument3"),
        COMMENT("comment");

        final String tag;

        TokenType(String tag) {
            this.tag = tag;
        }
    }

    private static final String labelRE = "[\\w.&&[\\D]][\\w.]*";

    private static final String labelDefRE = "(?:^(?<" + TokenType.LABEL_DEF.tag + ">" + labelRE + ")\\s*:)?";

    private static final String operationRE = "(?:(?<=\\s)(?<" + TokenType.OPERATION.tag + ">[\\w&&[^_]][\\w&&[^_]]*)(?=[\\s]|$))";

    private static final String registerByIndexRE = "[12]\\d?|3[01]?|[04-9]";
    private static final String registerByNameRE = "a[t0-3]|v[01]|t\\d|s[p0-7]|k[01]|gp|fp|ra|zero";
    private static final String registerRE = "\\$(?:" + registerByIndexRE + "|" + registerByNameRE + ")";

    private static final String literalRE = "(?:0(?:x[\\da-fA-F]+|[0-7]+)?|-?[1-9]\\d*)";

    private static final String registerWithOffsetRE = "(?<offset>" + literalRE + ")" + "\\((?<register>" + registerRE + ")\\)";

    //private static final String argumentRE = "(?:" + registerRE + "|" + labelRE + "|" + literalRE + "(?:\\(" + registerRE + "\\))?)";
    private static final String argumentRE = "[^\\s,#]+";

    private static final String argumentsListRE = "(?<" + TokenType.ARGUMENT1.tag + ">" + argumentRE + ")"
                               + "(?:\\s*,\\s*" + "(?<" + TokenType.ARGUMENT2.tag + ">" + argumentRE + ")"
                               + "(?:\\s*,\\s*" + "(?<" + TokenType.ARGUMENT3.tag + ">" + argumentRE + ")" + ")?)?";

    private static final String instructionRE = "(?:" + operationRE
                                              + "(?:\\s*" + argumentsListRE + ")?)?";
    //private static final String unparsedInstructionRE = "(?<" + TokenType.INSTRUCTION.tag + ">[^\\s](?:[^#][^\\s])?)";

    private static final String commentRE = "(?:(?<" + TokenType.COMMENT.tag + ">#.*[^\\s])\\s*$)?";

    private static final String completeLineRE = labelDefRE + "\\s*" + instructionRE + "\\s*" + commentRE;
    //private static final String lineLevelRE = labelDefRE + "\\s*" + unparsedInstructionRE + "\\s*" + commentRE;


    public Parser(String filePath) throws IOException {
        line = null;
        lineCnt = 0;

        this.filePath = Paths.get( filePath );
        reader = Files.newBufferedReader( this.filePath );

        parseIsValid = false;
        allTokensMatcher = Pattern.compile( completeLineRE ).matcher("");
        allTokensMatched = false;
        /*lineLevelMatcher = Pattern.compile( lineLevelRE ).matcher("");
        lineLevelMatched = false;*/
    }

    public void reset() throws IOException {
        line = null;
        lineCnt = 0;

        reader.close();
        reader = Files.newBufferedReader(filePath);

        parseIsValid = false;
        allTokensMatcher.reset("");
        allTokensMatched = false;
        /*lineLevelMatcher.reset("");
        lineLevelMatched = false;*/
    }

    public boolean parseAllTokens() throws IOException {
        if( (line = reader.readLine()) != null ) {
            lineCnt++;

            line = line.replace( "\t", String.join("", Collections.nCopies(4, " ")) );
            allTokensMatcher.reset(line);

            /*// Reset other matchers renders invalid
            lineLevelMatcher.reset("");
            lineLevelMatched = false;*/

            parseIsValid = allTokensMatched = allTokensMatcher.matches();

            return true;
        }

        return false;
    }

    /*public boolean parseLine() throws IOException {
        if( (line = reader.readLine()) != null ) {
            lineCnt++;

            line = line.replace( "\t", String.join("", Collections.nCopies(4, " ")) );
            lineLevelMatcher.reset(line);

            // Reset other matchers rendered obsolete
            allTokensMatcher.reset();
            allTokensMatched = false;

            parseIsValid = allTokensMatched = lineLevelMatcher.matches();

            return true;
        }
        return false;
    }*/

    /*public boolean parseInstruction() {
        return true;
    }*/

    public String getLine() {
        return line;
    }

    public int getLineNumber() {
        return lineCnt;
    }

    public boolean parseIsValid() {
        return parseIsValid;
    }

    /*public String getToken(TokenType tt) {
        return allTokensMatcher.group(tt.tag);
    }*/

    public String getLabel() {
        /*if(allTokensMatched && !lineLevelMatched)
            return allTokensMatcher.group(TokenType.LABEL_DEF.tag);
        else if(lineLevelMatched && !allTokensMatched)
            return lineLevelMatcher.group(TokenType.LABEL_DEF.tag);
        else {
            if(allTokensMatched && lineLevelMatched){
                System.out.println("ERROR: getLabel in Parser (both allTokensMatched and lineLevelMatched true at the same time");
            }
            return null;
        }*/

        return allTokensMatched ? allTokensMatcher.group(TokenType.LABEL_DEF.tag) : null;
    }

    /*public String getInstruction() {
        return lineLevelMatched ? lineLevelMatcher.group(TokenType.INSTRUCTION.tag) : null;
    }*/

    public String getOperation() {
        return allTokensMatched ? allTokensMatcher.group(TokenType.OPERATION.tag) : null;
    }

    public String getArgument1() {
        return allTokensMatched ? allTokensMatcher.group(TokenType.ARGUMENT1.tag) : null;
    }

    public String getArgument2() {
        return allTokensMatched ? allTokensMatcher.group(TokenType.ARGUMENT2.tag) : null;
    }

    public String getArgument3() {
        return allTokensMatched ? allTokensMatcher.group(TokenType.ARGUMENT3.tag) : null;
    }

    public ArrayList<String> getArgumentsList() {
        ArrayList<String> args = new ArrayList<>();
        String arg;
        if( (arg = getArgument1()) != null ) {
            args.add(arg);
            if( (arg = getArgument2()) != null ) {
                args.add(arg);
                if( (arg = getArgument3()) != null ) {
                    args.add(arg);
                }
            }
        }
        return args;
    }

    public ArrayList<String> getInstructionAsList(Map<String, Symbol> symbolTable) throws UndefinedSymbolException {
        ArrayList<String> instruction = new ArrayList<>();
        String op;
        if( (op = getOperation()) != null ) {
            instruction.add(op);
            ArrayList<String> args = getArgumentsList();

            for (String arg : args) {
                Matcher offsetRegisterMatcher = Pattern.compile(registerWithOffsetRE).matcher(arg);
                Matcher labelMatcher = Pattern.compile("^[^$\\-\\d]").matcher(arg);
                // Handle arguments in the format <offset>($<register>)
                if( offsetRegisterMatcher.matches() ) {
                    instruction.add( offsetRegisterMatcher.group("offset") );
                    instruction.add( offsetRegisterMatcher.group("register") );
                }
                // Handle label address resolution
                else if( labelMatcher.find() ) {
                    Symbol s = symbolTable.get(arg);
                    if( s != null )
                        instruction.add( Integer.toString( s.getAddress() ) );
                    else
                        throw new UndefinedSymbolException("Symbol \"" + arg + "\" is not defined");
                }
                else {
                    instruction.add(arg);
                }
            }
        }
        return instruction;
    }

    public String getComment() {
        /*if(allTokensMatched && !lineLevelMatched)
            return allTokensMatcher.group(TokenType.COMMENT.tag);
        else if(lineLevelMatched && !allTokensMatched)
            return lineLevelMatcher.group(TokenType.COMMENT.tag);
        else {
            if(allTokensMatched && lineLevelMatched){
                System.out.println("ERROR: getComment in Parser (both allTokensMatched and lineLevelMatched true at the same time");
            }
            return null;
        }*/
        return allTokensMatched ? allTokensMatcher.group(TokenType.COMMENT.tag) : null;
    }

    @Override
    public void close() throws IOException {
        try {
            reader.close();
        } finally {
            reader = null;
        }
    }
}
