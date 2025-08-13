/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hu.mosomate.twitcheventhub.utils.services;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 * Manager class for dispatching EventSub messages as UDP packages.
 * 
 * @author mosomate
 */
public class UdpDispatchManager {
    
    private static final Logger logger = Logger.getLogger(UdpDispatchManager.class.getName());
    
    /**
     * Background thread for running the message queue.
     */
    private volatile SwingWorker<Void, Object> worker;
    
    /**
     * Called when the service has started or stopped.
     */
    private final ActionListener listener;
    
    /**
     * Properly synchronized queue for the messages to be dispatched. 
     */
    private final LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    
    public UdpDispatchManager(ActionListener listener) {
        this.listener = listener;
    }

    /**
     * Puts a message into the queue for dispatching.
     * 
     * @param message the message to be dispatched
     * @return true is message sending was successful
     */
    public boolean sendMessage(String message) {
        if (worker == null || worker.isDone()) {
            return false;
        }
        
        return messageQueue.offer(message);
    }
    
    /**
     * Starts the service for the given ports.
     * 
     * @param ports ports to dispatch messages to
     */
    public void start(List<Integer> ports) {
        if (worker != null && !worker.isDone()) {
            return;
        }

        // Clear message queue
        messageQueue.clear();
        
        // Start new worker
        worker = new SwingWorker<Void, Object>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (DatagramSocket socket = new DatagramSocket()) {
                    InetAddress address = InetAddress.getLocalHost();

                    // Indication of service start
                    publish();

                    while (!isCancelled()) {
                        String message = messageQueue.take();

                        try {
                            byte[] buffer = message.getBytes();

                            // Send to all ports
                            for (int port : ports) {
                                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                                socket.send(packet);
                            }
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }

                return null;
            }

            @Override
            protected void done() {
                listener.onUdpMessageSenderStopped();
            }

            @Override
            protected void process(List<Object> chunks) {
                // Processing has started
                listener.onUdpMessageSenderStarted();
            }
        };
        
        // Start service
        worker.execute();
    }
    
    public void stop() {
        if (worker == null || worker.isDone()) {
            return;
        }
        
        worker.cancel(true);
    }
    
    public boolean isRunning() {
        return worker != null && !worker.isDone();
    }
    
    public interface ActionListener {
        void onUdpMessageSenderStarted();
        void onUdpMessageSenderStopped();
    }
}
