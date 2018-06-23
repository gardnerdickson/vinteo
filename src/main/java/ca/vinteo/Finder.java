package ca.vinteo;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class Finder {

    private final ImmutableList<String> keys;

    public Finder(List<String> keys) {
        this.keys = ImmutableList.copyOf(keys);
    }

    public List<String> findLike(String text) {
        List<String> results = new ArrayList<>();
        for (String key : keys) {
            if (key.contains(text)) {
                results.add(key);
            }
        }
        return results;
    }

    public ArrayList<String> all() {
        return new ArrayList<>(keys);
    }

}
