// Easy Example of a Fluent String List.
// See TFluentStringList.ag for the structure
// Jan Lahr-Kuhnert 2022
program Fluent_Stringlist_example;

uses TFluentStringList_fluent,classes;

// Your two needed Classes
type
  TFluentStringList = class (TFluentStringList0Impl)
    constructor Create;
  end;

  TFluentStringListActionImpl = class(TFluentStringListAction)
     private
      Liste:Tstringlist;
     public
      procedure  add( s:String);                override;
      function   get():tstringlist;             override;
      procedure  savetofile( s:String);         override;
    end;

//TFluentStringList
Constructor  TFluentStringList.Create;
var
  myaction: TFluentStringListActionImpl;
begin
  myaction:= TFluentStringListActionImpl.Create;
  Inherited create(myaction);
end;

//TFluentStringListActionImpl
procedure  TFluentStringListActionImpl.add( s:String);
begin
  if Liste= nil then liste:= TStringList.create;
  liste.Add(s);
end;

function   TFluentStringListActionImpl.get():tstringlist;
begin
  get := liste;
end;

procedure  TFluentStringListActionImpl.savetofile( s:String);
begin
  liste.SaveToFile(s);
end;

begin
 // Easy Example of a Fluent String List.
 // Create a Stringlist an save it
 TFluentStringList.create.add('Hello').add('World').savetofile('temp.txt');

 // Create a Stringlist and get it as a normal TStringlist
 writeln(TFluentStringList.create.add('Hello').add(' ').add('World').get().CommaText);

 readln();
end.

