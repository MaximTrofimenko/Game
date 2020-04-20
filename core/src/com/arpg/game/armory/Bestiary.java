package com.arpg.game.armory;

import com.arpg.game.units.Monster;
import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Bestiary {
    private Map<String, Monster> map;

    public Bestiary() {
        this.map = new HashMap<>();
        BufferedReader reader = null;
        try {
            reader = Gdx.files.internal("data/monsters.csv").reader(8192);
            reader.readLine();
            String str = null;
            while ((str  = reader.readLine()) != null) {
                Monster m = new Monster(str);
                map.put(m.getTitle(), m);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Monster getPatternFromTitle(String title) {
        return map.get(title);
    }
}
