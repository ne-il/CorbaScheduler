package jobs_client_server.impl;

import jobs.APOA;
import jobs.Struct1;
import jobs.Struct1Helper;
import org.omg.CORBA.Any;

public class AImpl1 extends APOA {

    @Override
    public void run(Any m){
        Struct1 struct1;
        System.out.println("On lance A1!!!!!");
        if(Struct1Helper.type().kind().value() == m.type().kind().value()) {
            struct1 = Struct1Helper.extract(m);
            try {
                Thread.sleep(struct1.time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else
            throw new IllegalArgumentException("AImpl1 : Not a struct1");
    }

}
