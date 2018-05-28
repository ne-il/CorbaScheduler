package jobs_client_server;

import jobs.*;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class Client {

    public static final int NO_RETURN_VALUE = -999999;

    public static void main(String args[]) throws InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, NotFound, CannotProceed {

        ORB orb = ORB.init(args, null);
        NamingContextExt namingContextExt = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
        NameComponent[] name = namingContextExt.to_name("ORD/ordObject");
        GSched ordonanceur = GSchedHelper.narrow(namingContextExt.resolve(name));
        RequestDescriptor request;


        Any anyStruct = orb.create_any();
        Any[] i = new Any[1];
        Struct1 struct = new Struct1(5000);
        Struct1Helper.insert(anyStruct, struct);
        i[0] = anyStruct;


        request = new RequestDescriptor("A", "", "run", i, NO_RETURN_VALUE);
        ordonanceur.sendRequest(request);
        ordonanceur.sendRequest(request);
        ordonanceur.sendRequest(request);
        ordonanceur.sendRequest(request);
        ordonanceur.sendRequest(request);


        Any anyString = orb.create_any();
        anyString.insert_string("Ceci est l'argument d entree");
        i = new Any[1];
        i[0] = anyString;
        request = new RequestDescriptor("B", "B1", "echo", i, TCKind._tk_string);
        ordonanceur.sendRequest(request);


        Any anyLong = orb.create_any();
        anyLong.insert_long(999);
        i = new Any[1];
        i[0] = anyLong;
        request = new RequestDescriptor("B", "B1", "echo", i, TCKind._tk_long);
        ordonanceur.sendRequest(request);


        Any anyStruct1 = orb.create_any();
        i = new Any[1];
        Struct1 struct1 = new Struct1(5000);
        Struct1Helper.insert(anyStruct1, struct1);
        i[0] = anyStruct1;
        request = new RequestDescriptor("B", "B1", "echo", i, Struct1Helper.type().kind().value());
        ordonanceur.sendRequest(request);
    }
}
