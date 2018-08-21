package com.vng.tfa.common;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLongArray;

public class EstimatedHistogram
{
  private static final long[] bucketOffsets = { 1L, 10L, 100L, 200L, 300L, 400L, 500L, 600L, 700L, 800L, 900L, 1000L, 2000L, 3000L, 4000L, 5000L, 6000L, 7000L, 8000L, 9000L, 10000L, 15000L, 20000L, 50000L, 100000L, 200000L, 300000L, 400000L, 500000L, 1000000L, 2000000L, 5000000L, 10000000L };
  private static final int numBuckets = bucketOffsets.length + 1;
  final AtomicLongArray buckets;
  
  public EstimatedHistogram()
  {
    this.buckets = new AtomicLongArray(numBuckets);
  }
  
  public void add(long n)
  {
    int index = Arrays.binarySearch(bucketOffsets, n);
    if (index < 0) {
      index = -index - 1;
    } else {
      index++;
    }
    this.buckets.incrementAndGet(index);
  }
  
  public long[] getBucketOffset()
  {
    return bucketOffsets;
  }
  
  public void clear()
  {
    for (int i = 0; i < numBuckets; i++) {
      this.buckets.set(i, 0L);
    }
  }
  
  public long[] get(Boolean reset)
  {
    long[] rv = new long[numBuckets];
    for (int i = 0; i < numBuckets; i++) {
      rv[i] = this.buckets.get(i);
    }
    if (reset.booleanValue()) {
      for (int i = 0; i < numBuckets; i++) {
        this.buckets.set(i, 0L);
      }
    }
    return rv;
  }
}
