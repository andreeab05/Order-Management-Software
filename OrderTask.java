import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderTask implements Runnable {
    File ordersFile;
    File productsFile;
    ExecutorService tpe;
    ExecutorService secondaryTPE;
    BufferedReader reader;
    BufferedWriter productsStatusWriter;
    BufferedWriter ordersStatusWriter;

    public OrderTask(File ordersFile, File productsFile, ExecutorService tpe, ExecutorService secondaryTPE,
                     BufferedReader reader, BufferedWriter productsStatusWriter, BufferedWriter ordersStatusWriter) {
        this.ordersFile = ordersFile;
        this.productsFile = productsFile;
        this.tpe = tpe;
        this.reader = reader;
        this.secondaryTPE = secondaryTPE;
        this.productsStatusWriter = productsStatusWriter;
        this.ordersStatusWriter = ordersStatusWriter;
    }

    @Override
    public void run() {
        String nextOrder;
        String currentOrder;
        int left;
        try {
            // Se citeste o linie noua din fisierul de comenzi
            Tema2.leftTasks.incrementAndGet();
            if ((currentOrder = reader.readLine()) == null)
            {
                left = Tema2.leftTasks.decrementAndGet();
                return;
            }
            else
            {
                String[] aux = currentOrder.split(",");
                String orderID = aux[0];
                int noProducts = Integer.parseInt(aux[1]);
                // Daca exista o comanda si numarul de produse din aceasta e mai mare decat 0,
                // se adauga in pool-ul de thread-uri de nivel 2 noProducts task-uri
                if (noProducts > 0)
                {
                    int counter = 1;
                    CountDownLatch waiter = new CountDownLatch(noProducts);
                    while (counter <= noProducts)
                    {
                        BufferedReader productReader = new BufferedReader(new FileReader(productsFile));
                        // Fiecare task va primi index-ul (valoarea lui counter)
                        // produsului din comanda
                        secondaryTPE.submit(new ProductTask(productsFile, secondaryTPE,
                                productReader, productsStatusWriter, waiter, counter, orderID));
                        counter++;
                    }

                    // Sincronizarea thread-ului de nivel 1 cu cele de nivel 2
                    // Thread-ul de nivel 1 nu poate prelua alta comanda pana
                    // la finalizarea comenzii curente
                    waiter.await();
                    ordersStatusWriter.write(currentOrder + ",shipped\n");
                }
                left = Tema2.leftTasks.decrementAndGet();

                // Dupa finalizarea comenzii curente, thread-ul adauga un nou task pentru urmatoarea
                // comanda
                tpe.submit(new OrderTask(ordersFile, productsFile, tpe, secondaryTPE, reader, productsStatusWriter, ordersStatusWriter));
            }
            // Daca nu mai sunt task-uri de nivel 1 in pool, se inchid atat pool-urile de nivel 1 si 2,
            // cat si fisierele de output si fisierul de comenzi
            //System.out.println(left);
            if(left == 0) {
                tpe.shutdown();
                secondaryTPE.shutdown();
                ordersStatusWriter.close();
                productsStatusWriter.close();
                reader.close();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
