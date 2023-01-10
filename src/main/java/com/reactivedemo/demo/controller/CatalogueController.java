package com.reactivedemo.demo.controller;

import java.time.Duration;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reactivedemo.demo.constants.CatalogueControllerAPIPaths;
import com.reactivedemo.demo.entity.CatalogueItem;
import com.reactivedemo.demo.exception.ResourceNotFoundException;
import com.reactivedemo.demo.model.ResourceIdentity;
import com.reactivedemo.demo.service.CatalogueCrudService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(CatalogueControllerAPIPaths.BASE_PATH)
public class CatalogueController {
	 @Autowired
	 private CatalogueCrudService catalogueCrudService;
	 
	    @GetMapping(CatalogueControllerAPIPaths.GET_ITEMS)
	    @ResponseStatus(value = HttpStatus.OK)
	    public Flux<CatalogueItem> getCatalogueItems() {
	        return catalogueCrudService.getCatalogueItems();
	    }

	    /**
	     * If api needs to push items as Streams to ensure Backpressure is applied, we need to set produces to MediaType.TEXT_EVENT_STREAM_VALUE
	     *
	     * MediaType.TEXT_EVENT_STREAM_VALUE  is the official media type for Server Sent Events (SSE)
	     * MediaType.APPLICATION_STREAM_JSON_VALUE is for server to server/http client communications.
	     *
	     * https://stackoverflow.com/questions/52098863/whats-the-difference-between-text-event-stream-and-application-streamjson
	     * @return catalogueItems
	     */
	    @GetMapping(path= CatalogueControllerAPIPaths.GET_ITEMS_STREAM, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	    @ResponseStatus(value = HttpStatus.OK)
	    public Flux<CatalogueItem> getCatalogueItemsStream() {
	        return catalogueCrudService
	                .getCatalogueItems()
	                .delayElements(Duration.ofMillis(200));
	    }
	    
	    @GetMapping(CatalogueControllerAPIPaths.GET_ITEM)
	    public Mono<CatalogueItem>
	        getCatalogueItemBySKU(@PathVariable(value = "sku") String skuNumber)
	            throws ResourceNotFoundException {

	        return catalogueCrudService.getCatalogueItem(skuNumber);
	    }
	    
	    @PostMapping(CatalogueControllerAPIPaths.CREATE)
	    @ResponseStatus(value = HttpStatus.CREATED)
	    public Mono<ResponseEntity> addCatalogueItem(@Valid @RequestBody CatalogueItem catalogueItem) {

	        Mono<Long> id = catalogueCrudService.addCatalogItem(catalogueItem);

	        return id.map(value -> ResponseEntity.status(HttpStatus.CREATED).body(new ResourceIdentity(value))).cast(ResponseEntity.class);
	    }
	    
	    @PutMapping(CatalogueControllerAPIPaths.UPDATE)
	    @ResponseStatus(value = HttpStatus.OK)
	    public void updateCatalogueItem(
	        @PathVariable(value = "sku") String skuNumber,
	        @Valid @RequestBody CatalogueItem catalogueItem) throws ResourceNotFoundException {

	        catalogueCrudService.updateCatalogueItem(catalogueItem);
	    }
	    
	    @DeleteMapping(CatalogueControllerAPIPaths.DELETE)
	    @ResponseStatus(value = HttpStatus.NO_CONTENT)
	    public void removeCatalogItem(@PathVariable(value = "sku") String skuNumber)
	        throws ResourceNotFoundException {

	        Mono<CatalogueItem> catalogueItem = catalogueCrudService.getCatalogueItem(skuNumber);
	        catalogueItem.subscribe(
	            value -> {
	                catalogueCrudService.deleteCatalogueItem(value);
	            }
	        );
	    }
}
