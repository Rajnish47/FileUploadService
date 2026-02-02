package com.rajnish.FileUploadService.util;

import com.rajnish.FileUploadService.model.ChunkMetadata;

import java.util.Comparator;

public class ChunksDataChunkNoComparator implements Comparator<ChunkMetadata> {

    @Override
    public int compare(ChunkMetadata c1, ChunkMetadata c2) {
        return Integer.compare(c1.getChunkNo(), c2.getChunkNo());
    }
}