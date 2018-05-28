package jobs_client_server.impl;

import jobs.*;
import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
/*
Can receive as argument long, string, struct1 or struct2
 */
public class BImpl1 extends BPOA {

    @Override
    public void run(Any m) {
        Struct1 struct1;
        System.out.println("B1 !!");
        if (Struct1Helper.type().kind().value() == m.type().kind().value()) {
            struct1 = Struct1Helper.extract(m);
            try {
                Thread.sleep(struct1.time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else
            throw new IllegalArgumentException("BImpl1 : Not a struct1");
    }

    @Override
    public Any echo(Any inputArg) {
        System.out.println("On lance B1 !!!!");

        if (Struct1Helper.type().kind().value() == inputArg.type().kind().value()) {
            Struct1 extractedStruct1 = Struct1Helper.extract(inputArg);

            Any returnValue = _orb().create_any();
            returnValue.type(Struct1Helper.type());
            Struct1Helper.insert(returnValue, extractedStruct1);

            try {
                Thread.sleep(extractedStruct1.time);
                return returnValue;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        else if (Struct2Helper.type().kind().value() == inputArg.type().kind().value()) {
            Struct2 extractedStruct2 = Struct2Helper.extract(inputArg);

            Any returnValue = _orb().create_any();
            returnValue.type(Struct2Helper.type());
            Struct2Helper.insert(returnValue, extractedStruct2);

            try {
                Thread.sleep(extractedStruct2.time);
                return returnValue;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        else if (TCKind._tk_string == inputArg.type().kind().value()) {
            String extractedString1 = inputArg.extract_string();

            Any returnValue = _orb().create_any();
            returnValue.type(_orb().get_primitive_tc(TCKind.tk_string));
            returnValue.insert_string(extractedString1);

            try {
                Thread.sleep(5000);
                return returnValue;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else if (TCKind._tk_long == inputArg.type().kind().value()) {
            int extractedLong = inputArg.extract_long();

            Any returnValue = _orb().create_any();
            returnValue.type(_orb().get_primitive_tc(TCKind.tk_long));
            returnValue.insert_long(extractedLong);
            try {
                Thread.sleep(5000);
                return returnValue;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else
            throw new IllegalArgumentException("BImpl1 : Not a struct1");

        return null;
    }

}
