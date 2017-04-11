package com.company;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MergedSubStrings extends ArrayList<SubString> {

    private String fullString;

    MergedSubStrings(String fullString){
        super();
        this.fullString = fullString;
    }

    @Override
    public String toString(){
        if (this.isEmpty()){
            return "";
        }
        this.sort(Comparator.comparingInt(s -> s.wordStart));//
        SubString first = get(0);
        SubString last = get(size()-1);
        first.start = getExpandedStart(first.wordStart);
        last.end = getExpandedEnd(last.wordEnd);

        // merging crossed subStrings
        List<SubString> result = new ArrayList<>();
        for (int i = 0, j = 0; i < this.size(); i++) {
            if (result.size() <= j) {
                result.add(new SubString(this.get(i)));
                continue;
            }
            //2 - magic constant, probably this should be 3 or even 4
            if (result.get(j).end >= this.get(i).start + 3) {
                result.get(j).end = Math.max(this.get(i).end, result.get(j).end);
                continue;
            }
            j++;
            result.add(new SubString(this.get(i)));
        }
        // merging non crossed subStrings
        return String.join(" \u2026 ", result.stream().map(SubString::toString).collect(Collectors.toList()));
    }

    private int getExpandedEnd(int end) {//найти ближайшую вправо позицию конца предложения
        Pattern pattern = Pattern.compile("[?.!\\u2026]|^\\s*(?=\\p{Lu})", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(fullString);

        int tempEnd = fullString.length();
        while (matcher.find()) {
            if (matcher.end() > end && tempEnd > matcher.end()) {
                tempEnd = matcher.end();
            }
        }
        return tempEnd;
    }//найти ближайшую вправо позицию конца предложения

    private int getExpandedStart(int start) {
        Pattern pattern = Pattern.compile("[?.!\\u2026]|^\\s*(?=\\p{Lu})");
        Matcher matcher = pattern.matcher(fullString);

        int tempStart = start;
        while (matcher.find()) {
            if (matcher.end() < start) {
                tempStart = matcher.end();
            }
        }
        return tempStart;
    } //найти ближайшую влево позицию начала предложения


}
