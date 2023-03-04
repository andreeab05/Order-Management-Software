import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductTask implements Runnable{
    File productsFile;
    ExecutorService tpe;
    CountDownLatch waiter;
    BufferedReader reader;
    String orderID;
    Integer productIndex;
    BufferedWriter productsStatusWriter;

    public ProductTask(File productsFile, ExecutorService tpe, BufferedReader reader,
                       BufferedWriter productsStatusWriter, CountDownLatch waiter,
                       Integer productIndex, String orderID) {
        this.productsFile = productsFile;
        this.tpe = tpe;
        this.reader = reader;
        this.waiter = waiter;
        this.orderID = orderID;
        this.productIndex = productIndex;
        this.productsStatusWriter = productsStatusWriter;
    }

    @Override
    public void run() {
        String product = null;
        try {
            int aux = 1;
            // Cautarea in fisierul de produse a produsului cu index-ul productIndex
            // si "prelucrarea" lui (scrierea in fisierul de output)
            while ((product = reader.readLine()) != null) {
                if (product.contains(orderID)) {
                    if (aux == productIndex) {
                        productsStatusWriter.write(product + ",shipped\n");
                        break;
                    } else
                        aux++;
                }
            }
            waiter.countDown();
            reader.close();
        } catch (IOException e) {
            System.out.println("Eroare!");
            e.printStackTrace();
        }
    }
}