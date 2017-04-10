package com.company;

import com.mongodb.*;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.*;

class Indexer {

    private static DBCollection keyCollection;

    private static DBCollection getKeyCollection() {
        if (keyCollection != null)
            return keyCollection;
        Mongo mongo = null;
        try {
            mongo = new Mongo();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        DB db = mongo.getDB("wordDB");
        keyCollection = db.getCollection("words");
        if (keyCollection == null) {
            keyCollection = db.createCollection("words", null);
        }
        return keyCollection;
    }

    static void indexKey(Key key) {
        getKeyCollection().update(
                new BasicDBObject("word", key.word),
                new BasicDBObject(
                        "$push",
                        new BasicDBObject("position", new BasicDBObject("file", key.fileName)
                                .append("line", key.numberLine)
                                .append("start", key.start)
                                .append("end", key.end))),
                true,
                false)
        ;
    }

    static HashMap<String, HashMap<String, Position>> findWords(ArrayList<String> words) {
        HashMap<String, HashMap<String, Position>> wordPositions = new HashMap<>();
        for (String word : words) {
            BasicDBObject query = new BasicDBObject();

            query.put("word", word);
            DBCursor cursor = getKeyCollection().find(query);

            String str;
            if (cursor.hasNext()) {
                str = cursor.next().toString();
                System.out.println(str);
                wordPositions.put(word, parsePositions(str));
            }
            else {
                wordPositions.put(word, null);
            }
        }
//        for (Map.Entry<String, HashMap<String, Position>> wordPositionEntry : wordPositions.entrySet()) {
//            System.out.println("Word: " + wordPositionEntry.getKey());
//            if (wordPositionEntry.getValue() != null) {
//                for (Map.Entry<String, Position> filePositionEntry : wordPositionEntry.getValue().entrySet()) {
//                    System.out.println("File: " + filePositionEntry.getKey());
//                    for (PositionInFile posInFile : filePositionEntry.getValue().positionsInFile) {
//                        System.out.println("Line:\t" + posInFile.line + "\tStart:\t" + posInFile.start + "\tEnd:\t" + posInFile.end);
//                    }
//                }
//            }
//        }

        return wordPositions;
    }

    private static HashMap<String, Position> parsePositions(String json) {
        HashMap<String, Position> positions = new HashMap<>();
        JSONArray jsonPositions = new JSONObject(json).getJSONArray("position");
        for (int i = 0; i < jsonPositions.length(); i++) {
            JSONObject jsonPosition = jsonPositions.getJSONObject(i);
            String file = jsonPosition.getString("file");
            int line = jsonPosition.getInt("line");
            int start = jsonPosition.getInt("start");
            int end = jsonPosition.getInt("end");
            if (!positions.containsKey(file)) {
                positions.put(file, new Position(file, new PositionInFile(line, start, end)));
            } else {
                positions.get(file).positionsInFile.add(new PositionInFile(line, start, end));
            }
        }
        return positions;
    }
}
