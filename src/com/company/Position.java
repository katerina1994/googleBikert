package com.company;

import java.util.ArrayList;

public class Position
{
    public String fileName;
    ArrayList<PositionInFile> positionsInFile;


    public Position(String _fileName, ArrayList<PositionInFile> _positionsInFile){
        fileName = _fileName;
        positionsInFile = _positionsInFile;
    }

    Position(String _fileName, PositionInFile _positionInFile){
        fileName = _fileName;
        positionsInFile = new ArrayList<>();
        positionsInFile.add(_positionInFile);
    }
}


