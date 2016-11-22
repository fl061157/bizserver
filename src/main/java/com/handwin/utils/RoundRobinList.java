package com.handwin.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fangliang on 26/5/15.
 */
public class RoundRobinList<T> {

    private List<T> collection;

    private int size;

    private int position;

    public RoundRobinList(List<T> list) {
        size = list.size();
        position = 0;
        collection = new ArrayList<>(size);
        list.stream().forEach(t -> collection.add(t));
    }

    public T get() {
        try {
            T t = collection.get(position);
            return t;
        } finally {
            position = (position + 1) % size;
        }
    }

}
