package com.company;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;


import java.net.UnknownHostException;

public class Indexer {

    private static DBCollection keyCollection;

    static DBCollection getKeyCollection() {
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
    /*public   getByLogin(String login){
        BasicDBObject query = new BasicDBObject();

        // задаем поле и значение поля по которому будем искать
        query.put("login", login);

        // осуществляем поиск
        DBObject result = table.findOne(query);

        // Заполняем сущность полученными данными с коллекции
        User user = new User();
        user.setLogin(String.valueOf(result.get("login")));
        user.setId(String.valueOf(result.get("_id")));

        // возвращаем полученного пользователя
        return user;
    }*/
    /* public static void initDB() throws UnknownHostException {
         Mongo mongo = new Mongo();
         DB db = mongo.getDB("index");
         keyCollection = db.getCollection("word");
         if (keyCollection == null) {
             keyCollection = db.createCollection("word", null);
         }
     }*/
    public static void indexKey(Key key) {
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
}
