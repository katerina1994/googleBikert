package com.company;

class PositionInFile
{
    int start;
    int end;
    int line;

    PositionInFile(int _line, int _start, int _end){
        line = _line;
        start = _start;
        end = _end;
    }
}
