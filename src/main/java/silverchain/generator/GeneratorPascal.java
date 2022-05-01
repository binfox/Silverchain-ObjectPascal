package silverchain.generator;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import silverchain.diagram.Diagram;
import silverchain.diagram.Diagrams;
import silverchain.diagram.Label;
import silverchain.diagram.State;
import silverchain.diagram.Transition;
import silverchain.javadoc.Javadocs;
import silverchain.parser.FormalParameter;
import silverchain.parser.FormalParameters;
import silverchain.parser.Method;
import silverchain.parser.MethodParameters;
import silverchain.parser.TypeParameter;
import silverchain.parser.TypeParameterList;

public final class GeneratorPascal extends GeneratorJava {

    /**
     * Constructor
     *
     * @param diagrams The diagrams for the fluent interface
     * @param javadocs the not used JavaDocs.
     */
    public GeneratorPascal(Diagrams diagrams, Javadocs javadocs) {
        super(diagrams, javadocs);
    }

    /**
     * Returns the first part of a dot-seperated-string
     *
     * @param in Inputstring
     * @param minparts Minimum Count of Dots
     * @return Return String
     */
    private String getfirstPointpartfromString(String in, int minparts) {
        String[] parts = in.split("\\.");
        if (parts.length < minparts) {
            return "";
        } else {
            return parts[0];
        }
    }

    /**
     * Returns the last part of a dot-seperated-string
     *
     * @param in Inputstring
     * @return Return String
     */
    private String getlastPointpartfromString(String in) {
        String[] parts = in.split("\\.");
        return parts[parts.length - 1];
    }

    @Override
    protected void generate(Diagram diagram, Javadocs javadocs) {
        // Open a single file
        beginFile(diagram.name().name() + "_fluent.pas");

        //write header
        write("unit " + diagram.name().name() + "_fluent;");
        writeLineBreak();
        write("{$interfaces CORBA}");
        writeLineBreak();
        write("Interface");
        writeLineBreak();

        writeuses(diagram); // write the uses
        writeLineBreak();

        //write the classes declerations
        write("type");
        writeLineBreak();

        generateforward(diagram);
        generatedecl(diagram);

        // write the implementation
        writeLineBreak();
        write("Implementation");
        writeLineBreak();
        generateimpl(diagram);

        // Close the single file
        endFile();
    }

    /**
     * Write the uses. It generates them from the parameters and the
     * return-types.
     *
     * @param diagram Diagramm with the States
     */
    protected void writeuses(Diagram diagram) {
        ArrayList<String> uses = new ArrayList<>();
        ArrayList<String> writeuses = new ArrayList<>();
        diagram.numberedStates().forEach(state -> uses.addAll(getStateUses(state)));
        for (String s : uses) {
            if (!writeuses.contains(s)) {
                writeuses.add(s);
            }
        }
        if (!writeuses.isEmpty()) {
            write("uses ");
            writeStateUses(writeuses);
            writeSemicolon();
        }

    }

    /**
     * Generates and writes forward declerations for alle classes
     *
     * @param diagram the Diagramm with the states
     */
    protected void generateforward(Diagram diagram) {
        write("//Forward decleration");
        writeLineBreak();
        diagram.numberedStates().forEach(state -> writeforward(state));
        writeLineBreak();
    }

    /**
     * Generate the decleration for all numbered states in the diagramm
     *
     * @param diagram the diagramm with the numbered states
     */
    protected void generatedecl(Diagram diagram) {
        writeIActionDecl(diagram);
        diagram.numberedStates().forEach(state -> generatedecl(state));
        writeLineBreak();

    }

    /**
     * Generate the implementation for all numbered states in the diagramm
     *
     * @param diagram the diagramm with the numbered states
     */
    protected void generateimpl(Diagram diagram) {
        diagram.numberedStates().forEach(state -> generateimpl(state));
        writeIActionImpl(diagram);
        writeLineBreak();
    }

    /**
     * Generate the implementation for a states
     *
     * @param state
     */
    protected void generateimpl(State state) {
        writeStateImpl(state);
        writeLineBreak();
    }

    /**
     * Generate the decleration for a states
     *
     * @param state
     */
    protected void generatedecl(State state) {
        writeIStateDecl(state);
        writeStateDecl(state);
        writeLineBreak();
    }

    /**
     * Writes the forward-decleration for a state
     *
     * @param state
     */
    protected void writeforward(State state) {
        //super.generate(state,javadocs);
        if (state.typeParameters().isEmpty()) {
          writeIndentation();
          write(getIStateName(state) + " = Interface;");
          writeLineBreak();
          writeIndentation();
          write(getIStateName(state) + "Impl = Class;");
          writeLineBreak();
        }else{
          //It works with FPC 3.3, FPC 3.2 can not Handle it.
          //ref: https://gitlab.com/freepascal.org/fpc/source/-/issues/34128
          // to aktivate it just remove the Backslashes
          writeIndentation();
          write("//generic "+getIStateName(state)+encode(state.typeParameters(), false).replace(";", ",") + " = Interface;");
          writeLineBreak();
          writeIndentation();
          write("//generic "+getIStateName(state) + "Impl"+encode(state.typeParameters(), false).replace(";", ",")+" = Class;");
          writeLineBreak();
        }
    }

    /**
     * write the Classdecleration with generics
     *
     * @param name Name ov the Class
     * @param generic Generic types
     */
    protected void writeClassDeclarationHead(String name, String generic) {

        writeIndentation();
        if (!generic.isEmpty()) {
            writeGeneric();
        }
        write(name);
        if (!generic.isEmpty()) {
            write(" ");

            write(generic.replace(";", ","));
        }
        write("= Class(TInterfacedObject,");

    }

    @Override
    protected void writeClassDeclarationHead(String name) {
        writeClassDeclarationHead(name, "");
    }

    /**
     * writes the interface state decleration
     *
     * @param state
     */
    protected void writeIStateDecl(State state) {
        write("//writeIStateDecl");
        writeLineBreak();

        writeInterfaceDeclaration(getIStateName(state), encode(state.typeParameters(), true));
        //writeTypeParameterDeclaration(state.typeParameters());
        writeLineBreak();

        // Method declaration
        for (Transition transition : state.transitions()) {
            writeIndentation();
            writeIndentation();

            writeStateMethodDeclaration(transition);
            writeSemicolon();
        }
        writeIndentation();
        writeRightBracket();

    }

    /**
     * Writes the Deleration of an Interface with generics
     *
     * @param name Name of the itnerface
     * @param generic Generic types
     */
    protected void writeInterfaceDeclaration(String name, String generic) {

        writeIndentation();
        if (!generic.isEmpty()) {
            writeGeneric();
        }
        write(name);
        if (!generic.isEmpty()) {
            write(" ");

            write(generic.replace(";", ",").replace(" extends ", ":"));
        }
        write("= interface");
    }

    @Override
    protected void writeInterfaceDeclaration(String name) {
        writeInterfaceDeclaration(name, "");
    }

    /**
     * Writes the abstract classes decleration for the actions
     *
     * @param diagram
     */
    protected void writeIActionDecl(Diagram diagram) {
        write("//writeIActionDecl");
        writeLineBreak();

        // Interface declaration
        writeAbstractDeclaration(getIActionName(diagram), encode(diagram.typeParameters(), true));

        //writeTypeParameterDeclaration(diagram.typeParameters());
        writeLineBreak();

        //Liste der proceduren/functionen
        List<Transition> transitions
                = diagram.numberedStates().stream()
                        .map(State::transitions)
                        .flatMap(Collection::stream)
                        .collect(toList());

        for (Transition transition : transitions) {
            writeIndentation();
            writeIndentation();
            write(getIActionMethodHead(transition, true));
            writeSemicolon();
        }

        Set<Method> encodedMethods = new HashSet<>();
        for (Transition transition : transitions) {
            if (!encodedMethods.contains(transition.label().method())) {
                writeIndentation();
                writeIndentation();
                write(getIActionMethodHead(transition, false));
                write("; virtual; abstract");
                writeSemicolon();
                encodedMethods.add(transition.label().method());
            }
        }
        writeIndentation();
        writeRightBracket();
    }

    /**
     * Writes the implementation of the abstract action classes
     *
     * @param diagram
     */
    protected void writeIActionImpl(Diagram diagram) {
        write("//writeIActionImpl");
        writeLineBreak();
        //Liste der proceduren/functionen
        List<Transition> transitions
                = diagram.numberedStates().stream()
                        .map(State::transitions)
                        .flatMap(Collection::stream)
                        .collect(toList());

        for (Transition transition : transitions) {

            write(getIActionMethodHead(transition, true, getIActionName(diagram)));
            writeSemicolon();

            writeLeftBracket();

            writeIndentation();
            write(getIActionMethodBody(transition));
            writeLineBreak();

            writeRightBracket();
            writeLineBreak();
        }

        //endFile();
    }

    /**
     * writes the abstract Class decleration with generics
     *
     * @param name name if the abstract class
     * @param generic Generic types
     */
    protected void writeAbstractDeclaration(String name, String generic) {
        writeIndentation();
        if (!generic.isEmpty()) {
            writeGeneric();
        }
        write(name);
        if (!generic.isEmpty()) {
            write(" ");

            write(generic.replace(";", ",").replace(" extends ", ":"));
        }
        write("= Class Abstract");

    }

    /**
     * returns the Methode head of a transition
     *
     * @param transition
     * @param full
     * @param prefix
     * @return
     */
    protected String getIActionMethodHead(Transition transition, boolean full, String prefix) {
        MethodParameters mps = transition.label().method().parameters();
        Optional<TypeParameterList> opt = mps.localTypeParameters();
        List<TypeParameter> lst = opt.map(l -> l.stream().collect(toList())).orElse(emptyList());
        String s1 = lst.isEmpty() ? "" : encode(lst, true) + " ";

        State d = transition.destination();
        String s2 = "procedure ";
        String s3 = "";
        String specialize = "";

        if (!getIStateReference(d).equals("void")) {
            if (d.isNumbered()) {
                s2 = "procedure ";
                s3 = "";
            } else {
                s2 = "function ";
                s3 = getlastPointpartfromString(getIStateReference(d));
                if (s3.contains("<") && s3.contains(">")) {
                    specialize = ":specialize ";
                    s3=s3.replace(";", ",");
                } else {
                    s3 = ":" + s3;
                }
            }
        }
        if (!prefix.isEmpty()) {
            prefix = prefix + ".";
        }
        return s1 + s2 + " " + prefix + ReplaceDollar(getIActionMethodSignature(transition, true, full)) + specialize + s3;
    }

    @Override
    protected String getIActionMethodHead(Transition transition, boolean full) {
        return getIActionMethodHead(transition, full, "");
    }

    /**
     * writes the class decleration of a state
     *
     * @param state
     */
    protected void writeStateDecl(State state) {
        write("//writeStateDecl");
        writeLineBreak();

        // Class declaration
        writeClassDeclarationHead(
                getStateName(state),
                encode(state.typeParameters(), true));

        //Superclass
        write(getlastPointpartfromString(getIStateReference(state)));
        write(")");
        writeLineBreak();

        // Field declaration
        writeIndentation();
        write("private");
        writeLineBreak();

        writeIndentation();
        writeIndentation();
        writeActionDeclaration(state.diagram(), true);
        writeSemicolon();
        writeLineBreak();
        writeIndentation();
        write("public");
        writeLineBreak();

        // Constructor
        writeIndentation();
        writeIndentation();
        writeConstructor();
        write("Create(");
        writeActionDeclaration(state.diagram());
        write(")");
        writeSemicolon();

        // Method declaration
        for (Transition transition : state.transitions()) {
            writeIndentation();
            writeIndentation();

            write(getStateMethodHead(transition));
            writeSemicolon();

        }
        writeIndentation();
        writeRightBracket();
    }

    /**
     * write the implementation of a state
     *
     * @param state
     */
    protected void writeStateImpl(State state) {
        write("//writeStateImpl");
        writeLineBreak();

        //Constructor implementation
        writeConstructor();
        write(getStateName(state));
        write(".Create(");
        writeActionDeclaration(state.diagram());
        write(")");
        writeSemicolon();
        writeLeftBracket();
        writeIndentation();
        write("Self.action := myaction");
        writeSemicolon();
        writeRightBracket();
        writeLineBreak();

        // Method implementation
        for (Transition transition : state.transitions()) {

            write(getStateMethodHead(transition, getStateName(state)));
            writeSemicolon();
            writeLeftBracket();
            writeIndentation();
            write(getStateMethodBodyListenerInvocation(transition));
            writeIndentation();
            write(getStateMethodBodyReturnNextState(transition));
            writeRightBracket();
            writeLineBreak();

        }

    }

    @Override
    protected String getIStateQualifiedName(State state) {
        return getIStateName(state); // Pascal has not the Qualified Names
    }

    @Override
    protected String getFilePath(String name) {
        return name.replace('.', '/') + "_fluent.pas";
    }

    /**
     * write the name of a unit and adds "_fluent"
     *
     * @param name
     */
    protected void writeUnit(String name) {
        write("unit " + name + "_fluent;\n\n");
    }

    @Override
    protected void writePackageDeclaration(String name) {
        write(name.isEmpty() ? "" : "unit " + name + ";\n\n");
    }

    /**
     * write the name of the action variable or the call of the function with
     * the action var.
     *
     * @param diagram
     * @param variable Is it the variable or the call of the function
     */
    protected void writeActionDeclaration(Diagram diagram, Boolean variable) {
        if (!variable) {
            write("myaction:");
        } else {
            write("action:");
        }
        if (!diagram.typeParameters().isEmpty()) {
            write("specialize " + getIActionQualifiedName(diagram).replace(";", ",") + encode(diagram.typeParameters(), false).replace(";", ","));
        } else {
            write(getIActionQualifiedName(diagram));
        }
    }

    @Override
    protected void writeActionDeclaration(Diagram diagram) {
        writeActionDeclaration(diagram, false);
    }

    @Override
    protected String getIActionQualifiedName(Diagram diagram) {
        return getIActionName(diagram); // Pascal has not the Qualified Names
    }

    @Override
    protected void writeLeftBracket() {
        write("Begin\n");
    }

    @Override
    protected void writeRightBracket() {
        write("End;\n");
    }

    /**
     * Exports the parameters of a function as an Arraylist of strings
     *
     * @param p the formal parameters
     * @return an list of the Parameters
     */
    private ArrayList<String> getparams(FormalParameters p) {

        ArrayList<String> ausgabe = new ArrayList<>();
        for (FormalParameter f : p) {
            ausgabe.add(f.type().toString());
        }
        return ausgabe;
    }

    /**
     * Write the uses
     *
     * @param in the list of all uses
     */
    protected void writeStateUses(ArrayList<String> in) {
        Boolean first = true;
        for (String txt : in) {
            if (!first) {
                write(" , ");
            }
            write(txt);
            first = false;
        }
    }

    /**
     * Get the uses from a state
     *
     * @param s
     * @return List of uses
     */
    protected ArrayList<String> getStateUses(State s) {
        ArrayList<String> extuses = new ArrayList<>();
        for (Transition transition : s.transitions()) {
            State d = transition.destination();
            Label l = transition.label();

            //Parameter
            if (transition.label().method().parameters().formalParameters().isPresent()) {
                ArrayList<String> param = getparams(transition.label().method().parameters().formalParameters().get());
                for (String p : param) {
                    String part = getfirstPointpartfromString(p, 2);
                    if (!part.isEmpty()) {
                        extuses.add(part);
                    }
                }
            }

            //Pascal verbindet die Units �ber USES - R�ckgabetypen m�ssen beachtet werden.
            String[] extclasses = getIStateReference(d).split("\\.");
            if (extclasses.length > 1) {
                // es existieren eingezogene Klassen f�r die R�ckgabeparameter
                if (!extclasses[0].equals("java")) {
                    if (!extuses.contains(extclasses[0])) {
                        extuses.add(extclasses[0]);
                    }
                }
            }
        }
        return extuses;
    }

    @Override
    protected <T> String csv(Stream<T> stream, Function<T, String> function) {
        return stream.map(function).collect(joining("; "));
    }

    @Override
    protected String encode(FormalParameter parameter, boolean decl) {
        if (decl) {
            String s1 = getlastPointpartfromString(encode(parameter.type()));
            String s2 = parameter.isVarArgs() ? "... " : " ";
            String s3 = parameter.name();
            if (s1.equalsIgnoreCase("int")) {
                s1 = "Integer";
            }
            return s2 + s3 + ":" + s1;
        }
        return parameter.name();
    }

    @Override
    protected String getIStateReference(State state) {
        if (state.isNumbered()) {
            if (state.typeParameters().isEmpty()) {
                return getIStateQualifiedName(state) + encode(state.typeParameters(), false);
            } else {
                return "specialize " + getIStateQualifiedName(state) + encode(state.typeParameters(), false).replace(";", ",");
            }
        }
        return state.typeReference().map(l -> encode(l.typeReference())).orElse("void");
    }

    /**
     * encodes a method
     *
     * @param method
     * @param decl is a decleration
     * @param Generic Gerneic types
     * @return
     */
    protected String encode(Method method, boolean decl, String Generic) {
        String s = method.parameters().formalParameters().map(p -> encode(p, decl)).orElse("");
        s = method.name() + Generic.replace(";", ",") + "(" + s + ")";
        if (decl) {
            s = s + method.exceptions().map(es -> " throws " + encode(es)).orElse("");
        }
        return s;
    }

    @Override
    protected String getStateMethodHead(Transition transition) {
        return getStateMethodHead(transition, "");
    }

    /**
     * return the function head of a method
     *
     * @param transition
     * @param classname Name of the class
     * @return
     */
    protected String getStateMethodHead(Transition transition, String classname) {
        List<TypeParameter> p = transition.typeParameters();
        State d = transition.destination();
        Label l = transition.label();

        String pre = "function ";
        String s2 = getIStateName(d);
        if (!d.isNumbered()) {
            s2 = getlastPointpartfromString(getIStateReference(d));
        }
        List<TypeParameter> psub = d.typeParameters();
        if (!psub.isEmpty()) { //R�ckgebewert ist ein Generic
            if (!d.isNumbered()) {

                if (s2.contains("<") && s2.contains(">")) { //da ist etwas unklar in Silverchain.
                    s2 = ":specialize " + s2.replace(";", ",");
                } else {
                    s2 = ":" + s2;
                }

            } else {
                s2 = ":specialize " + s2 + encode(psub, true).replace(";", ",");
            }
        } else {
            if (s2.contains("<") && s2.contains(">")) { //von Hand �bergebene specialisierung
                s2 = ":specialize " + s2.replace(";", ",");
            } else {
                s2 = ":" + s2;
            }
        }

        if (getIStateReference(d).equals("void")) {
            s2 = "";
            pre = "procedure ";
        }
        if (!classname.isEmpty()) {
            classname = classname + ".";
        }

        //String s2 = getIStateReference(d) + " " + encode(l.method(), true);
        if (p.isEmpty()) {
            return pre + classname + encode(l.method(), true) + s2;
            // return encode(l.method(), true);
        } else {
            return "generic " + pre + classname + encode(l.method(), true, encode(p, true).replace(";", ",")).replace(" extends ", ":") + s2;

            // return encode(l.method(), true)+":"+getIStateName(d);
            //getIStateReference(d);
        }

    }

    @Override
    protected String getStateMethodBodyListenerInvocation(Transition transition) {
        Optional<Label> r = transition.destination().typeReference();
        String s1 = r.map(l -> "exit ").orElse("");
        String s2 = r.map(l -> l.typeReference().referent()).map(l -> "(" + l.name() + ") ").orElse("");
        if (s1.isEmpty()) {
            return "    " + s2 + "Self.action." + ReplaceDollar(getIActionMethodInvocation(transition, true)).replace(";", ",") + ";\n";
        } else {
            return "    " + s1 + s2 + "(Self.action." + ReplaceDollar(getIActionMethodInvocation(transition, true)).replace(";", ",") + ");\n";
        }

    }

    @Override
    protected String getStateMethodBodyReturnNextState(Transition transition) {
        State d = transition.destination();
        return d.isNumbered() ? "    exit( " + getStateQualifiedName(d) + ".Create(Self.action));\n" : "";
    }

    @Override
    protected String getIActionMethodBody(Transition transition) {
        String s = transition.destination().typeReference().map(r -> "exit ").orElse("");
        if (s.isEmpty()) {
            return getIActionMethodInvocation(transition, false).replace(";", ",") + ";";
        } else {
            return s + "(" + getIActionMethodInvocation(transition, false) + ");";
        }
    }

    @Override
    protected String getStateQualifiedName(State state) {
        return getStateName(state);
    }

    @Override
    protected void endFile() {
        write("End.");
        writeLineBreak();
        super.endFile();

    }
    
    protected void writeConstructor(){
        write("Constructor ");
    }

    /**
     * replaces the dollar symbole in a string, because of it is not useable in
     * Pascal names.
     *
     * @param in
     * @return the string with the replaced $
     */
    private String ReplaceDollar(String in) {
        return (in.replace("$", "_"));
    }

    private void writeGeneric() {
        write("generic ");
    }

}
