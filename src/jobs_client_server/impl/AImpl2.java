package jobs_client_server.impl;

import jobs.APOA;
import jobs.Struct2;
import jobs.Struct2Helper;
import org.omg.CORBA.Any;

public class AImpl2 extends APOA {

    @Override
    public void run(Any m){
        Struct2 struct2;
        System.out.println("On lance A2 !!!!");
        if(Struct2Helper.type().kind().value() == m.type().kind().value()) {
            struct2 = Struct2Helper.extract(m);
            try {
                Thread.sleep(struct2.time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else
            throw new IllegalArgumentException("AImpl2 : Not a struct2");

    }

}
