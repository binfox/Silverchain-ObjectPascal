unit TFluentStringList_fluent;
{$interfaces CORBA}
Interface
uses classes;

type
//Forward decleration
  TFluentStringList0 = Interface;
  TFluentStringList0Impl = Class;
  TFluentStringList1 = Interface;
  TFluentStringList1Impl = Class;

//writeIActionDecl
  TFluentStringListAction= Class Abstract
    procedure  state0_add( s:String);
    procedure  state1_add( s:String);
    function  state1_get():tstringlist;
    procedure  state1_savetofile( s:String);
    procedure  add( s:String); virtual; abstract;
    function  get():tstringlist; virtual; abstract;
    procedure  savetofile( s:String); virtual; abstract;
  End;
//writeIStateDecl
  TFluentStringList0= interface
    function add( s:String):TFluentStringList1;
  End;
//writeStateDecl
  TFluentStringList0Impl= Class(TInterfacedObject,TFluentStringList0)
  private
    action:TFluentStringListAction;

  public
    Constructor Create(myaction:TFluentStringListAction);
    function add( s:String):TFluentStringList1;
  End;

//writeIStateDecl
  TFluentStringList1= interface
    function add( s:String):TFluentStringList1;
    function get():tstringlist;
    procedure savetofile( s:String);
  End;
//writeStateDecl
  TFluentStringList1Impl= Class(TInterfacedObject,TFluentStringList1)
  private
    action:TFluentStringListAction;

  public
    Constructor Create(myaction:TFluentStringListAction);
    function add( s:String):TFluentStringList1;
    function get():tstringlist;
    procedure savetofile( s:String);
  End;



Implementation
//writeStateImpl
Constructor TFluentStringList0Impl.Create(myaction:TFluentStringListAction);
Begin
  Self.action := myaction;
End;

function TFluentStringList0Impl.add( s:String):TFluentStringList1;
Begin
      Self.action.state0_add(s);
      exit( TFluentStringList1Impl.Create(Self.action));
End;


//writeStateImpl
Constructor TFluentStringList1Impl.Create(myaction:TFluentStringListAction);
Begin
  Self.action := myaction;
End;

function TFluentStringList1Impl.add( s:String):TFluentStringList1;
Begin
      Self.action.state1_add(s);
      exit( TFluentStringList1Impl.Create(Self.action));
End;

function TFluentStringList1Impl.get():tstringlist;
Begin
      exit (Self.action.state1_get());
  End;

procedure TFluentStringList1Impl.savetofile( s:String);
Begin
      Self.action.state1_savetofile(s);
  End;


//writeIActionImpl
procedure  TFluentStringListAction.state0_add( s:String);
Begin
  add(s);
End;

procedure  TFluentStringListAction.state1_add( s:String);
Begin
  add(s);
End;

function  TFluentStringListAction.state1_get():tstringlist;
Begin
  exit (get());
End;

procedure  TFluentStringListAction.state1_savetofile( s:String);
Begin
  savetofile(s);
End;


End.
