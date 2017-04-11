package com.company;

public class SubString {
    int start;
    int end;
    int wordStart;
    int wordEnd;
    private String fullString;
    String baseWord;

    SubString(String _fullString, int wordStart, int _start, int wordEnd, int _end, String baseWord) {
        fullString = _fullString;
        this.wordStart = wordStart;
        this.wordEnd = wordEnd;
        start = Math.max(0, _start);
        end = Math.min(_end, fullString.length());
        this.baseWord = baseWord;
    }

    SubString(String _fullString, int start, int end, String baseWord) {
        fullString = _fullString;
        this.wordStart = start;
        this.wordEnd = end;
        this.start = Math.max(0, start);
        this.end = Math.min(end, fullString.length());
        this.baseWord = baseWord;
    }

    SubString(SubString other) {
        this.start = other.start;
        this.end = other.end;
        this.wordStart = other.wordStart;
        this.wordEnd = other.wordEnd;
        fullString = other.fullString;
        baseWord = other.baseWord;
    }

    @Override
    public String toString() {
        return fullString.substring(start, end);
    }
}
