package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public static final String LETTERS = "abc";
    public static final String LAST_STRING = "last";
    public static final int NUM_OF_TEXTS = 10_000;
    public static final int TEXT_LENGTH = 100_000;
    public static final int MAX_QUEUE_SIZE = 100;
    public static List<BlockingQueue<String>> queues = new ArrayList<>();
    public static List<Integer> maxLengths = new ArrayList<>();
    public static List<String> longestTexts = new ArrayList<>();

    public static void main(String[] args) {
        for (int i = 0; i < LETTERS.length(); i++) {
            queues.add(new ArrayBlockingQueue<>(MAX_QUEUE_SIZE));
            maxLengths.add(0);
            longestTexts.add("");
        }

        List<Thread> threads = new ArrayList<>();
        // Генерация текстов
        Thread threadGenerateTexts = new Thread(() -> {
            for (int i = 0; i < NUM_OF_TEXTS; i++) {
                String text = generateText(LETTERS, TEXT_LENGTH);
                try {
                    for (int j = 0; j < LETTERS.length(); j++) {
                        queues.get(j).put(text);
                        //System.out.println("Текст " + i + " положен в очередь " + j + ", размер: " + queues.get(j).size());
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
            for (int j = 0; j < LETTERS.length(); j++) {
                try {
                    queues.get(j).put(LAST_STRING);
                    //System.out.println("Последний текст положен в очередь " + j + ", размер: " + queues.get(j).size());
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        threads.add(threadGenerateTexts);
        threadGenerateTexts.start();

        // Потоки на чтение текстов
        for (int i = 0; i < LETTERS.length(); i++) {
            Thread thread = getThread(i);
            threads.add(thread);
            thread.start();
        }

        // Ожидание завершения всех потоков: на запись и на чтение
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                return;
            }
        }

        // Вывод итоговой информации на экран
        for (int i = 0; i < LETTERS.length(); i++) {
            System.out.println("Строка с максимальным количеством символов " + LETTERS.charAt(i)
                    + " (" + maxLengths.get(i) + " символов из " + TEXT_LENGTH + "): " + longestTexts.get(i));
        }
    }

    private static Thread getThread(int i) {
        return new Thread(() -> {
            String text;
            while (true) {
                try {
                    //Thread.sleep(100);
                    text = queues.get(i).take();
                    //System.out.println("Текст взят из очереди " + i + ", размер: " + queues.get(i).size());
                } catch (InterruptedException e) {
                    return;
                }
                // Прекращение чтения после получения последней строки
                if (text.equals(LAST_STRING)) {
                    break;
                } else {
                    int count = (int) text.chars().filter(c -> c == LETTERS.charAt(i)).count();
                    if (count > maxLengths.get(i)) {
                        maxLengths.set(i, count);
                        longestTexts.set(i, text);
                    }
                }
            }
        });
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}