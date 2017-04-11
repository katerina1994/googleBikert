package com.company;

import com.mongodb.*;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

class StringInDB {
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
        keyCollection = db.getCollection("String");
        if (keyCollection == null) {
            keyCollection = db.createCollection("String", null);
        }
        return keyCollection;
    }

    static void StringsFile(String str, String fileName, Integer line) {
        getKeyCollection().insert(new BasicDBObject("FileName", fileName).
                append("line", line)
                .append("String", str));
    }

    static String getString(String fileName, Integer line) {
        BasicDBObject query = new BasicDBObject();

        List<BasicDBObject> obj = new ArrayList<>();
        obj.add(new BasicDBObject("FileName", fileName));
        obj.add(new BasicDBObject("line", line));

        query.put("$and", obj);

        DBCursor cursor = getKeyCollection().find(query);

        if (cursor.hasNext()) {
            String json = cursor.next().toString();
            return new JSONObject(json).getString("String");
        }
        return "";
    }
}
