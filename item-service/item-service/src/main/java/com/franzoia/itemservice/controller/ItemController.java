package com.franzoia.itemservice.controller;

import com.franzoia.itemservice.service.ItemService;
import com.franzoia.common.dto.ItemDTO;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import com.franzoia.common.util.ErrorResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/**
 * All operations with a item will be routed by this controller.
 * <p/>
 */
@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

	private final ItemService itemsService;

	@Autowired
	public ItemController(final ItemService itemsService) {
		this.itemsService = itemsService;
	}

	@GetMapping
	public List<ItemDTO> listAll() {
		return itemsService.getMapper()
				.convertEntityToDTO(itemsService.findAll()).stream()
					.sorted(Comparator.comparing(ItemDTO::name)).toList();
	}

	@GetMapping("/{itemId}")
	public ItemDTO getItem(@PathVariable final long itemId) throws EntityNotFoundException {
		return itemsService.getMapper().convertEntityToDTO(itemsService.findOne(itemId));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ItemDTO createItem(@Valid @RequestBody final ItemDTO itemsDTO) throws ConstraintsViolationException {
		return itemsService.create(itemsDTO);
	}

	@DeleteMapping("/{itemId}")
	public ResponseEntity<ErrorResponse> deleteItem(@PathVariable final long itemId) throws EntityNotFoundException, ServiceNotAvailableException {
		itemsService.delete(itemId);
		return new ResponseEntity<>(ErrorResponse.builder().message("Item successfully deleted").build(), HttpStatus.ACCEPTED);
	}

	@PutMapping("/{itemId}")
	public ItemDTO update(@PathVariable final long itemId, @Valid @RequestBody final ItemDTO itemsDTO)
			throws EntityNotFoundException, ConstraintsViolationException {
		return itemsService.update(itemId, itemsDTO);
	}

}
