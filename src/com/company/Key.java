package com.company;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.ArrayList;

public class Key {


    public String word;
    public ArrayList<Position> positions;
    public String fileName;
    public int start;
    public int end;
    public int numberLine;

        //геттеры и сеттеры для полей

        public BasicDBObject toDBObject() {

            BasicDBObject document = new BasicDBObject();
            //BasicDBObject document = new BasicDBObject();

            document.put("word", word);
            document.put("position", positions);
//            positions.forEach(position->{
//
//                document.put({"filename",position.fileName,})
//            });
//            document.put("fileName", fileName);
//            document.put("start", start);
//            document.put("end", end);
//            document.put("numberLine", numberLine);

            return document;
        }

        public static Key fromDBObject(DBObject document) {
            Key key = new Key();

            key.word = (String) document.get("word");
            key.fileName = (String) document.get("fileName");
            key.start = (int) document.get("start");
            key.end = (int) document.get("end");
            key.numberLine = (int) document.get("numberLine");

            return key;
        }
    }

