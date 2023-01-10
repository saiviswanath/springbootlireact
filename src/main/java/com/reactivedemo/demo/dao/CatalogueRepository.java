package com.reactivedemo.demo.dao;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;

import com.reactivedemo.demo.entity.CatalogueItem;

import reactor.core.publisher.Mono;

public interface CatalogueRepository extends ReactiveSortingRepository<CatalogueItem, Long> {
	Mono<CatalogueItem> findBySku(String sku);
}
