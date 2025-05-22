package com.cpl.reconciliation.tasks.utils;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class Utility {
    public <T> List<List<T>> chunkList(List<T> list, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be greater than zero");
        }
        int listSize = list.size();
        int numberOfChunks = (int) Math.ceil((double) listSize / chunkSize);
        return IntStream.range(0, numberOfChunks)
                .mapToObj(i -> list.subList(i * chunkSize, Math.min((i + 1) * chunkSize, listSize)))
                .collect(Collectors.toList());
    }
}
