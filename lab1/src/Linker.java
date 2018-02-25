import java.io.*;
import java.util.*;

public class Linker {

    private String filePath;
    private ArrayList<Integer> moduleAddress;
    public Map<String, Variable> variableMap;//save all variables
    public Map<String, Variable> variableUsedMap;//record the used variables
    private Map<String, Userr> userMap;// record the user's index in which module
    private ArrayList<Userr> userList;//check if userlist has symbol which is not used in program text

    private ArrayList<String> ErrList;
    int cur_base_address;


    public Linker() {

        this.filePath = "/Users/Eddy/nyu/os/lab1/input.txt";
        this.moduleAddress = new ArrayList<>();
        this.cur_base_address = 0;
        this.variableMap = new HashMap<>();
        this.variableUsedMap = new HashMap<>();
        userList = new ArrayList<>();
        userMap = new HashMap<>();
        ErrList = new ArrayList<>();
        // userIsNotUsedButDefined = false;

    }


    public void firstPass() {

        try {
            File file = new File(filePath);
            Scanner sc = new Scanner(file);
            moduleAddress.add(cur_base_address);
            System.out.println("Symbol Table: \n");

            int definition_address;

            String variable = null;

            while (sc.hasNextInt()) {


                //read the first line of a module group
                int groupNum = sc.nextInt();
                for (int i = 0; i < groupNum; i++) {


                    //read the definition line in each module
                    definition_address = 0;
                    int variable_defined_count = sc.nextInt();
                    if (variable_defined_count != 0) {
                        for (int j = 1; j <= variable_defined_count; j++) {

                            String variable_definition = sc.next();
                            variable = variable_definition;
                            Variable v = new Variable();
                            v.name = variable_definition;
                            int offset = sc.nextInt();
                            definition_address = offset;


                            if (variableMap.containsKey(variable_definition)) {
                                //error type 1
                                v.address = variableMap.get(variable_definition).address;
                                v.module_index = variableMap.get(variable_definition).module_index;
                                System.out.println("Error: This variable is multiply defined; first value used");
                            } else {
                                v.module_index = i;
                                v.address = cur_base_address + offset;
                                //System.out.println(v.name + "=" + v.address);
                            }
                            variableMap.put(variable_definition, v);
                        }
                    }


                    //read the user list in each module
                    int user_count = sc.nextInt();
                    for (int a = 1; a <= user_count; a++) {
                        String user = sc.next();
                    }


                    //read the program text in each module

                    int index = sc.nextInt();
                    int moduleSize = index;

                    if (definition_address > moduleSize) {
                        ErrList.add("Error: In module " + i + " the def of " + variable + "  exceeds the module size; zero(relative) used.");
                        Variable v = new Variable();
                        v.address = cur_base_address;
                        v.module_index = i;
                        v.name = variable;
                        variableMap.put(variable, v);
                    }


                    for (int k = 1; k <= index; k++) {

                        String type = sc.next();
                        String a = sc.next();
                        Code code = new Code();
                        code.opcode = a.substring(0, 1);
                        code.type = type;
                        code.address = a.substring(1, 4);


                    }

                    cur_base_address = cur_base_address + moduleSize;
                    moduleAddress.add(cur_base_address);
                }
            }


        } catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException+" + e.getMessage());
        }


    }

    public void secondPass() {

        try {
            File file = new File(filePath);
            Scanner sc = new Scanner(file);

            int groupNum, count;
            int line = 0;
            String symbol;
            int index = 0;//pointer to the userlist symbol
            int offset = 0;
            int moduleSize = 0;
            int absAddress = 0; // offset + current_module_base_address
            int[] variabUseList = new int[10];
            String[] systemErr = new String[10];
            String var = null;
            printVariable();

            System.out.println("\nMemory Map:");

            while (sc.hasNext()) {

                //group num
                groupNum = sc.nextInt();

                for (int i = 0; i < groupNum; i++) {
                    ArrayList<Userr> tempUserList = new ArrayList<>();
                    //definition line
                    count = sc.nextInt();
                    for (int j = 1; j <= count; j++) {
                        symbol = sc.next();
                        var = symbol;
                        offset = sc.nextInt();
                        //absAddress = offset + moduleAddress

                    }

                    // Use list.
                    count = sc.nextInt();
                    while (count > 0) {
                        symbol = sc.next(); // Symbol that appears in use list.


                        Userr user = new Userr();
                        user.name = symbol;
                        user.isUsed = false;
                        user.moduleIndex = i;


                        if (variableMap.get(symbol) != null) // If symbol was unused, remove it and put it in SymbolsUsed.
                        {
                            Variable v = variableMap.get(symbol);
                            //int def = v.address; // Get the symbol's value.
                            variableMap.remove(symbol); // Remove symbol from unused list.
                            variableUsedMap.put(symbol, v); // Add symbol to used list.
                            variabUseList[index] = v.address; // Add value to array.
                        }
                        if (variableUsedMap.get(symbol) == null) // Check if symbol is defined.
                        // a symbol can't be used if it hasn't been declared.
                        {
                            systemErr[index] = "Error: " + symbol + " is not defined; zero used.";
                            Variable v = new Variable();
                            v.name = symbol;
                            v.address = 0;
                            v.module_index = groupNum;
                            variableUsedMap.put(symbol, v); // Add new symbol to the list of used symbols with val 0.


                        } else {
                            variabUseList[index] = variableUsedMap.get(symbol).address;
                            userMap.put(symbol, user);
                            tempUserList.add(user);

                            //systemErr[index] = ""; // Note that there is no error.
                        }


                        index++;
                        count--;
                    }

                    // Program text.
                    count = sc.nextInt();
                    moduleSize = count;
                    while (count > 0) {


                        Code code = new Code();
                        code.type = sc.next();
                        code.tempAdd = sc.next();

                        int temp = Integer.parseInt(code.tempAdd);


                        if (code.type.equals("A") || code.type.equals("I")) {
                            if (code.type.equals("A") && (temp % 1000) > 200) {
                                System.out.print(line + ": " + (temp - (temp % 1000)));
                                System.out.println("Error: Absolute address exceeds machine size; zero used.");
                            } else {
                                System.out.println(line + ": " + temp);
                            }
                        }
                        if (code.type.equals("E")) {
                            if (temp % 1000 < 10) {
                                absAddress = variabUseList[temp % 1000] + (temp / 1000) * 1000; // External addr = Symbol def +  addr.
                                int pos = temp % 1000;
                                tempUserList.get(pos).isUsed = true;
                            }

                            if ((temp % 1000) >= index) {
                                System.out.println(line + ": " + temp);
                                System.out.println("Error: External address exceeds length of use list; treated as immediate.");
                            } else {

                                System.out.println(line + ": " + absAddress);
                                if (systemErr[temp % 1000] == null) {
                                    // Do nothing.
                                } else {
                                    System.out.println(systemErr[temp % 1000]);
                                }
                            }
                        }
                        if (code.type.equals("R")) {
                            if (((temp % 1000) > moduleAddress.get(i + 1) - moduleAddress.get(i))) {
                                System.out.print(line + ": " + (temp - (temp % 1000)));
                                System.out.println("Error: Relative address exceeds module size; zero used.");
                            } else {
                                absAddress = moduleAddress.get(i) + temp; // Relative addr = baseAddr[moduleCount] + addr.
                                System.out.println(line + ": " + absAddress);
                            }
                        }

                        line++;
                        count--;
                    }


                    //line = 0;
                    index = 0;
                    for (Userr u : tempUserList) {
                        userList.add(u);
                    }
                    System.out.println();
                }
                checkUnusedSymbols();
                printError();
            }

        } catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException: " + e.getMessage());
        }

    }


    private void checkUnusedSymbols() {
        for (String s : variableMap.keySet()) {
            System.out.println("\nWarning: " + s + " was defined in module " + (variableMap.get(s).module_index) + " but never used.");
        }
        for (Userr u : userList) {
            if (u.isUsed == false)
                System.out.println("Warning: In module " + u.moduleIndex + " " + u.name + " appeared in the use list but not actually used.");
        }

    }

    public void printVariable() {
        for (String key : variableMap.keySet()) {
            System.out.println(key + "=" + variableMap.get(key).address);
        }

    }

    public void printError() {
        for (String s : ErrList) {
            System.out.println(s);
        }
    }


    public static void main(String[] args) {
        Linker linker = new Linker();
        linker.firstPass();
        linker.secondPass();
    }


}