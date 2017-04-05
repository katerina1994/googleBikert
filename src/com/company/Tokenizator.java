package com.company;


import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizator {

    public static void tokenizator(String fileName, String filePath) {
        String line;
        int idLine = 0;
        try {
            //InputStream fis = Tokenizator.class.getResourceAsStream(filePath);
            InputStream fis = new FileInputStream(filePath);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("windows-1251"));
            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                Pattern pattern = Pattern.compile("[A-ZА-Яa-zа-я]+", Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    Key key = new Key();
                    key.word = matcher.group();
                    key.fileName = fileName;
                    key.numberLine = idLine;
                    key.start = matcher.start();
                    key.end = matcher.end();
                    Indexer.indexKey(key);
                    System.out.println(matcher.group() + " Документ: " + fileName + " Номер Строки: " + idLine + " Начало: " + matcher.start() + " Конец: " + matcher.end());
                }
                idLine++;
                System.out.println("");

            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }

    }
}
