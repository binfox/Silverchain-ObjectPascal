// Easy Example of a Fluent Map builder.
// Jan Lahr-Kuhnert - See TFluentMap.ag for the structure
program fluent_interfaces_1;

{$interfaces CORBA}

uses TFluentMap_fluent,fgl,SysUtils ;

type
   MyMap = specialize TFPGMap<String, Integer>; // Specialize the map formate, i user String Key and a Integer Value.

   // the Fluent interface uses String as key.
   FluentMap0Impl =specialize TFluentMap0Impl<Integer>;
   FluentMapAction=specialize TFluentMapAction<Integer>;

   // Your two needed Classes
   FluentMap = class(FluentMap0Impl)
     constructor Create;
   end;

   FluentMapActionImpl = class(FluentMapAction )
     private
       // this is the map, we build
       map:mymap;
     public
       // the functions from the FluentMapAction<v> Class --> override
       function  build():specialize TFPGMap<String, integer>; override;
       procedure  print();                                    override;
       procedure  put( key:String;  value:integer);           override;
   end;



//FluentMap
Constructor FluentMap.Create;
var
  myaction:FluentMapActionImpl;
begin
  myaction:=FluentMapActionImpl.Create;
  Inherited create(myaction);
end;

//FluentMapActionImpl
procedure  FluentMapActionImpl.put( key:String;  value:Integer);
begin
  if map = nil then map := mymap.create;
  map.Add(key,value);
end;

function FluentMapActionImpl.build():specialize TFPGMap<String, integer>;
begin
  build := map;
end;

procedure FluentMapActionImpl.print();
var
  i:integer;
begin
  for i:=0 to map.Count-1 do
  begin
    writeln(map.Keys[i]+':'+inttostr(map.Data[i]));
  end;
end;

var
  Map:MyMap;
  i:integer;
begin

  // Easy Example of a Fluent Map builder.

  writeln('print() Function');
  FluentMap.create.put('uno',1).put('Zwei',2).put('Three',3).put('quattro',4).print();

  writeln(); // Seperate the output

  writeln('build() Function');
  Map:=FluentMap.create.put('uno',1).put('Zwei',2).put('three',3).put('quattro',4).build();
  writeln(map.ToString+':');
  for i:=0 to map.Count-1 do
  begin
    writeln(map.Keys[i]+':'+inttostr(map.Data[i]));
  end;

  readln();

end.



