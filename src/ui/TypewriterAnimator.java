package ui;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.*;
import javax.swing.text.*;

/**
 * Animates text in a typewriter style with optional color support.
 * Queues messages in FIFO order and displays them sequentially.
 * Provides methods to check animation state, get partial text, or instantly finish messages.
 */
public class TypewriterAnimator {

    // ====== CONFIGURATION ======
    private static final int TYPEWRITER_DELAY = 20; // ms per character

    // ====== DEPENDENCIES ======
    private final Runnable onUpdate;
    private final Runnable onScrollRequest;

    // ====== STATE ======
    private final java.util.List<ColoredMessage> completedMessages = new java.util.ArrayList<>();
    private String currentMessage = "";
    private Color currentColor = null;
    private int typingIndex = 0;
    private boolean isTyping = false;
    private Timer typewriterTimer;

    // ====== QUEUE ======
    public static class ColoredMessage {
        public final String text;
        public final Color color;

        public ColoredMessage(String text, Color color) {
            this.text = text;
            this.color = color;
        }
    }

    private final Queue<ColoredMessage> messageQueue = new LinkedList<>();
    private boolean processingQueue = false;

    // ====== CONSTRUCTOR ======
    public TypewriterAnimator(StyledDocument document, Runnable onUpdate, Runnable onScrollRequest) {
        this.onUpdate = onUpdate;
        this.onScrollRequest = onScrollRequest;
    }

    // ====== PUBLIC API ======
    public void queueMessage(String message, Color color) {
        String msg = message.trim();
        if (msg.isEmpty()) return;

        if (isAnimationActive()) instantCompleteCurrentMessage();
        messageQueue.offer(new ColoredMessage(msg, color));
        if (!processingQueue) processNextMessage();
    }

    public void clear() {
        stopTyping();
        messageQueue.clear();
        completedMessages.clear();
        processingQueue = false;
    }

    public boolean isTyping() { return isTyping; }
    public int getQueueSize() { return messageQueue.size(); }
    
    /**
     * Returns all completed messages (excluding the currently typing message).
     */
    public java.util.List<ColoredMessage> getCompletedMessages() { 
        return java.util.Collections.unmodifiableList(completedMessages); 
    }
    
    /**
     * Replaces all completed messages with a new list.
     * Used for message cleanup (e.g., limiting to MAX_MESSAGES).
     */
    public void setCompletedMessages(java.util.List<ColoredMessage> messages) {
        completedMessages.clear();
        completedMessages.addAll(messages);
    }

    public String getCurrentDisplayText() {
        StringBuilder sb = new StringBuilder();
        for (ColoredMessage msg : completedMessages) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(msg.text);
        }
        if (!currentMessage.isEmpty()) {
            String partial = currentMessage.substring(0, Math.min(typingIndex, currentMessage.length()));
            if (sb.length() > 0) sb.append("\n");
            sb.append(partial);
        }
        return sb.toString();
    }

    public Color getCurrentColor() { return currentColor; }
    public boolean hasPartialMessage() { return !currentMessage.isEmpty() && typingIndex > 0; }

    public String getPartialMessage() {
        if (currentMessage.isEmpty()) return "";
        return currentMessage.substring(0, Math.min(typingIndex, currentMessage.length()));
    }

    // ====== QUEUE PROCESSING ======
    private void processNextMessage() {
        if (isTyping || messageQueue.isEmpty()) {
            processingQueue = false;
            return;
        }

        processingQueue = true;
        ColoredMessage nextMessage = messageQueue.poll();
        if (nextMessage != null) startTypingMessage(nextMessage.text, nextMessage.color);
        else processingQueue = false;
    }

    // ====== ANIMATION LOGIC ======
    private void startTypingMessage(String message, Color color) {
        if (isTyping) return;
        setTypingState(message, color);
        typewriterTimer = new Timer(TYPEWRITER_DELAY, e -> typeNextCharacter());
        typewriterTimer.setCoalesce(false); // Prevent event merging else the TYPEWRITER_DELAY won't be consistent
        typewriterTimer.start();
    }

    private void typeNextCharacter() {
        if (!isTyping || typingIndex >= currentMessage.length()) {
            finishCurrentMessage();
            return;
        }

        typingIndex++;
        onUpdate.run();
        onScrollRequest.run();
    }

    private void finishCurrentMessage() {
        if (!isTyping) return;
        stopTyping();
        completedMessages.add(new ColoredMessage(currentMessage, currentColor));
        clearCurrentMessage();
        onUpdate.run();
        onScrollRequest.run();
        SwingUtilities.invokeLater(this::processNextMessage);
    }

    private void instantCompleteCurrentMessage() {
        if (!isTyping) return;
        resetTimer();
        completedMessages.add(new ColoredMessage(currentMessage, currentColor));
        resetTypingState();
        onUpdate.run();
        onScrollRequest.run();
        SwingUtilities.invokeLater(this::processNextMessage);
    }

    // ====== STATE HELPERS ======
    private void setTypingState(String message, Color color) {
        currentMessage = message;
        currentColor = color;
        typingIndex = 0;
        isTyping = true;
    }

    private void resetTypingState() {
        currentMessage = "";
        currentColor = null;
        typingIndex = 0;
        isTyping = false;
    }

    private void clearCurrentMessage() {
        currentMessage = "";
        currentColor = null;
        typingIndex = 0;
    }

    private boolean isAnimationActive() {
        return isTyping && typewriterTimer != null;
    }

    private void stopTyping() {
        resetTimer();
        isTyping = false;
    }

    // ====== TIMER HELPERS ======
    private void resetTimer() {
        if (typewriterTimer != null) {
            typewriterTimer.stop();
            typewriterTimer = null;
        }
    }
}
