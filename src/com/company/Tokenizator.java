package com.company;


import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Tokenizator {

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

    static void findWordsInSameLine(String input) {
        ArrayList<String> inputWords = parseLine(input);
        HashMap<String, HashMap<String, Position>> wordsMap = null;

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
        ArrayList<String> files = includedFile(wordsMap, nonFoundWords, foundWords);


        for (String file : files) {
            System.out.println("File: " + file);
            HashMap<String, List<Integer>> mappedLines = new HashMap<>();
            for (String word : foundWords) {
                List<Integer> lines = wordsMap
                        .get(word)
                        .get(file)
                        .positionsInFile
                        .stream()
                        .map(filepos -> filepos.line)
                        .collect(Collectors.toList());
                Set<Integer> hs = new LinkedHashSet<>();
                hs.addAll(lines);
                lines.clear();
                lines.addAll(hs);
                mappedLines.put(word, lines);
            }

            List<Integer> linesIntersection = new ArrayList<>(mappedLines.values()).get(0);

            for (List<Integer> lines : mappedLines.values()) {
                linesIntersection.retainAll(lines);
//                System.out.println(linesIntersection);
            }
            System.out.println(linesIntersection);
            for (Integer lineNum : linesIntersection) {
                String line = StringInDB.getString(file, lineNum);
                System.out.println(line);
                processLine(line, foundWords);
            }

        }


    }

    private static void processLine(String line, ArrayList<String> foundWords) {
        List<SubString> parsedLine = parseLineWithPositions(line);

        int windowLeft = 3;
        int windowRight = 3;
        List<SubString> subStrings = new ArrayList<>();
        for (int i = 0; i < parsedLine.size(); i++) {
            if (foundWords.contains(parsedLine.get(i).baseWord)) {
                int startIndex = Math.max(0, i - windowLeft);
                int endIndex = Math.min(parsedLine.size() - 1, i + windowRight);
                subStrings.add(new SubString(
                        line,
                        parsedLine.get(startIndex).start,
                        parsedLine.get(endIndex).end,
                        parsedLine.get(i).baseWord));
            }
        }
        List<SubString> mergedSubStrings = mergeSubStrings(subStrings);
        for (SubString s : mergedSubStrings) {
            System.out.println(s);
            s.expandToSentence();
            System.out.println(s);
            System.out.println(hightlightWords(s.toString(), foundWords));
        }
    }

    private static List<SubString> mergeSubStrings(List<SubString> subStrings) {
        List<SubString> result = new ArrayList<>();
        for (int i = 0, j = 0; i < subStrings.size(); i++) {
            if (result.size() <= j) {
                result.add(new SubString(subStrings.get(i)));
                continue;
            }
            //2 - magic constant, probably this should be 3 or even 4
            if (result.get(j).end >= subStrings.get(i).start + 3) {
                result.get(j).end = subStrings.get(i).end;
                continue;
            }
            j++;
        }
        return result;
    }

    private static class SubString {
        int start;
        int end;
        String fullString;
        String baseWord;

        SubString(String _fullString, int _start, int _end, String baseWord) {
            fullString = _fullString;
            start = Math.max(0, _start);
            end = Math.min(_end, fullString.length() - 1);
            this.baseWord = baseWord;
        }

        SubString(SubString other) {
            this.start = other.start;
            this.end = other.end;
            fullString = other.fullString;
            baseWord = other.baseWord;
        }

        @Override
        public String toString() {
            return (start == 0 ? "" : "... ")
                    + fullString.substring(start, end)
                    + (end == fullString.length() - 1 ? "" : " ...");
        }

        void expandToSentence() {
            Pattern pattern = Pattern.compile("[?.!\\u2026]|^\\s*(?=\\p{Lu})", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(fullString);

            int tempStart = start;
            int tempEnd = fullString.length() - 1;
            while (matcher.find()) {
                if (matcher.end() < start) {
                    tempStart = matcher.end();
                }
                if (matcher.end() > end && tempEnd > matcher.end()) {
                    tempEnd = matcher.end();
                }
            }
            start = tempStart;
            end = tempEnd;

        }


    }

    static String hightlightWords( String s, List<String> words) {
        Pattern pattern = Pattern.compile("\\p{L}+", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(s);

        StringBuffer output = new StringBuffer();

        while (matcher.find()) {
            if (words.contains(matcher.group().toLowerCase())) {
                String rep = "<b>" + matcher.group() + "</b>";
                matcher.appendReplacement(output, rep);
            }
            else {
                matcher.appendReplacement(output, matcher.group());
            }
        }
        matcher.appendTail(output);
        return output.toString();
    }

    static void findWordsInSameFile(String input) {
        ArrayList<String> inputWords = parseLine(input);
        HashMap<String, HashMap<String, Position>> wordsMap = null;

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
        ArrayList<String> files = includedFile(wordsMap, nonFoundWords, foundWords);

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
        findWordsInSameLine(input);

    }

    private static ArrayList<String> includedFile(HashMap<String, HashMap<String, Position>> wordsMap,
                                                  ArrayList<String> nonFoundWords,
                                                  ArrayList<String> foundWords) {
        nonFoundWords.clear();
        foundWords.clear();

        for (Map.Entry<String, HashMap<String, Position>> wordEntry : wordsMap.entrySet()) {
            if (wordEntry.getValue() != null) {
                foundWords.add(wordEntry.getKey());
            } else {
                nonFoundWords.add(wordEntry.getKey());
            }
        }

        if (foundWords.size() == 0) {
            return new ArrayList<>();
        }

        ArrayList<String> filesIntersection = new ArrayList<>(wordsMap.get(foundWords.get(0)).keySet());
        for (String word : foundWords) {
            filesIntersection.retainAll(wordsMap.get(word).keySet());
            System.out.println(filesIntersection);
        }
        return filesIntersection;
    }

}