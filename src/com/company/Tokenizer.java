package com.company;


import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Tokenizer {

    //Разбиение файла по строкам а затем парсим по словам и отдаем в Indexer.indexKey(Слово имя файла строку начало и конец);
    static void processFile(String fileName, String filePath) {
        String line;
        int lineNum = 0;
        try {
            InputStream fis = new FileInputStream(filePath);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                StringInDB.StringsFile(line, fileName, lineNum);
                Pattern pattern = Pattern.compile("\\p{L}+", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    Key key = new Key();
                    key.word = matcher.group().toLowerCase();
                    key.fileName = fileName;
                    key.numberLine = lineNum;
                    key.start = matcher.start();
                    key.end = matcher.end();
                    Indexer.indexKey(key);
                    System.out.println(matcher.group().toLowerCase() + " Документ: " + fileName + " Номер Строки: " + lineNum + " Начало: " + matcher.start() + " Конец: " + matcher.end());

                    System.out.println("");
                }
                lineNum++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print(e.getMessage());
        }
    }

    // парсим строку input по словам и отдаем ее в findWordsInSameFile в структуре ArrayList;
    static private ArrayList<String> parseLine(String input) {
        Pattern pattern = Pattern.compile("\\p{L}+", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        ArrayList<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group().toLowerCase());
        }
        return list;
    }

    static private ArrayList<SubString> parseLineWithPositions(String input) {
        Pattern pattern = Pattern.compile("\\p{L}+", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        ArrayList<SubString> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(new SubString(input, matcher.start(), matcher.end(), matcher.group().toLowerCase()));
        }
        return list;
    }

    static Map<String, Map<Integer, List<String>>> findWordsInSameLine(String input) {
        ArrayList<String> inputWords = parseLine(input);
        HashMap<String, HashMap<String, Position>> wordsMap = null;
        Map<String, Map<Integer, List<String>>> result = new LinkedHashMap<>();
        try {
            wordsMap = Indexer.findWords(inputWords);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print(e.getMessage());
        }

        if (wordsMap == null) {
            return result;
        }

        ArrayList<String> nonFoundWords = new ArrayList<>();
        ArrayList<String> foundWords = new ArrayList<>();
        ArrayList<String> files = includedFile(wordsMap, nonFoundWords, foundWords);//лист файлов со всеми найденными хотя бы где нибудь словами

        for (String file : files) {
            result.put(file, new LinkedHashMap<>());
            System.out.println("File: " + file);
            HashMap<String, List<Integer>> mappedLines = new LinkedHashMap<>();// ключ: Слово, Значение: список уникальных строк где оно содержится
            for (String word : foundWords) {
                List<Integer> lines = wordsMap //создаем список строк данного слова в данном файле
                        .get(word)
                        .get(file)
                        .positionsInFile
                        .stream()
                        .map(filepos -> filepos.line)
                        .collect(Collectors.toList());
                Set<Integer> hs = new LinkedHashSet<>();
                hs.addAll(lines); //оставляет только уникальные номера строк
                lines.clear();
                lines.addAll(hs);
                mappedLines.put(word, lines);
            }

            List<Integer> linesIntersection = new ArrayList<>(mappedLines.values()).get(0);//создаем лист от колекции списков уникальных строк и берем первый элемент

            for (List<Integer> lines : mappedLines.values()) {
                linesIntersection.retainAll(lines);// пересечение уникальных строк н-ого элемента
            }
            System.out.println(linesIntersection); // номера строк где содежатся все слова

            for (Integer lineNum : linesIntersection) {
                String line = StringInDB.getString(file, lineNum); //вытаскивает строку из базы
                System.out.println(line);
                result.get(file).put(lineNum, processLine(line, foundWords)); //сохраняем список цитат для текущего файла и текущей строки
            }

        }
        return result;

    }

    private static List<String> processLine(String line, ArrayList<String> foundWords) {
        List<SubString> parsedLine = parseLineWithPositions(line);//список подстрок(слова с их позициями)
        List<String> highlightedStrings = new ArrayList<>();
        int windowLeft = 3;
        int windowRight = 3;

        List<SubString> subStrings = new MergedSubStrings(line);// обьединенный список подстрок
        for (int i = 0; i < parsedLine.size(); i++) {
            if (foundWords.contains(parsedLine.get(i).baseWord)) { //
                int startIndex = Math.max(0, i - windowLeft);
                int endIndex = Math.min(parsedLine.size() - 1, i + windowRight);
                subStrings.add(new SubString(
                        line,
                        parsedLine.get(i).start,
                        parsedLine.get(startIndex).start,
                        parsedLine.get(i).end,
                        parsedLine.get(endIndex).end,
                        parsedLine.get(i).baseWord));
            }
        }
        System.out.println(subStrings);
        highlightedStrings.add(highlightWords(subStrings.toString(), foundWords));
        System.out.println("Highlighted: " + highlightWords(subStrings.toString(), foundWords));
        return highlightedStrings;
    }

    private static String highlightWords(String s, List<String> words) {
        Pattern pattern = Pattern.compile("\\p{L}+", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(s);

        StringBuffer output = new StringBuffer();

        while (matcher.find()) {
            if (words.contains(matcher.group().toLowerCase())) {
                String rep = "<b>" + matcher.group() + "</b>";
                matcher.appendReplacement(output, rep);
            } else {
                matcher.appendReplacement(output, matcher.group());
            }
        }
        matcher.appendTail(output);
        return output.toString();
    }

    static void findWordsInSameFile(String input) {
        ArrayList<String> inputWords = parseLine(input);
        HashMap<String, HashMap<String, Position>> wordsMap = null; //Вытащенные из базы искомые слова и их позиции

        try {
            wordsMap = Indexer.findWords(inputWords);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print(e.getMessage());
        }

        if (wordsMap == null) {
            return;
        }

        ArrayList<String> nonFoundWords = new ArrayList<>();
        ArrayList<String> foundWords = new ArrayList<>();
        ArrayList<String> files = includedFile(wordsMap, nonFoundWords, foundWords);//лист файлов со всеми найденными хотя бы где нибудь словами

        System.out.println("Non found words: " + nonFoundWords);
        System.out.println("Found words: " + foundWords);
        System.out.println("All found words were found in: " + files);
        for (String file : files) {
            System.out.println("File: " + file);
            for (String word : foundWords) {
                System.out.println("Word: " + word);
                for (PositionInFile posInFile : wordsMap.get(word).get(file).positionsInFile) {
                    System.out.println("Line:\t" + posInFile.line + "\tStart:\t" + posInFile.start + "\tEnd:\t" + posInFile.end);
                }
            }
        }
    }

    private static ArrayList<String> includedFile(HashMap<String, HashMap<String, Position>> wordsMap,
                                                  ArrayList<String> nonFoundWords,
                                                  ArrayList<String> foundWords) {
        nonFoundWords.clear();
        foundWords.clear();

        for (Map.Entry<String, HashMap<String, Position>> wordEntry : wordsMap.entrySet()) { //entrySet() - Получает набор элементов
            if (wordEntry.getValue() != null) { // Если есть значение ()
                foundWords.add(wordEntry.getKey()); // Добавляем слово в список найденных слов
            } else {
                nonFoundWords.add(wordEntry.getKey());// Добавляем слово в список не найденных слов
            }
        }

        if (foundWords.size() == 0) {
            return new ArrayList<>(); // вернет пустой лист если слова не нашлись
        }
        // Ищем файлы содержащие все найденные слова
        ArrayList<String> filesIntersection = new ArrayList<>(wordsMap.get(foundWords.get(0)).keySet());//Вытащит все файлы для первого слова
        for (String word : foundWords) {
            filesIntersection.retainAll(wordsMap.get(word).keySet());// ищет пересечение файлов для слов
           // System.out.println(filesIntersection);
        }
        return filesIntersection;
    }

}