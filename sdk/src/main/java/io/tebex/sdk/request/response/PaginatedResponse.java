package io.tebex.sdk.request.response;

import io.tebex.sdk.util.Pagination;

import java.util.List;

public class PaginatedResponse<T> {
    private final Pagination pagination;
    private final List<T> data;

    public PaginatedResponse(Pagination pagination, List<T> data) {
        this.pagination = pagination;
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public List<T> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "PaginatedResponse{" +
                "pagination=" + pagination +
                ", data=" + data +
                '}';
    }
}