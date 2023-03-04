import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Tema2 {
    public static AtomicInteger leftTasks = new AtomicInteger(0);
    public static void main(String[] args) {
        String folderName = args[0];
        int P = Integer.parseInt(args[1]);
        // Deschiderea fisierelor de input
        File ordersFile = new File(folderName + "/orders.txt");
        File productsFile = new File(folderName + "/order_products.txt");

        // Crearea pool-ului de P thread-uri de nivel 1 ce vor solutiona comenzile
        ExecutorService tpe = Executors.newFixedThreadPool(P);
        ExecutorService secondaryTpe = Executors.newFixedThreadPool(P);
        File orders_out;
        File products_out;
        try {
            // Creare fisier orders_out.txt si deschiderea lui pentru scriere
            orders_out = new File("orders_out.txt");
            orders_out.createNewFile();
            BufferedWriter ordersStatusWriter = new BufferedWriter(new FileWriter(orders_out));


            // Creare fisier order_products_out.txt si deschiderea lui pentru scriere
            products_out = new File("order_products_out.txt");
            products_out.createNewFile();
            BufferedWriter productsStatusWriter = new BufferedWriter(new FileWriter(products_out));

            BufferedReader reader = new BufferedReader(new FileReader(ordersFile));
            // Introducerea a P task-uri in pool pentru ca cele P thread-uri de nivel 1
            // sa inceapa prelucrarea comenzilor
            for(int i = 0 ; i < P ; i++){
                tpe.submit(new OrderTask(ordersFile, productsFile, tpe, secondaryTpe, reader,
                        productsStatusWriter, ordersStatusWriter));
            }

        } catch (IOException e) {
           System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
