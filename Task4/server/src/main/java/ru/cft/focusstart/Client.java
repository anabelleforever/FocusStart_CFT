package ru.cft.focusstart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.cft.focusstart.dto.Notification;

import java.io.*;
import java.net.Socket;

import static ru.cft.focusstart.ClientManager.getClientManager;

class Client implements Runnable {
    private Socket clientSocket;
    private ObjectMapper mapper = new ObjectMapper();
    private String userName;
    private Thread thread;
//    private Logger log = Logger.getLogger("Client: ");

    Client(Socket socket) {
        clientSocket = socket;
        mapper.registerModule(new JavaTimeModule());
        thread = new Thread(this);
        thread.start();
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    String getUserName() {
        return userName;
    }

    Socket getClientSocket() {
        return clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            while (!thread.isInterrupted()) {
                Notification notification = null;
                if (reader.ready()) {
                    notification = mapper.readValue(reader.readLine(), Notification.class);
                }
                if (notification != null) {
                    getClientManager().parseNotification(notification, this);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    thread.interrupt();
                }
            }
            //описать все ошибки текстом
        } catch (JsonMappingException e) {
//            log.error(e.getMessage());
            System.out.println("ошибка маппинга реквеста");
        } catch (JsonProcessingException e) {
//            log.error(e.getMessage());
            System.out.println("не io ошибка json");
        } catch (IOException e) {
//            log.error(e.getMessage());
            System.out.println("io ошибка json");
        }
    }

    void notify(Notification notification) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
            writer.write(mapper.writeValueAsString(notification));
            writer.write(System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
//            log.error("Ошибка при отправке сообщения. " + e.getMessage());
            System.out.println("Ошибка при отправке сообщения. ");
        }
    }

    void stop() {
        thread.interrupt();
        try {
            clientSocket.close();
        } catch (IOException e) {
//            log.error("Ошибка при закрытии сокета. " + e.getMessage());
            System.out.println("Ошибка при закрытии сокета. ");
        }
    }
}
