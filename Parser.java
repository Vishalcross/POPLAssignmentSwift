/***********************************************************************************************************
*
* This is a program for a swift like parser which currently supports name, structural and internal name
* equivalence.
* NOTE:
* Implicitly, SWIFT does not support internal name equivalence
* Here we take an input from the file TestCases.txt and put the output into Output.txt
* For more information on the syntax refer to README.txt
* @authors
* Tanmay Kulkarni 2015B3A70647H
* Ch Vishal       2015B5A70605H
*
*************************************************************************************************************/
import java.util.*;
import java.io.*;
class typeException extends RuntimeException{
    public typeException(){
        super();
    }
    public typeException(String message){
        super(message);
    }
}
class DataType{
    final static int PRIMITIVE=0;
    final static int STRUCTURE=1;
    final static int FUNCTION=2;
    final static int ARRAY=3;
    final static int ERROR=-1;
    final static String PRIMITIVE_TOKEN="var";
    final static String STRUCTURE_TOKEN="struct";
    final static String FUNCTION_TOKEN="func";
    String name;
    int dataClass;
    DataType(String name,int dataClass){
        this.name=name;
        this.dataClass=dataClass;
    }

}
class Identifier{
    String name;
    DataType dataType;
    Identifier(String name,DataType dataType){
        this.name=name;
        this.dataType=dataType;
    }
    static int identify(String typeToken){
        switch(typeToken){
            case DataType.PRIMITIVE_TOKEN:return DataType.PRIMITIVE;
            case DataType.STRUCTURE_TOKEN:return DataType.STRUCTURE;
            case DataType.FUNCTION_TOKEN:return DataType.FUNCTION;
            default:return DataType.ERROR;
        }
    }
}
class ArrayProperties{
    String type;
    int numberOfDimensions;
    ArrayProperties(String type,int numberOfDimensions){
        this.type=type;
        this.numberOfDimensions=numberOfDimensions;
    }

    public String toString(){
        return type+" "+numberOfDimensions;
    }
}

public class Parser{
    static String input="";
    static String enclosingStruct="";
    static String enclosingFunc="";
    static HashMap<String,Identifier> identifierTable = new HashMap<>();
    static HashMap<String,DataType> typeTable = new HashMap<>();
    static HashMap<String,ArrayList<String>> nonPrimitiveMap = new HashMap<>();
    static HashMap<String,ArrayProperties> arrayMap = new HashMap<>();
    static HashMap<String,ArrayList<String>> returnMap = new HashMap<>();
    static HashSet<String> structures = new HashSet<>();
    static boolean insideStruct=false;
    static boolean insideFunc = false;
    static boolean insideReturn = false;
    static void initTypes(){
        typeTable.put("Int",new DataType("Int",DataType.PRIMITIVE));
        typeTable.put("Float",new DataType("Float",DataType.PRIMITIVE));
        typeTable.put("String",new DataType("String",DataType.PRIMITIVE));
        typeTable.put("Int8",new DataType("Int8",DataType.PRIMITIVE));
        typeTable.put("Int16",new DataType("Int16",DataType.PRIMITIVE));
        typeTable.put("UInt",new DataType("UInt",DataType.PRIMITIVE));
        typeTable.put("Double",new DataType("Double",DataType.PRIMITIVE));
    }
    static void checkArray(String identifierString,String arrayString){
        String typeString="";
        int count=0,maxCount=-1;
        for(int i=0;i<arrayString.length();i++){
            if(arrayString.charAt(i)=='['){
                count++;
                if(maxCount<count){
                    maxCount=count;
                }
                else if(count<=maxCount){
                    throw new typeException("Start closing brackets only when all open brackets have finished");
                }
            }
            else if(arrayString.charAt(i)==']'){
                count--;
                if(count<0){
                    throw new typeException("Bracket Mismatch");
                }
            }
            else{
                typeString+=arrayString.substring(i,i+1);
            }
        }
        if(count!=0){
            throw new typeException("Bracket Mismatch");
        }

        if(isPrimitiveType(typeString)==true){
            typeString+=" array:"+maxCount;

            ArrayProperties temp=new ArrayProperties(typeString,maxCount);
            arrayMap.put(identifierString,temp);
            DataType tempType=new DataType(typeString,DataType.ARRAY);
            if(insideStruct==false && insideFunc==false){
                addIdentifier(identifierString,tempType);
            }
            else if(insideFunc==true){
                if(insideReturn==true){
                    addReturnType(identifierString,tempType,enclosingFunc);
                }
                else{//Arguments
                    addIdentifier(identifierString,tempType,enclosingFunc);
                }
            }
            else if(insideStruct==true){
                addIdentifier(identifierString,tempType,enclosingStruct);
            }
        }
        else{
            throw new typeException("The data type is not defined for the array xxx"+typeString+"xxx");
        }

    }

    static String isArray(String arrayString){
         String typeString="";
        int count=0,maxCount=-1;
        for(int i=0;i<arrayString.length();i++){
            if(arrayString.charAt(i)=='['){
                count++;
                if(maxCount<count){
                    maxCount=count;
                }
                else if(count<=maxCount){
                    throw new typeException("Start closing brackets only when all open brackets have finished");
                }
            }
            else if(arrayString.charAt(i)==']'){
                count--;
                if(count<0){
                    throw new typeException("Bracket Mismatch");
                }
            }
            else{
                typeString+=arrayString.substring(i,i+1);
            }
        }
        if(count!=0){
            throw new typeException("Bracket Mismatch");
        }

        if(isPrimitiveType(typeString)==true){
            return typeString;
        }
        else{
            throw new typeException("The data type is not defined for the array");
        }
    }

    static void addIdentifier(String name,DataType type){
        if(insideStruct==true){ //Initialising the outermost struct
            nonPrimitiveMap.put(name,new ArrayList<String>());//Defining an empty arrayList for struct
            typeTable.put(name,type);
            structures.add(name);
        }
        else if(insideFunc==true){
            returnMap.put(name,new ArrayList<String>());//Empty arrayList for return values
            nonPrimitiveMap.put(name,new ArrayList<String>());//Defining an empty arrayList for func
        }
        identifierTable.put(name,new Identifier(name,type));
    }

    static void addIdentifier(String name,DataType type,String nonPrimitiveIdentifier){
        if(insideStruct==true || insideFunc==true){
            nonPrimitiveMap.get(nonPrimitiveIdentifier).add(type.name);
        }
    }

    static void addReturnType(String name,DataType type,String nonPrimitiveIdentifier){

        returnMap.get(nonPrimitiveIdentifier).add(type.name);
    }
    static void parsePrimitive(String[] parts){
        if(parts.length==3 && parts[1].charAt(parts[1].length()-1)==':'){ //name: dataType
            String name=parts[1].substring(0,parts[1].length()-1);
            if(isPrimitiveType(parts[2])==true){
                DataType temp=new DataType(parts[2],DataType.PRIMITIVE);
                if(insideStruct==false){
                    addIdentifier(name,temp);
                }
                else{
                    addIdentifier(name,temp,enclosingStruct);
                }
            }
            else if(parts[2].charAt(0)=='['){
                checkArray(name,parts[2]);
            }
            else{
                throw new typeException(parts[2]+" is not a valid type");
            }
        }
        else if(parts.length==4 && parts[2].charAt(0)==':' && parts[2].length()==1){//name : dataType
            String name=parts[1];
            if(isPrimitiveType(parts[3])==true){
                DataType temp=new DataType(parts[3],DataType.PRIMITIVE);
                if(insideStruct==false){
                    addIdentifier(name,temp);
                }
                else{
                    addIdentifier(name,temp,enclosingStruct);
                }
            }
            else if(parts[3].charAt(0)=='['){
                checkArray(name,parts[3]);
            }
            else{
                throw new typeException(parts[3]+" is not a valid type");
            }
        }
        else if(parts.length==3 && parts[2].charAt(0)==':' && parts[2].length()>1){//name :dataType
            String name=parts[1];
            String typeString=parts[2].substring(1,parts[2].length());
            if(isPrimitiveType(typeString)==true){
                DataType temp=new DataType(parts[2].substring(1,parts[2].length()),DataType.PRIMITIVE);
                if(insideStruct==false){
                    addIdentifier(name,temp);
                }
                else{
                    addIdentifier(name,temp,enclosingStruct);
                }
            }
            else if(typeString.charAt(0)=='['){
                checkArray(name,typeString);
            }
            else{
                throw new typeException(parts[2].substring(1,parts[2].length())+" is not a valid type");
            }
        }
        else if(parts.length==2 && parts[1].contains(":")){ //name:dataType
            String[] subParts = parts[1].split(":");
            String name=subParts[0];
            if(isPrimitiveType(subParts[1])==true){
                DataType temp=new DataType(subParts[1],DataType.PRIMITIVE);
                if(insideStruct==false){
                    addIdentifier(name,temp);
                }
                else{
                    addIdentifier(name,temp,enclosingStruct);
                }
            }
            else if(subParts[1].charAt(0)=='['){
                checkArray(name,subParts[1]);
            }
            else{
                throw new typeException(subParts[1]+" is not a valid type");
            }
        }
        else{
            throw new typeException("The synatx is incorrect for primitive type!");
        }
    }

    static void parseStruct(String[] parts){
        // This discards the input after the start brace on the same line
        if(insideStruct==true){
            throw new typeException("Structure definition is not defined within another structure definition");
        }
        if(insideFunc==true){
            throw new typeException("You cannot define a structure inside a function");
        }
        if(parts.length==2 && parts[1].charAt(parts[1].length()-1)=='{'){ //struct identifier{
            enclosingStruct=parts[1].substring(0,parts[1].length()-1);
            insideStruct=true;
            DataType temp=new DataType(enclosingStruct,DataType.STRUCTURE);
            addIdentifier(enclosingStruct,temp);
        }
        else if(parts.length==3 && parts[2].length()==1 && parts[2].charAt(0)=='{' ){ //struct identifier {
            enclosingStruct=parts[1];
            insideStruct=true;
            DataType temp=new DataType(enclosingStruct,DataType.STRUCTURE);
            addIdentifier(enclosingStruct,temp);
        }
        else{
            throw new typeException("The syntax is incorrect for a structures");
        }
    }

    static void parseFunc(String line){
        if(insideFunc==true){
            throw new typeException("Function definition inside another is not allowed");
        }
        if(insideStruct==true){
            throw new typeException("Structures do not support functions");
        }
        insideFunc=true;
        int i=4; //func has 4 letters
        while(line.charAt(i) == ' ' || line.charAt(i) == '\t'){ //Account for all the possible whitespaces
            i++;
        }
        String functionName = "";
        String arguments = "";
        String inBetweens = ""; //the part between the arguments and {}
        while(line.charAt(i) != '('){
            functionName += line.charAt(i); //gather the non-whitespace characters
            i++;
            if(i == line.length()) break; //not found any (
        }
        if(i == 5 || i == line.length()){
          throw new typeException("Definition of function is incomplete");
        }
        else{

            i++;
            String trimedName=functionName.trim();
            enclosingFunc=trimedName;
            DataType temp=new DataType(trimedName,DataType.FUNCTION);
            addIdentifier(trimedName,temp); //found2arg
            if(i == line.length()){
                throw new typeException("Missing ')' in function declaration");
            }

            while(line.charAt(i) != ')'){
                arguments += line.charAt(i);
                i++;
                if(i == line.length()) break;
            }
            if(i == line.length()){
                throw new typeException("Missing '{'");
            }


            i++;
            if(i == line.length())
             throw new typeException("Reached end of string, '{' '}'  not found");
            while(line.charAt(i) != '{'){
                inBetweens += line.charAt(i);
                i++;
                if(i == line.length() ) break;
            }
            if(i == line.length()){
                throw new typeException("Error, Not found '}' ");

            }


            if( line.charAt(line.length() - 1 ) != '}'){
                throw new typeException("Error, no closing '}' found");
            }



        }
        //after parsing, figuring out arguments and return types area, we parse to find the specifics
        //parsing the arguments and separating them into identifier and data type
        arguments = arguments.trim();
        if(arguments.length()!=0){
            String[] argumentsList = arguments.split(",");

            for(String x : argumentsList){
                String[] argumentParts = x.split(":");
                if(argumentParts.length % 2 != 0 || argumentParts.length==0){ //argumentParts stores the arguments in pairs
                    throw new typeException("Error, arguments are not entered properly, please enter variableName: dataType");
                }
                else{
                    //check if the data type is a primitive or an array
                    String trimedType=argumentParts[1].trim();
                    if(isPrimitiveType(trimedType)==true){
                        DataType tempType = new DataType(trimedType,DataType.PRIMITIVE);
                        String name = trimedType;
                        addIdentifier(name,tempType,enclosingFunc);
                    }
                    else if(trimedType.charAt(0)=='['){
                        String trimedIdentifierName=argumentParts[0].trim();
                        checkArray(trimedIdentifierName,trimedType);
                    }
                    else{
                        throw new typeException("The type is invalid or has not been defined yet");
                    }
                }
            }
        }


        inBetweens = inBetweens.trim();
        int j = 0;
        if(inBetweens.length() == 0){ //parsing for return arguments

        }
        else{
            boolean  t2 = false;
            insideReturn=true;
            if(inBetweens.charAt(0) == '-' && inBetweens.charAt(1) == '>'){

            }
            j = 2;
            if(j == inBetweens.length() ){
                throw new typeException("Missing return arguments");
            }

            String returnArgs = "";
            while( inBetweens.charAt(j) != '('){
                j++;
                if(j == inBetweens.length() ){
                    t2 = true;
                    break;
                }
            }
            if(t2){

                j = 2;
                while(j != inBetweens.length() ){
                    returnArgs += inBetweens.charAt(j++);
                }

                returnArgs = returnArgs.trim();
                String returnArgsList[] = returnArgs.split(":");
                String trimedType = returnArgsList[1].trim();
                if(isPrimitiveType(trimedType)==true){
                    DataType tempType=new DataType(trimedType,DataType.PRIMITIVE);
                    String name= trimedType;
                    addReturnType(name,tempType,enclosingFunc);
                }
                else if(trimedType.charAt(0)=='['){
                    String trimedIdentifierName = returnArgsList[0].trim();
                    checkArray(trimedIdentifierName,trimedType);
                }
                else{
                    throw new typeException("The return type is invalid or has not been defined yet");
                }
            }
            else{
                j++;
                while( inBetweens.charAt(j) != ')'){
                    returnArgs += inBetweens.charAt(j);
                    j++;
                    if(j ==inBetweens.length() ) break;
                }
                if(j==inBetweens.length() ){
                    throw new typeException("Missing ')' for return tuple");

                }


                if(returnArgs.trim().length()!=0){
                    String[] commaSeparated = returnArgs.split(",");
                    if(commaSeparated.length == 1 ){
                        throw new typeException("cannot create a single-element tuple with an element label");
                    }
                    for(String x : commaSeparated){
                        String[] returnArgList = x.split(":");
                        if(returnArgList.length%2 != 0 || returnArgList.length==0){
                            throw new typeException("There is an error in return arguments, please enter variableName: dataType");

                        }

                        String trimedType=returnArgList[1].trim();
                        if(isPrimitiveType(trimedType)==true){
                            DataType tempType=new DataType(trimedType,DataType.PRIMITIVE);
                            String name= trimedType;
                            addReturnType(name,tempType,enclosingFunc);
                        }
                        else if(trimedType.charAt(0)=='['){
                            String trimedIdentifierName=returnArgList[0].trim();
                            checkArray(trimedIdentifierName,trimedType);
                        }
                        else{
                            throw new typeException("The return type is invalid or has not been defined yet");
                        }

                    }

                    // return arguments are stored in returnArgList
                }
            }
        }
        insideReturn=false;
        insideFunc=false;
    }


    static boolean isPrimitiveType(String type){
        //Check if the dataType lies in the set of primitive types
        if(typeTable.containsKey(type)==true){
            return true;
        }
        else{
            return false;
        }
    }
    static void inputPrompt(){
        System.out.println("This program currently supports primitives, arrays, structures and functions");
        System.out.println();
        System.out.println("Every DataType has an Identifier attached to it, \nfunc functioName(argumentName: argumentType) -> (returnVariableName: returnVariableType){} is accepted\nWhereas\nfunc functioName(argumentType) -> (returnVariableType){} is not accepted\nAlso, if a function returns one variable, it should not be put in parantheses and should be kept after the -> as variableName: variableType\nThe original language accepts (Int) and rejects (a:Int), we reject both.\n");
        System.out.println("Enter the input and press EOF(^D) to exit:");
    }
    static void init(){
        input="";
        enclosingStruct="";
        enclosingFunc="";
        identifierTable = new HashMap<>();
        typeTable = new HashMap<>();
        nonPrimitiveMap = new HashMap<>();
        arrayMap = new HashMap<>();
        returnMap = new HashMap<>();
        structures = new HashSet<>();
        insideStruct=false;
        insideFunc = false;
        insideReturn = false;
        initTypes();
    }
    static void getInput(){
        //take input from user
        // Scanner sc = new Scanner(System.in);
        // while(sc.hasNextLine()){
        //     input+=sc.nextLine()+"\n";
        // }
        String inputFile = "./TestCases.txt";
        String outputFile = "./Output.txt";
        int count=0;
        String testCaseDelim="TESTCASE";
        String line="";
        boolean TestCaseStart=false;
        try{
            File fileInput = new File(inputFile);
            Scanner sc=new Scanner(fileInput);
            BufferedWriter buffWrite=new BufferedWriter(new FileWriter(outputFile));
            while(sc.hasNextLine()){
                line=sc.nextLine();
                //System.out.println(line);
                if(line.equals(testCaseDelim) && TestCaseStart==true){
                    System.out.println("The input is:\n"+input);
                    String output="";
                    Equivalences equivalenceTable = new Equivalences();
                    tokenize(equivalenceTable);
                    equivalenceTable.populate();
                    output += equivalenceTable.output;
                    count++;
                    buffWrite.write(testCaseDelim+" "+count+"\n");
                    buffWrite.write(output);
                    buffWrite.write("\n");
                    init();
                }
                else if(line.equals(testCaseDelim)){
                	TestCaseStart=true;
                }
                else if(TestCaseStart==true){
                    input+=line+"\n";
                }
            }
            String output="";
            Equivalences equivalenceTable = new Equivalences();
            tokenize(equivalenceTable);
            equivalenceTable.populate();
            output += equivalenceTable.output;
            count++;
            buffWrite.write(testCaseDelim+" "+count+"\n");
            buffWrite.write(output);
            buffWrite.write("\n");
            buffWrite.close();
        }
        catch(FileNotFoundException fex){

        }
        catch(IOException ioe){

        }
    }
    static void tokenize(Equivalences equivalenceTable){
        //Tokenize the input and strip them of trailing whitespaces and then pass into respective parsers
        String trimedInput=input.trim();
        String lines[]=trimedInput.split("\n");
        int dataClass=0;
        for(String line: lines){
            String trimedLine=line.trim();
            if(trimedLine.length()<=0){
                continue;
            }
            if(trimedLine.contains(",") && trimedLine.contains("func")==false){
                equivalenceTable.internalNameEquivalence(trimedLine);
                continue;
            }
            String[] parts=trimedLine.split("\\s+");

            dataClass=Identifier.identify(parts[0]);
            if(dataClass==DataType.PRIMITIVE){
                parsePrimitive(parts);
            }
            else if(dataClass==DataType.STRUCTURE){
                parseStruct(parts);
            }
            else if(dataClass==DataType.FUNCTION){
                parseFunc(trimedLine);
            }
            else if(insideStruct==true && parts[0].charAt(0)=='}'){
                insideStruct=false;
            }
            else{
                throw new typeException("This type has not been defined for this mini-language");
            }
        }
    }
    public static void main(String args[]){
        initTypes();
        inputPrompt();
        getInput();
    }

}
class Equivalences{
    static int internalEquivalenceCount=1;
    ArrayList<String> primitives = new ArrayList<>();
    ArrayList<String> structures = new ArrayList<>();
    ArrayList<String> functions = new ArrayList<>();
    ArrayList<String> arrays = new ArrayList<>();
    ArrayList<String> nameEquivalences = new ArrayList<>();
    ArrayList<String> structuralEquivalence = new ArrayList<>();
    HashMap<String,ArrayList<String>> internalNameEquivalentMap=new HashMap<>();
    HashMap<String,ArrayList<String>> nameEquivalentPrimitive=new HashMap<>();
    HashMap<String,ArrayList<String>> nameEquivalentArray=new HashMap<>();
    boolean[][] truths=null;
    String output = "";

    void nameEquivalence(ArrayList<String> identifiers,int klass ){
        // String output = "";
        if(klass==DataType.PRIMITIVE){
            for(String key:identifiers){
                String dataType=Parser.identifierTable.get(key).dataType.name;
                if(nameEquivalentPrimitive.containsKey(dataType)==true){
                    nameEquivalentPrimitive.get(dataType).add(key);
                }
                else{
                    ArrayList<String> temp=new ArrayList<>();
                    temp.add(key);
                    nameEquivalentPrimitive.put(dataType,temp);
                }
            }

            // System.out.println("@@@@@@@@@@@@@@@@@@ PRIMITIVE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            output += "@@@@@@@@@@@@@@@@@@ PRIMITIVE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n";
            for(String key:nameEquivalentPrimitive.keySet()){
                // System.out.print(key + ": ");
                output += key + ": ";
                for(String identifier:nameEquivalentPrimitive.get(key)){
                    // System.out.print(identifier+" ");
                    output += identifier+" ";
                }
                // System.out.println("");
                output += "\n";
            }
        }
        else if(klass==DataType.ARRAY){
            for(String key:identifiers){
                String dataType=Parser.arrayMap.get(key).toString();
                if(nameEquivalentArray.containsKey(dataType)==true){
                    nameEquivalentArray.get(dataType).add(key);
                }
                else{
                    ArrayList<String> temp=new ArrayList<>();
                    temp.add(key);
                    nameEquivalentArray.put(dataType,temp);
                }

            }

            // System.out.println("@@@@@@@@@@@@@@@@@@ ARRAY @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            output += "@@@@@@@@@@@@@@@@@@ ARRAY @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n";
            for(String key:nameEquivalentArray.keySet()){
                // System.out.print(key + ": ");
                output += key + ": ";
                for(String identifier:nameEquivalentArray.get(key)){
                    // System.out.print(identifier+" ");
                    output += identifier+" ";
                }
                // System.out.println("");
                output += "\n";
            }

        }
        else{
            throw new typeException("Invalid type for nameEquivalence");
        }
    }
    boolean compare(String struct1,String struct2,boolean[][] truths){
        ArrayList<String> set1 = Parser.nonPrimitiveMap.get(struct1);
        ArrayList<String> set2 = Parser.nonPrimitiveMap.get(struct2);

        if(set1.size()!=set2.size()){
            return false;
        }

        for(int i=0;i<set1.size();i++){
            if(Parser.structures.contains(set1.get(i))==true && Parser.structures.contains(set2.get(i))==true){
                 if(truths[structures.indexOf(set1.get(i))][structures.indexOf(set2.get(i))]==false){
                    return false;
                 }
            }
            else if(Parser.structures.contains(set1.get(i))==false && Parser.structures.contains(set2.get(i))==false){
                if(set1.get(i).equals(set2.get(i))==false){
                    return false;
                }
            }
            else{
                return false;
            }
        }
        return true;
    }

    boolean comapareReturns(String struct1,String struct2,boolean[][] truths){
        ArrayList<String> set1 = Parser.returnMap.get(struct1);
        ArrayList<String> set2 = Parser.returnMap.get(struct2);

        if(set1.size()!=set2.size()){
            return false;
        }

        for(int i=0;i<set1.size();i++){
            if(Parser.structures.contains(set1.get(i))==true && Parser.structures.contains(set2.get(i))==true){
                 if(truths[structures.indexOf(set1.get(i))][structures.indexOf(set2.get(i))]==false){
                    return false;
                 }
            }
            else if(Parser.structures.contains(set1.get(i))==false && Parser.structures.contains(set2.get(i))==false){
                if(set1.get(i).equals(set2.get(i))==false){
                    return false;
                }
            }
            else{
                return false;
            }
        }
        return true;
    }
    void structuralEquivalence(ArrayList<String> identifiers,int klass){

        if(klass==DataType.STRUCTURE){
            truths=new boolean[identifiers.size()][identifiers.size()];
            for(int i=0;i<identifiers.size();i++){
                for(int j=0;j<identifiers.size();j++){
                    truths[i][j]=true;
                }
            }
            boolean updated=true;


            while(updated==true){
                updated=false;
                for(int i=0;i<identifiers.size()-1;i++){
                    for(int j=i+1;j<identifiers.size();j++){
                        if(truths[i][j]==true && compare(identifiers.get(i),identifiers.get(j),truths)==false){
                            truths[i][j]=false;
                            truths[j][i]=false;
                            updated=true;
                        }
                    }
                }
            }




            HashMap<String,String> equivalences = new HashMap<>();
            HashMap<String,Boolean> printed = new HashMap<>();
            for(int i=0;i<identifiers.size();i++){
                equivalences.put(structures.get(i),structures.get(i));
                printed.put(structures.get(i),false);
            }

            for(int i=0;i<identifiers.size()-1;i++){
                if(equivalences.get(structures.get(i) ).equals(structures.get(i))){
                    for(int j=i+1;j<identifiers.size();j++){
                        if(truths[i][j]==true){
                            equivalences.put(structures.get(j),structures.get(i));
                        }
                    }
                }
            }

            // System.out.println("@@@@@@@@@@@@@@@@@@ STRUCTURE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            output += "@@@@@@@@@@@@@@@@@@ STRUCTURE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n";
            for(String i: equivalences.keySet()){
                if(printed.get(i) == false){
                    // System.out.print(i+": ");
                    output += i+": ";
                    for(String j: equivalences.keySet()){
                        if(equivalences.get(j) == i){
                            printed.put(j,true);
                            // System.out.print(j+" ");
                            output += j+" ";
                        }
                    }
                    // System.out.println();
                    output += "\n";
                }
            }
        }
        else if(klass==DataType.FUNCTION){
            boolean[][] funcTruths=new boolean[identifiers.size()][identifiers.size()];


            for(int i=0;i<funcTruths.length;i++){
                for(int j=0;j<funcTruths.length;j++){
                    funcTruths[i][j]=true;
                }
            }

            boolean updated=true;
            while(updated==true){
                updated=false;
                for(int i=0;i<funcTruths.length-1;i++){
                    for(int j=i+1;j<funcTruths.length;j++){
                        if(funcTruths[i][j]==true){
                            //Check for returns

                            if( (Parser.returnMap.get(identifiers.get(i)).size()!=0 && Parser.returnMap.get(identifiers.get(j)).size()!=0)){
                                if(compare(identifiers.get(i),identifiers.get(j),truths)==false ){
                                    funcTruths[i][j]=false;
                                    funcTruths[j][i]=false;
                                    updated=true;

                                }
                                else if(comapareReturns(identifiers.get(i),identifiers.get(j),truths)==false){
                                    funcTruths[i][j]=false;
                                    funcTruths[j][i]=false;
                                    updated=true;

                                }
                            }
                            else if(Parser.returnMap.get(identifiers.get(i)).size()==0 && Parser.returnMap.get(identifiers.get(j)).size()==0){
                                if(compare(identifiers.get(i),identifiers.get(j),truths)==false ){
                                    funcTruths[i][j]=false;
                                    funcTruths[j][i]=false;
                                    updated=true;

                                }
                            }
                            else{
                                funcTruths[i][j]=false;
                                funcTruths[j][i]=false;
                                updated=true;
                            }
                        }
                    }
                }
            }

            HashMap<String,String> equivalences = new HashMap<>();
            HashMap<String,ArrayList<String>> print = new HashMap<>();

            for(int i=0;i<identifiers.size();i++){
                equivalences.put(identifiers.get(i),identifiers.get(i));
            }

            for(int i=0;i<identifiers.size()-1;i++){
                if(equivalences.get(identifiers.get(i)).equals(identifiers.get(i))==true){
                    ArrayList<String> temp=new ArrayList<>();
                    temp.add(identifiers.get(i));
                    print.put(identifiers.get(i),temp);
                    for(int j=i+1;j<identifiers.size();j++){
                        if(funcTruths[i][j]==true){
                            equivalences.put(identifiers.get(j),identifiers.get(i));
                            print.get(identifiers.get(i)).add(identifiers.get(j));
                        }
                    }
                }
            }
            if(identifiers.size()>0 && equivalences.get(identifiers.get(identifiers.size()-1)).equals(identifiers.get(identifiers.size()-1))==true){
                ArrayList<String> temp=new ArrayList<>();
                temp.add(identifiers.get(identifiers.size()-1));
                print.put(identifiers.get(identifiers.size()-1),temp);
            }

            // System.out.println("@@@@@@@@@@@@@@@@@@ FUNCTION @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            output += "@@@@@@@@@@@@@@@@@@ FUNCTION @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n";
            for(String key:print.keySet()){
                // System.out.print(key+": ");
                output += key + ": ";
                for(String elem:print.get(key)){
                    // System.out.print(elem+" ");
                    output += elem+" ";
                }
                // System.out.println();
                output += "\n";
            }
        }
        else{
            throw new typeException("Invalid type for nameEquivalence");
        }
    }
    void internalNameEquivalence(String line){
        String[] parts=line.split(",");
        ArrayList<String> internalNameEquivalence = new ArrayList<>();

        for(int i=0;i<parts.length-1;i++){
            if(parts[i].contains(":")){
                throw new typeException("Illegal :");
            }
            String[] subParts=parts[i].split("\\s+");
            if(subParts.length>1){
                throw new typeException("Identifiers not , separated");
            }
            internalNameEquivalence.add(subParts[0]);//Not terminal
        }
        if(parts[parts.length-1].contains(":")==false){
            throw new typeException("No : found");
        }
        parts[parts.length-1]=parts[parts.length-1].trim();
        String subParts[]=parts[parts.length-1].split(":");
        if(subParts.length!=2){
            throw new typeException("Too many identifiers or types matched");
        }
        internalNameEquivalence.add(subParts[0]);//terminal
        if(Parser.isPrimitiveType(subParts[1].trim())==true ){
            internalNameEquivalentMap.put(internalEquivalenceCount+". "+subParts[1],internalNameEquivalence);
            internalEquivalenceCount++;
        }
        else if(subParts[1].trim().charAt(0)=='['){
            int count=0;
            String typeVal=Parser.isArray(subParts[1].trim());
            String trimedType=subParts[1].trim();
            while(trimedType.charAt(count)=='['){
                count++;
            }
            internalNameEquivalentMap.put(internalEquivalenceCount+". Array of dimension " + count + " and type is " + typeVal + ":" , internalNameEquivalence);
            internalEquivalenceCount++;
        }
        else{
            throw new typeException("Invalid type");
        }

        // System.out.println("@@@@@@@@@@@@@@@@@@ INTERNAL NAME EQ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

    }

    void fillPrimitives(String identifierName){
        primitives.add(identifierName);
    }
    void fillStructs(String identifierName){
        structures.add(identifierName);
    }
    void fillFuncs(String identifierName){
        functions.add(identifierName);
    }
    void fillArrays(String identifierName){
        arrays.add(identifierName);
    }
    void printEquivalences(String prompt,ArrayList<String[]> Output){
        System.out.println(prompt);
        for(String[] pair : Output){
            System.out.println(pair[0]+","+pair[1]);
        }
    }
    void populate(){
        for(String key:Parser.identifierTable.keySet()){
            if(Parser.identifierTable.get(key).dataType.dataClass==DataType.PRIMITIVE){
                fillPrimitives(key);
            }
            else if(Parser.identifierTable.get(key).dataType.dataClass==DataType.STRUCTURE){
                fillStructs(key);
            }
            else if(Parser.identifierTable.get(key).dataType.dataClass==DataType.ARRAY){
                fillArrays(key);
            }
            else if(Parser.identifierTable.get(key).dataType.dataClass==DataType.FUNCTION){
                fillFuncs(key);
            }
        }

        nameEquivalence(primitives,DataType.PRIMITIVE);
        nameEquivalence(arrays,DataType.ARRAY);
        structuralEquivalence(structures,DataType.STRUCTURE);
        structuralEquivalence(functions,DataType.FUNCTION);
        printInernalQuivalence();
    }
    void printInernalQuivalence(){
        output += "@@@@@@@@@@@@@@@@@@ INTERNAL NAME EQ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n";
        for(String key: internalNameEquivalentMap.keySet()){
            // System.out.print(key+" ");
            output += key+" ";
            for(String identifier:internalNameEquivalentMap.get(key)){
                // System.out.print(identifier+" ");
                output += identifier + " ";
            }
            // System.out.println("");
            output += "\n";
        }
    }
}
