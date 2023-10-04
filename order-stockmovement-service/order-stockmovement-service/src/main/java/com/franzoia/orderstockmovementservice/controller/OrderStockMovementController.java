package com.franzoia.orderstockmovementservice.controller;

import com.franzoia.common.dto.OrderStockMovementDTO;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.util.ErrorResponse;
import com.franzoia.orderstockmovementservice.service.OrderStockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All operations with a orderstockmovement will be routed by this controller.
 * <p/>
 */
@RestController
@RequestMapping("/api/v1/order-stock-movements")
public class OrderStockMovementController {

	private final OrderStockMovementService orderstockmovementsService;

	@Autowired
	public OrderStockMovementController(final OrderStockMovementService orderstockmovementsService) {
		this.orderstockmovementsService = orderstockmovementsService;
	}

	@Operation( summary = "List all OrderStockMovement available for an Order",
			description = "List all OrderStockMovement available for an Order" )
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "A List of all OrderStockMovement for an Order" )})
	@GetMapping("/order/{orderId}")
	public List<OrderStockMovementDTO> listByOrder(@PathVariable("orderId") final Long orderId) {
		return orderstockmovementsService.listByOrder(orderId);
	}

	@Operation( summary = "List all OrderStockMovement available for a StockMovement",
			description = "List all OrderStockMovement available for a StockMovement" )
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "A List of all OrderStockMovement for a StockMovement" )})
	@GetMapping("/stock-movement/{stockMovementId}")
	public List<OrderStockMovementDTO> listByStockMovement(@PathVariable("stockMovementId") final Long stockMovementId) {
		return orderstockmovementsService.listByStockMovement(stockMovementId);
	}

	@Operation( summary = "Creates a new OrderStockMovement",
			description = "Creates a new OrderStockMovement. ")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "New OrderStockMovement created"),
			@ApiResponse(responseCode = "404", description = "When the Order/StockMovement with the provided ID not found", content = { @Content(schema = @Schema()) })})
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderStockMovementDTO createOrderStockMovement(@Valid @RequestBody final OrderStockMovementDTO orderStockMovementDTO) throws ConstraintsViolationException {
		return orderstockmovementsService.create(orderStockMovementDTO);
	}

	@Operation( summary = "Removes a OrderStockMovement by ID",
			description = "Removes a OrderStockMovement by Order and StockMovement IDs")
	@ApiResponses({ @ApiResponse(responseCode = "203", description = "The OrderStockMovement was deleted"),
			@ApiResponse(responseCode = "404", description = "When the Order/StockMovement with the provided ID not found", content = { @Content(schema = @Schema()) })})
	@DeleteMapping("/order/{orderId}/stock-movement/{stockMovementId}")
	public ResponseEntity<ErrorResponse> deleteOrderStockMovement(@PathVariable("orderId") final long orderId,
																  @PathVariable("stockMovementId") final long stockMovementId)
			throws EntityNotFoundException {
		orderstockmovementsService.delete(orderId, stockMovementId);
		return new ResponseEntity<>(ErrorResponse.builder().message("OrderStockMovement successfully deleted").build(), HttpStatus.ACCEPTED);
	}

}
