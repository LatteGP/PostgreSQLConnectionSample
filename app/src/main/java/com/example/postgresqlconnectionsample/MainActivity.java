package com.example.postgresqlconnectionsample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DBConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DBConnectionクラスをインスタンス
        this.connection = new DBConnection(this);

        this.findViewById(R.id.connect_button).setOnClickListener(
                // データベースへ接続
                (v) -> this.connection.connect()
        );

        this.findViewById(R.id.sql_button).setOnClickListener((v) -> {
            // 例：studentsテーブルからidカラムとnameカラムを取得する
            // SQLを作成
            String sql = "SELECT * FROM students;";
            // 取得するレコードのカラムを指定
            String[] columns = {"id", "name"};
            // SQLを実行しレコードを取得
            List<HashMap<String, String>> result = this.connection.executeQuery(sql, columns);
            // レコードをログに出力
            for (Map<String, String> record : result) {
                Log.d(record.get("id"), record.get("name"));
            }
        });
    }
}