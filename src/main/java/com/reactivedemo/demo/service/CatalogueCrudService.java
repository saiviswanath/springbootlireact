package com.reactivedemo.demo.service;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.reactivedemo.demo.dao.CatalogueRepository;
import com.reactivedemo.demo.entity.CatalogueItem;
import com.reactivedemo.demo.event.CatalogueItemEvent;
import com.reactivedemo.demo.exception.ResourceNotFoundException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CatalogueCrudService {
    private final ApplicationEventPublisher publisher;
    private final CatalogueRepository catalogueRepository;

    CatalogueCrudService(ApplicationEventPublisher publisher, CatalogueRepository catalogueRepository) {
        this.publisher = publisher;
        this.catalogueRepository = catalogueRepository;
    }
    
    public Flux<CatalogueItem> getCatalogueItems() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        return catalogueRepository.findAll(sort);
    }
    
    public Mono<CatalogueItem> getCatalogueItem(String skuNumber) throws ResourceNotFoundException {
        return getCatalogueItemBySku(skuNumber);
    }
    
    private Mono<CatalogueItem> getCatalogueItemBySku(String skuNumber) throws ResourceNotFoundException {
        return catalogueRepository.findBySku(skuNumber)
            .switchIfEmpty(Mono.defer(() -> Mono.error(new ResourceNotFoundException(
                String.format("Catalogue Item not found for the provided SKU :: %s" , skuNumber)))));
    }
    
    public Mono<Long> addCatalogItem(CatalogueItem catalogueItem) {
        catalogueItem.setCreatedOn(Instant.now());

        return
            catalogueRepository
                .save(catalogueItem)
                .doOnSuccess(item -> publishCatalogueItemEvent(CatalogueItemEvent.CATALOGUEITEM_CREATED, item))
                .flatMap(item -> Mono.just(item.getId()));
    }
    
    private final void publishCatalogueItemEvent(String eventType, CatalogueItem item) {
        this.publisher.publishEvent(new CatalogueItemEvent(eventType, item));
    }
    
    public void updateCatalogueItem(CatalogueItem catalogueItem) throws ResourceNotFoundException{

        Mono<CatalogueItem> catalogueItemfromDB = getCatalogueItemBySku(catalogueItem.getSku());

        catalogueItemfromDB.subscribe(
            value -> {
                value.setName(catalogueItem.getName());
                value.setDescription(catalogueItem.getDescription());
                value.setPrice(catalogueItem.getPrice());
                value.setInventory(catalogueItem.getInventory());
                value.setUpdatedOn(Instant.now());

                catalogueRepository
                    .save(value)
                    .doOnSuccess(item -> publishCatalogueItemEvent(CatalogueItemEvent.CATALOGUEITEM_UPDATED, item))
                    .subscribe();
            });
    }
    
    public void deleteCatalogueItem(CatalogueItem catalogueItem) {

        // For delete to work as expected, we need to subscribe() for the flow to complete
        catalogueRepository.delete(catalogueItem).subscribe();
    }
}
