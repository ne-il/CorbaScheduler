package jobs_client_server;

import jobs.*;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.util.*;

import static jobs_client_server.Client.NO_RETURN_VALUE;

public class Ordonanceur extends GSchedPOA {

    private class Worker {

        private final Queue<org.omg.CORBA.Request> queue;
        private final Object object;

        private Worker(Object object) {
            this.queue = new ArrayDeque<>();
            this.object = object;
        }

        public void addRequest(String method, Any[] anys, int returnType) {

            org.omg.CORBA.Request request = object._request(method);

//            On ajoute le parametre d'entree (peu importe ce qu'il y a a l'interieur du Any)
            for (Any a : anys)
                request.add_in_arg().insert_any(a);

//            On definie le type de retour
            if(returnType != NO_RETURN_VALUE) {
                if(returnType == Struct1Helper.type().kind().value()) {
                    request.set_return_type(Struct1Helper.type());
                }
                else if(returnType == Struct2Helper.type().kind().value()) {
                    request.set_return_type(Struct2Helper.type());
                }
                else if (returnType == TCKind._tk_string) {
                    TypeCode tc_string = _orb().create_string_tc(250);
                    request.set_return_type(tc_string);
                }

                else if (returnType == TCKind._tk_long) {
                    TypeCode tc_long = _orb().get_primitive_tc(TCKind.tk_long);
                    request.set_return_type(tc_long);
                }
            }

            queue.add(request);

            if (queue.size() == 1)
                request.send_deferred();

        }

        public int size() {
            return queue.size();
        }

        public void sendIfFinished() {

            if(queue.isEmpty())
                return;
            org.omg.CORBA.Request request = queue.peek();
//            System.out.println("waiting for response ...");
            if (request.poll_response()) {
                System.out.println("Worker " + object.getClass().toGenericString() + " is finished");
                try {

                    request.get_response();

                    //Add this line if we want to return the value to the client
                    Any response = request.return_value();
                    if(response.type().kind().value() == Struct1Helper.type().kind().value()) {
                        if(Struct1Helper.type().equal(response.type())){
                            Struct1 s1 = Struct1Helper.extract(response);
                            System.out.println("Voici le struct1.time qu on a extrait: " + s1.time);
                        }
                        else if(Struct2Helper.type().equal(response.type())){
                            Struct2 s2 = Struct2Helper.extract(response);
                            System.out.println("Voici le struct2.time qu on a extrait: " + s2.time);
                        }
                    }

                    else if(response.type().kind().value() == TCKind._tk_string) {
                        String extractedString = response.extract_string();
                        System.out.println("Voici le string qu on a extrait: " + extractedString );
                    }
                    else if(response.type().kind().value() == TCKind._tk_long) {
                        int extractedLong = response.extract_long();
                        System.out.println("Voici le Long qu on a extrait: " + extractedLong);
                    }

                    else{
                        System.out.println("Un job sans valeur de retour s'est fini");

                    }

                } catch (WrongTransaction wrongTransaction) {
                    wrongTransaction.printStackTrace();
                }

                queue.poll();
                if(queue.size() != 0) {
                    queue.peek().send_deferred();
                    System.out.println("Sending new request...");
                }
//            } else {
//                System.out.println("Not done !");
            }


        }

    }


    private final NamingContextExt serviceName;
    private final HashMap<String, Worker> proxyHashMap;


    private Thread listenerThread;

    public Ordonanceur(NamingContextExt serviceName) {
        this.serviceName = serviceName;
        this.proxyHashMap = explore("", serviceName);

        listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Set<String> keys = proxyHashMap.keySet();
                while (true) {
                    for (String k : keys) {
                        Worker worker = proxyHashMap.get(k);
                        //prints every 10 ms the current size of the current worker's stack for debugging
//                        System.out.println(k + " " + worker.size());
                        worker.sendIfFinished();
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        listenerThread.start();
    }


    @Override
    public void sendRequest(RequestDescriptor req) {
        String className = req._class;
        String interfaceName = req.interf;
        String methodName = req.method;
        org.omg.CORBA.Any params[] = req.params;
        int returnType = req.return_value;

        if (className != null && !className.equals("")) {
            Worker w = proxyHashMap.get(interfaceName + "/" + className);
            w.addRequest(methodName, params, returnType);
        }
        else if(className == null || className.equals("")) {
            int min = Integer.MAX_VALUE;
            String key = "";
            for (String k: proxyHashMap.keySet()) {
                if(k.contains(interfaceName + "/") && proxyHashMap.get(k).queue.size() < min) {
                    min = proxyHashMap.get(k).queue.size();
                    key = k;
                }
            }
            if(key.equals("")) {
                System.out.println("no suitable worker found");
            } else {
                Worker w = proxyHashMap.get(key);
                w.addRequest(methodName, params, returnType);
            }
        }
    }

    private HashMap<String, Worker> explore(String name, NamingContextExt context) {

        BindingListHolder bindingListHolder = new BindingListHolder();
        BindingIteratorHolder bindingIteratorHolder = new BindingIteratorHolder();
        context.list(10, bindingListHolder, bindingIteratorHolder);
        Binding[] holders = bindingListHolder.value;

        HashMap<String, Worker> map = new HashMap<>();

        for (int i = 0; i < holders.length; i++) {
            if (holders[i].binding_name[0].id.equals("ORD"))
                continue;
            if (holders[i].binding_type == BindingType.ncontext) {
                System.out.println("folder : " + holders[i].binding_name[0].id);
                try {
                    map.putAll(explore(holders[i].binding_name[0].id + "/", NamingContextExtHelper.narrow(context.resolve(holders[i].binding_name))));
                } catch (NotFound | CannotProceed | InvalidName notFound) {
                    notFound.printStackTrace();
                    return (HashMap<String, Worker>) Collections.EMPTY_MAP;
                }
            } else {
                String path = name + holders[i].binding_name[0].id;
                System.out.println("file : " + path);
                try {
                    map.put(path, new Worker(context.resolve(holders[i].binding_name)));
                } catch (NotFound | CannotProceed | InvalidName invalidName) {
                    invalidName.printStackTrace();
                }
            }
        }
        return map;
    }

}
