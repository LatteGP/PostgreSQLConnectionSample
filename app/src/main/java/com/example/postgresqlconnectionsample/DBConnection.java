package com.example.postgresqlconnectionsample;

import android.content.Context;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DBConnection {

    private Connection connection;
    private final String url;
    private final String user;
    private final String password;
    private boolean status = false;

    public DBConnection(Context context) {
        // データベースのURLを生成
        StringBuilder builder = new StringBuilder();
        builder.append("jdbc:postgresql://");
        builder.append(context.getString(R.string.host));
        builder.append(":");
        builder.append(context.getString(R.string.port));
        builder.append("/");
        builder.append(context.getString(R.string.database));
        this.url = builder.toString();
        Log.d("Log", this.url);

        // データベースのユーザー情報を取得
        this.user = context.getString(R.string.user);
        this.password = context.getString(R.string.password);
    }

    /**
     * データベースへ接続する
     */
    public void connect() {
        // データベースへ接続する(非同期で行う)
        new Thread(() -> {
            try {
                Class.forName("org.postgresql.Driver");
                this.connection = DriverManager.getConnection(this.url, this.user, this.password);
                this.status = true;
                Log.d("DBConnection", "SUCCESSFUL");
            } catch (Exception e) {
                this.status = false;
                Log.d("DBConnection", "FAILED");
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * SQL(クエリ)を実行し、レコードを取得する
     *
     * @param sql 実行するSQL文
     * @param columns 取得するカラム
     * @return レコード
     */
    public List<HashMap<String, String>> executeQuery(String sql, String[] columns) {
        // レコードリストを生成
        List<HashMap<String, String>> records = new ArrayList<>();
        // データベース接続失敗ならレコードを空で返す
        if (!this.status) {
            return records;
        }

        // SQL実行処理待機用ラッチ
        CountDownLatch latch = new CountDownLatch(1);
        // SQL実行は非同期で行う
        // (メインスレッドで処理するとNetworkOnMainThreadExceptionが発生するっぽい)
        new Thread(() -> {
            try {
                // ステートメントを生成
                Statement statement = this.connection.createStatement();
                // SQLを実行
                ResultSet result = statement.executeQuery(sql);
                // 1件ずつレコードを取得
                while (result.next()) {
                    // マップを生成
                    HashMap<String, String> record = new HashMap<>();
                    for (String column : columns) {
                        // 指定したカラムを取得する
                        record.put(column, result.getString(column));
                    }
                    // マップをレコードリストへ格納
                    records.add(record);
                }
                // 実行待機を解除
                latch.countDown();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();

        try {
            // SQL実行処理を待機
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // レコードリストを返す
        return records;
    }
}