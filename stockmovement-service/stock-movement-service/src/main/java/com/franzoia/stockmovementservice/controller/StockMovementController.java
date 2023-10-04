package com.franzoia.stockmovementservice.controller;

import com.franzoia.common.dto.StockMovementDTO;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import com.franzoia.common.util.ErrorResponse;
import com.franzoia.stockmovementservice.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All operations with a stockmovement will be routed by this controller.
 * <p/>
 */
@Tag(name = "Stock", description = "Stock management API")
@RestController
@RequestMapping("/api/v1/stock-movements")
public class StockMovementController {

	private final StockMovementService stock_movementsService;

	@Autowired
	public StockMovementController(final StockMovementService stock_movementsService) {
		this.stock_movementsService = stock_movementsService;
	}

	@Operation( summary = "List all Stock data available for a product",
				description = "Check all available stock information for a provided product and creates a List<StockDTO>" )
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "A List of all Stock movement " )})
	@GetMapping
	public List<StockMovementDTO> listAll() {
		return stock_movementsService.listAll();
	}

	@Operation( summary = "Retrieves a StockMovement by ID",
				description = "Retrieves a StockMovement by its ID")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "The StockMovement"),
					@ApiResponse(responseCode = "404", description = "When the StockMovement with the provided ID is not found", content = { @Content(schema = @Schema()) })})
	@GetMapping("/{stock_movementId}")
	public StockMovementDTO getStockMovement(@PathVariable final long stock_movementId) throws EntityNotFoundException, ServiceNotAvailableException {
		return stock_movementsService.get(stock_movementId);
	}

	@Operation( summary = "Creates a new StockMovement",
			description = "Creates a new StockMovement. Upon success registration it will search for pending orders and fill them")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "New StockMovement created"),
					@ApiResponse(responseCode = "404", description = "When the Item with the provided ID is not found", content = { @Content(schema = @Schema()) })})
	@PostMapping
	public StockMovementDTO createStockMovement(@Valid @RequestBody final StockMovementDTO stock_movementsDTO) throws EntityNotFoundException, ConstraintsViolationException, ServiceNotAvailableException {
		return stock_movementsService.create(stock_movementsDTO);
	}

	@Operation( summary = "Removes a StockMovement by ID",
			description = "Removes a StockMovement by its ID if the quantity available is the same as the quantity informed before")
	@ApiResponses({ @ApiResponse(responseCode = "203", description = "The StockMovement was deleted"),
					@ApiResponse(responseCode = "404", description = "When the StockMovement with the provided ID is not found", content = { @Content(schema = @Schema()) })})
	@DeleteMapping("/{stock_movementId}")
	public ResponseEntity<ErrorResponse> deleteStockMovement(@PathVariable final long stock_movementId) throws EntityNotFoundException, ServiceNotAvailableException {
		stock_movementsService.delete(stock_movementId);
		return new ResponseEntity<>(ErrorResponse.builder()
				.message("StockMovement Successfully deleted")
				.build(), HttpStatus.ACCEPTED);
	}

	@Operation( summary = "Updates a StockMovement by ID",
			description = "Update the information of a StockMovement by its ID")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "The StockMovement successfully updated"),
					@ApiResponse(responseCode = "404", description = "When the StockMovement or the Item with the provided ID is not found", content = { @Content(schema = @Schema()) })})
	@PutMapping("/{stock_movementId}")
	public StockMovementDTO update(@PathVariable final long stock_movementId, @RequestBody final StockMovementDTO stockmovementDTO)
			throws EntityNotFoundException, ConstraintsViolationException, ServiceNotAvailableException {
		return stock_movementsService.update(stock_movementId, stockmovementDTO);
	}

	@Operation( summary = "List all StockMovement by ItemID",
			description = "List all StockMovement that were created with the Item (ID) provided")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "The StockMovement List<StockMovement>"),
					@ApiResponse(responseCode = "404", description = "When the Item with the provided ID is not found", content = { @Content(schema = @Schema()) })})
	@GetMapping("/item/{itemId}")
	public List<StockMovementDTO> listByItem(@PathVariable final Long itemId) throws EntityNotFoundException, ServiceNotAvailableException {
		return stock_movementsService.listByItem(itemId);
	}

	@Operation( summary = "List all Stock Movements with available quantity greater than zero",
				description = "List all Stock Movements with available quantity greater than zero")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "The StockMovement List<StockMovementDTO>") })
	@GetMapping("/quantityAvailable")
	public List<StockMovementDTO> listNonZeroQuantityAvailable() {
		return stock_movementsService.listByNonZeroQuantityAvailable();
	}



}
