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


public final class GeneratorPascal extends GeneratorJava{

  private String getlastPointpartfromString(String in){
      String[] parts = in.split("\\.");
      return parts[parts.length-1];
  }

  public GeneratorPascal(Diagrams diagrams, Javadocs javadocs) {
      super(diagrams, javadocs);
  }

  @Override
  protected void generate(Diagram diagram, Javadocs javadocs) {
    beginFile(diagram.name().name()+"_fluent.pas");
    write("unit "+diagram.name().name()+"_fluent;");
    writeLineBreak();
    write("{$interfaces CORBA}");
    writeLineBreak();
    write("Interface");
    writeLineBreak();
    write("uses classes;");
    writeLineBreak();    
    generateuses(diagram);
    writeLineBreak();    
    write("type");
    writeLineBreak();
    
    generateforeward(diagram);
    generatedecl(diagram,javadocs);
     
    
    writeLineBreak();
    write("Implementation");
    writeLineBreak();    
    generateimpl(diagram,javadocs); 
    
    endFile();
  }
  protected void generateuses(Diagram diagram) {
      //protected boolean writeStateMethodReturnUses(State s,boolean close,boolean addimpl,String unitname) {
      diagram.numberedStates().forEach(state -> writeStateMethodReturnUses(state,false,false,diagram.name().name()));
      //diagram.numberedStates().forEach(state -> generateforeward(state));
  }
  
  protected void generateforeward(Diagram diagram) {
    write("//Foreward decleration");
    writeLineBreak();       
    diagram.numberedStates().forEach(state -> generateforeward(state));
    writeLineBreak();       
  }
  protected void generatedecl(Diagram diagram, Javadocs javadocs) {
    generateIActionDecl(diagram);      
    diagram.numberedStates().forEach(state -> generatedecl(state, javadocs));
    writeLineBreak();       

  }
  protected void generateimpl(Diagram diagram, Javadocs javadocs) {
    diagram.numberedStates().forEach(state -> generateimpl(state, javadocs));
    generateIActionImpl(diagram);
    writeLineBreak();       
  }

  
  protected void generateimpl(State state, Javadocs javadocs) {
    //super.generate(state,javadocs);
    generateStateImpl(state);
    writeLineBreak();       
  }
  
  protected void generatedecl(State state, Javadocs javadocs) {
    //super.generate(state,javadocs);

    generateIStateDecl(state,javadocs);
    //Fill uses with used Units from istatedecl
    //writeStateMethodReturnUses(state,true,false,getIStateName(state));
    generateStateDecl(state);
    /*
    //Fill uses with used Units
    write("uses ");
    if (writeStateMethodReturnUses(state,false,true,getStateName(state))){
        write(" , " );
    }
    write(getIActionName(state.diagram())+"_fluent");
    write(" , " );
    write(getIStateQualifiedName(state)+"_fluent");
    //write(getIStateReference(state)+"_fluent");
    
    writeSemicolon();
    writeLineBreak();    
    */    
    writeLineBreak();       
  }
  
  protected void generateforeward(State state) {
    //super.generate(state,javadocs);
    writeIndentation();
    write(getIStateName(state) +" = Interface;");
    writeLineBreak();
    writeIndentation();
    write(getIStateName(state) +"Impl = Class;");
    writeLineBreak();
  }
  
  
    protected void writeClassDeclarationHead(String name, String generic) {

       writeIndentation();
       if (!generic.isEmpty()){
           write("generic ");
       }
       write(name);
       if (!generic.isEmpty()){
           write(" ");
           
           write(generic.replace(";",","));
       }
       write("= Class(TInterfacedObject,");

   }  
  
  @Override
   protected void writeClassDeclarationHead(String name) {
        writeClassDeclarationHead(name,"");
   }
   
  protected void generateIStateDecl(State state, Javadocs javadocs) {
    write("//generateIStateDecl");
    writeLineBreak();    

    
    writeInterfaceDeclaration(getIStateName(state),encode(state.typeParameters(), true));
    //writeTypeParameterDeclaration(state.typeParameters());
    writeLineBreak();

    // Method declaration
    for (Transition transition : state.transitions()) {
      writeIndentation();
      writeIndentation();
      pasteComment(transition, javadocs);

      writeStateMethodDeclaration(transition);
      writeSemicolon();
    }
    writeIndentation();
    writeRightBracket();

  }   
  
protected void writeInterfaceDeclaration(String name,String generic) {
    
       writeIndentation();
       if (!generic.isEmpty()){
           write("generic ");
       }
       write(name);
       if (!generic.isEmpty()){
           write(" ");
           
           write(generic.replace(";",",").replace(" extends ", ":"));
       }
       write("= interface");
}
  
@Override  
protected void writeInterfaceDeclaration(String name) {
    writeInterfaceDeclaration(name,"");
}


protected void generateIActionDecl(Diagram diagram) {
    write("//generateIActionDecl");
    writeLineBreak();
    

    // Interface declaration
    writeAbstractDeclaration(getIActionName(diagram),encode(diagram.typeParameters(),true));
    
    //writeTypeParameterDeclaration(diagram.typeParameters());
    writeLineBreak();

    
    //Liste der proceduren/functionen
    List<Transition> transitions =
        diagram.numberedStates().stream()
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


protected void generateIActionImpl(Diagram diagram) {
    write("//generateIActionImpl");
    writeLineBreak();    
    //Liste der proceduren/functionen
    List<Transition> transitions =
        diagram.numberedStates().stream()
            .map(State::transitions)
            .flatMap(Collection::stream)
            .collect(toList());

    
    for (Transition transition : transitions) {
      
      write(getIActionMethodHead(transition, true,getIActionName(diagram)));
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

  protected void writeAbstractDeclaration(String name, String generic) {
       writeIndentation();
       if (!generic.isEmpty()){
           write("generic ");
       }
       write(name);
       if (!generic.isEmpty()){
           write(" ");
           
           write(generic.replace(";",",").replace(" extends ", ":"));
       }
       write("= Class Abstract");

  }

  
  
   protected String getIActionMethodHead(Transition transition, boolean full, String prefix) {
    MethodParameters mps = transition.label().method().parameters();
    Optional<TypeParameterList> opt = mps.localTypeParameters();
    List<TypeParameter> lst = opt.map(l -> l.stream().collect(toList())).orElse(emptyList());
    String s1 = lst.isEmpty() ? "" : encode(lst, true) + " ";

    State d = transition.destination();
    String s2 = "procedure ";
    String s3 = "";
    String specialize = "";
    
    if (!getIStateReference(d).equals("void")){
        if (d.isNumbered()){
            s2="procedure ";
            s3= "";
        }else{
            s2 = "function ";
            s3 = getlastPointpartfromString(getIStateReference(d));
            if (s3.contains("<") && s3.contains(">")){
              specialize = ":specialize ";
            }else{
                s3 = ":"+s3;
            }
        }
    }
    if (!prefix.isEmpty()){
        prefix=prefix+".";
    }
    return s1 + s2 + " "+prefix + ReplaceDollar(getIActionMethodSignature(transition, true, full))+specialize+s3;
  }
  
  @Override
   protected String getIActionMethodHead(Transition transition, boolean full) {
       return getIActionMethodHead(transition,full,"");
  }

   
protected void generateStateDecl(State state) {
    write("//generateStateDecl");
    writeLineBreak();
    
    // Class declaration
    
    writeClassDeclarationHead(
                getStateName(state),
                encode(state.typeParameters(), true));
    //writeTypeParameterDeclaration(state.typeParameters());
    //Superklasse
    write(getlastPointpartfromString(getIStateReference(state)));
    write(")");
    writeLineBreak();

    // Field declaration
    writeIndentation();
    write("private");
    writeLineBreak();
    
    writeIndentation();
    writeIndentation();
    writeActionDeclaration(state.diagram(),true);
    writeSemicolon();
    writeLineBreak();
    writeIndentation();
    write("public");
    writeLineBreak();
    
    // Constructor
    writeIndentation();
    writeIndentation(); // Einrückung
    write("Constructor Create(");
    writeActionDeclaration(state.diagram());
    write(")");
    writeSemicolon();
    
    
    // Method declaration
    for (Transition transition : state.transitions()) {
      writeIndentation();
      writeIndentation();
      
      write(getStateMethodHead(transition));
      //write("; virtual; abstract;");
      writeSemicolon();

    }
    writeIndentation();
    writeRightBracket();
  }

protected void generateStateImpl(State state) {
    write("//generateStateImpl");
    writeLineBreak();
    
    //Constructor implementation
    write("Constructor ");
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

      write(getStateMethodHead(transition,getStateName(state)));
      writeSemicolon();
      writeLeftBracket();
      writeIndentation();
      write(getStateMethodBodyListenerInvocation(transition));
      writeIndentation();
      write(getStateMethodBodyReturnNextState(transition));
      writeRightBracket();
      writeLineBreak();
          
    }
   
    //endFile();
  }

  @Override
  protected String getIStateQualifiedName(State state) {
    return  getIStateName(state); // Pascal has not the Qualified Names
  }

  @Override
  protected String getFilePath(String name) {
    return name.replace('.', '/') + "_fluent.pas";
  }
  
  protected void writeUnit(String name) {
    write( "unit " + name + "_fluent;\n\n");
  }
  
  @Override
  protected void writePackageDeclaration(String name) {
    write(name.isEmpty() ? "" : "unit " + name + ";\n\n");
  }

  
  protected void writeActionDeclaration(Diagram diagram,Boolean variable) {
    if (!variable){
      write("myaction:");
    }else{
      write("action:");
    }
    if (!diagram.typeParameters().isEmpty()){
      write("specialize "+getIActionQualifiedName(diagram)+encode(diagram.typeParameters(),false).replace(";", ","));
    }else{
      write(getIActionQualifiedName(diagram)); 
    }
  }
  
  @Override
  protected void writeActionDeclaration(Diagram diagram) {
    writeActionDeclaration(diagram,false);
  }
  @Override
  protected String getIActionQualifiedName(Diagram diagram) {
    return getIActionName(diagram); // Pascal has not the Qualified Names
  }  
  

  protected void write(String s,String pos) {
    stringBuilder.append("-");
    stringBuilder.append(pos);
    stringBuilder.append("-");
    stringBuilder.append(s);
  }
  
  @Override
  protected void writeLeftBracket(){
    write("Begin\n");
  }
  
  @Override  
  protected void writeRightBracket(){
    write("End;\n");
  }
  protected String getStateMethodreturn(Transition transition) {
    List<TypeParameter> p = transition.typeParameters();
    State d = transition.destination();
    Label l = transition.label();
    String s1 = getlastPointpartfromString(getIStateReference(d)) ;
    return s1;
  }  
  
  private String exportparams(FormalParameters p){
    System.out.println("silverchain.generator.GeneratorPascal.exportparams()");      
    String ausgabe="";
    for (FormalParameter f : p)  {
        System.out.println(f.type().toString());
        ausgabe= f.type().toString();
    }
    return ausgabe;
  }
  
  protected boolean writeStateMethodReturnUses(State s,boolean close,boolean addimpl,String unitname) {
    ArrayList<String> uses = new ArrayList<>();
    ArrayList<String> extuses = new ArrayList<>();
    for (Transition transition : s.transitions()) {
      State d = transition.destination();
      Label l = transition.label();
      
      
      //Object param[] = transition.label().method().parameters().formalParameters().stream().map(p->exportparams(p)).toArray();
      String param=exportparams(transition.label().method().parameters().formalParameters().get());
      //transition.label().method().parameters().formalParameters().toString());
      System.out.println("silverchain.generator.GeneratorPascal.writeStateMethodReturnUses()");
      System.out.println(param);
      extuses.add(param);

      //if (!param.contains("java.")) {
//        extuses.add(param);
//      }
      
      //Pascal verbindet die Units über USES - Rückgabetypen müssen beachtet werden.
      String[] extclasses = getIStateReference(d).split("\\.");
      if (extclasses.length > 1) {
          // es existieren eingezogene Klassen für die Rückgabeparameter
          if (!extclasses[0].contains("java.")) {
            if (!extuses.contains(extclasses[0])){
              extuses.add(extclasses[0]);
            }
          }
      }
      if (!getIStateName(d).contains("java.")) {
        if ((!unitname.equals(getIStateName(d))) && (!getIActionName(s.diagram()).equals(getIStateName(d))) && (!getIStateQualifiedName(s).equals(getIStateName(d)))){
          if ((!getIStateReference(d).equals("void"))&&(d.isNumbered())){
            
            if (!uses.contains(getIStateName(d))){
              uses.add(getIStateName(d));
            
            }
          }
        }
      }
    }
    Boolean first = true;
    if (close && !uses.isEmpty()){
        write("uses ");
    }
    for (String txt : uses) {
        if (!first){
            write(" , ");
        }
        write(txt+"_fluent");
        if (addimpl){
           write(","+txt+"impl_fluent");  
        }
        first = false;
    }
    for (String txt : extuses) {
        if (!first){
            write(" , ");
        }
        write(txt);
        first = false;
    }    
    
    if (close && !uses.isEmpty()){
        writeSemicolon();
    }
    return !first;
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
          s1="Integer";
      }
      return s2 + s3 +":"+ s1 ;
    }
    return parameter.name() ;
  }
       
  @Override
  protected String getIStateReference(State state) {
    if (state.isNumbered()) {
      if (state.typeParameters().isEmpty()){
        return getIStateQualifiedName(state) + encode(state.typeParameters(), false);    
      }else{
        return "specialize "+ getIStateQualifiedName(state) + encode(state.typeParameters(), false).replace(";", ",");
      }
    }
    return state.typeReference().map(l -> encode(l.typeReference())).orElse("void");
  }    

  
  protected String encode(Method method, boolean decl,String Generic) {
    String s = method.parameters().formalParameters().map( p -> encode(p, decl)).orElse("");
    s = method.name() +Generic.replace(";", ",")+ "(" + s + ")";
    if (decl) {
      s = s + method.exceptions().map(es -> " throws " + encode(es)).orElse("");
    }
    return s;
  }
  

  
  @Override  
  protected String getStateMethodHead(Transition transition) {
      return getStateMethodHead(transition,"");
  }
  
  protected String getStateMethodHead(Transition transition,String classname) {
    List<TypeParameter> p = transition.typeParameters();
    State d = transition.destination();
    Label l = transition.label();
    
    String pre = "function ";
    String s2 = getIStateName(d);
    if (!d.isNumbered()){
            s2=getlastPointpartfromString(getIStateReference(d));
    }
    List<TypeParameter> psub = d.typeParameters(); 
    if (!psub.isEmpty()){ //Rückgebewert ist ein Generic
        if (!d.isNumbered()){
            
            if (s2.contains("<") && s2.contains(">")){ //da ist etwas unklar in Silverchain.
                s2=":specialize "+s2;
            }else{
                s2=":"+s2;
            }
            
        }else{
            s2=":specialize "+s2+encode(psub, true).replace(";", ",");
        }
    }else{
        if (s2.contains("<") && s2.contains(">")){ //von Hand übergebene specialisierung
            s2=":specialize "+s2 ;
        }else{
            s2=":"+s2;
        }
    }
    
    if (getIStateReference(d).equals("void")){
        s2="";
        pre = "procedure ";
    }
    if (!classname.isEmpty()){
        classname=classname+".";
    }
    
    //String s2 = getIStateReference(d) + " " + encode(l.method(), true);
    if (p.isEmpty()){
     return pre+classname+encode(l.method(), true)+s2;
     // return encode(l.method(), true);
    }else{
     return "generic "+pre+classname+encode(l.method(), true,encode(p, true).replace(";", ",")).replace(" extends ", ":") +s2;
     
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
        return "    " + s2 + "Self.action." + ReplaceDollar(getIActionMethodInvocation(transition, true)).replace(";",",") + ";\n";
    }else{
        return "    " + s1 + s2 + "(Self.action." + ReplaceDollar(getIActionMethodInvocation(transition, true)).replace(";",",") + ");\n";
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
        return  getIActionMethodInvocation(transition, false).replace(";", ",") + ";";
    }else{
        return s +"("+ getIActionMethodInvocation(transition, false) + ");";
    }
  }
  
  
  @Override 
  protected String getStateQualifiedName(State state) {
    return  getStateName(state);
  }
  
  @Override
  protected void endFile() {
    write("End.");
    writeLineBreak();
    super.endFile();
    
  }
  private String ReplaceDollar(String in){
      return (in.replace("$","_"));
  }
  
}
