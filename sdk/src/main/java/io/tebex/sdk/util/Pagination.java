package io.tebex.sdk.util;

import com.google.gson.JsonObject;

public class Pagination {
    private final int totalResults;
    private final int currentPage;
    private final int lastPage;
    private final String previous;
    private final String next;

    public Pagination(int totalResults, int currentPage, int lastPage, String previous, String next) {
        this.totalResults = totalResults;
        this.currentPage = currentPage;
        this.lastPage = lastPage;
        this.previous = previous;
        this.next = next;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getLastPage() {
        return lastPage;
    }

    public boolean hasPrevious() {
        return previous != null;
    }

    public boolean hasNext() {
        return next != null;
    }

    public String getPrevious() {
        return previous;
    }

    public String getNext() {
        return next;
    }

    @Override
    public String toString() {
        return "Pagination{" +
                "totalResults=" + totalResults +
                ", currentPage=" + currentPage +
                ", lastPage=" + lastPage +
                ", previous='" + previous + '\'' +
                ", next='" + next + '\'' +
                '}';
    }

    public static Pagination fromJsonObject(JsonObject jsonObject) {
       return new Pagination(
               jsonObject.get("totalResults").getAsInt(),
               jsonObject.get("currentPage").getAsInt(),
               jsonObject.get("lastPage").getAsInt(),
               !jsonObject.get("previous").isJsonNull() ? jsonObject.get("previous").getAsString() : null,
               !jsonObject.get("next").isJsonNull() ? jsonObject.get("next").getAsString() : null
       );
    }
}
