unit TFluentMap_fluent;
{$interfaces CORBA}
Interface
uses fgl;


type

//Foreward decleration
  generic TFluentMap0<V>= Interface;

  generic TFluentMap0Impl<V>= Class
  End;

//writeIActionDecl
  generic TFluentMapAction<V>= Class Abstract
    function  state0_build():specialize TFPGMap<String, V>;
    procedure  state0_print();
    procedure  state0_put( key:String;  value:V);
    function  build():specialize TFPGMap<String, V>; virtual; abstract;
    procedure  print(); virtual; abstract;
    procedure  put( key:String;  value:V); virtual; abstract;
  End;

//writeIStateDecl
  generic TFluentMap0<V>= interface
    function build():specialize TFPGMap<String, V>;
    procedure print();
    function put( key:String;  value:V):specialize TFluentMap0<V>;
  End;

//writeStateDecl
  generic TFluentMap0Impl<V>= Class(TInterfacedObject,specialize TFluentMap0<V>)
  private
    action:specialize TFluentMapAction<V>;

  public
    Constructor Create(myaction:specialize TFluentMapAction<V>);
    function build():specialize TFPGMap<String, V>;
    procedure print();
    function put( key:String;  value:V):specialize TFluentMap0<V>;
  End;

implementation

//writeStateImpl
Constructor TFluentMap0Impl.Create(myaction:specialize TFluentMapAction<V>);
Begin
  Self.action := myaction;
End;

function TFluentMap0Impl.build():specialize TFPGMap<String, V>;
Begin
      exit (Self.action.state0_build());
  End;

procedure TFluentMap0Impl.print();
Begin
      Self.action.state0_print();
  End;

function TFluentMap0Impl.put( key:String;  value:V):specialize TFluentMap0<V>;
Begin
      Self.action.state0_put(key, value);
      exit( TFluentMap0Impl.Create(Self.action));
End;


//writeIActionImpl
function  TFluentMapAction.state0_build():specialize TFPGMap<String, V>;
Begin
  exit (build());
End;

procedure  TFluentMapAction.state0_print();
Begin
  print();
End;

procedure  TFluentMapAction.state0_put( key:String;  value:V);
Begin
  put(key, value);
End;


End.
