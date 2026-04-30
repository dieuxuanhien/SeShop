package com.seshop.shared.domain;

public interface QueryHandler<Q, R> {
    R handle(Q query);
}
