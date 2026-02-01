package com.rajnish.FileUploadService.util;

import com.rajnish.FileUploadService.model.ChunkData;

import java.util.Comparator;

public class ChunksDataChunkNoComparator implements Comparator<ChunkData> {

    @Override
    public int compare(ChunkData c1, ChunkData c2) {
        return Integer.compare(c1.getChunkNo(), c2.getChunkNo());
    }
}