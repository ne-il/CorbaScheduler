package jobs_client_server;

import jobs_client_server.impl.AImpl1;
import jobs_client_server.impl.AImpl2;
import jobs_client_server.impl.BImpl1;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Object;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

public class Server {

    public static void main(String[] args) throws InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, CannotProceed, AlreadyBound, NotFound, ServantAlreadyActive, WrongPolicy, ObjectNotActive, AdapterInactive {

        ORB orb = ORB.init(args, null);

        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        //server name defined
        NamingContextExt namingContext = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
        NamingContext aContext;
        NamingContext bContext;
        NamingContext ordContext;


        //servant objects creation
        //AImpl1
        AImpl1 aImpl1 = new AImpl1();
        byte[] aImpl1Id = rootPOA.activate_object(aImpl1);
        org.omg.CORBA.Object aImpl1Ref = rootPOA.id_to_reference(aImpl1Id);

        //AImpl2
        AImpl2 aImpl2 = new AImpl2();
        byte[] aImpl2Id = rootPOA.activate_object(aImpl2);
        org.omg.CORBA.Object aImpl2Ref = rootPOA.id_to_reference(aImpl2Id);

        //BImpl1
        BImpl1 bImpl1 = new BImpl1();
        byte[] bImpl1Id = rootPOA.activate_object(bImpl1);
        org.omg.CORBA.Object bImpl1Ref = rootPOA.id_to_reference(bImpl1Id);

        //interface names defined



        //A namespace
        try {
            aContext = namingContext.bind_new_context(namingContext.to_name("A"));

        } catch(AlreadyBound alreadyBound) {
            aContext = NamingContextHelper.narrow(namingContext.resolve(namingContext.to_name("A")));
        }

        //B namespace
        try {
            bContext = namingContext.bind_new_context(namingContext.to_name("B"));

        } catch(AlreadyBound alreadyBound) {
            bContext = NamingContextHelper.narrow(namingContext.resolve(namingContext.to_name("B")));
        }

        //servants namespaces
        //A1
        try {
            aContext.bind(namingContext.to_name("A1"), aImpl1Ref);
        } catch (AlreadyBound alreadyBound) {
            aContext.rebind(namingContext.to_name("A1"), aImpl1Ref);
        }

        //A2
        try {
            aContext.bind(namingContext.to_name("A2"), aImpl2Ref);
        } catch (AlreadyBound alreadyBound) {
            aContext.rebind(namingContext.to_name("A2"), aImpl2Ref);
        }

        //B1
        try {
            bContext.bind(namingContext.to_name("B1"), bImpl1Ref);
        } catch (AlreadyBound alreadyBound) {
            bContext.rebind(namingContext.to_name("B1"), bImpl1Ref);
        }

        //ordonanceur


        //creation of the scheduler
        Ordonanceur ordonanceur = new Ordonanceur(namingContext);
        byte[] ordonanceurId = rootPOA.activate_object(ordonanceur);
        Object ordonanceurRef = rootPOA.id_to_reference(ordonanceurId);

        try {
            ordContext = namingContext.bind_new_context(namingContext.to_name("ORD"));
        } catch (AlreadyBound alreadyBound) {
            ordContext = NamingContextHelper.narrow(namingContext.resolve(namingContext.to_name("ORD")));
        }
        try {
            ordContext.bind(namingContext.to_name("ordObject"),ordonanceurRef);
        } catch (AlreadyBound alreadyBound) {
            ordContext.rebind(namingContext.to_name("ordObject"),ordonanceurRef);
        }

        //server launch
        rootPOA.the_POAManager().activate();
        System.out.println("server now running...");
        orb.run();
    }

}
