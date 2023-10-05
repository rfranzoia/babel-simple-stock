package com.franzoia.orderservice.controller;

import com.franzoia.common.dto.ItemDTO;
import com.franzoia.common.dto.OrderDTO;
import com.franzoia.common.dto.OrderStatus;
import com.franzoia.common.dto.UserDTO;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import com.franzoia.common.util.ErrorResponse;
import com.franzoia.orderservice.service.OrderService;
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
import java.util.Map;

/**
 * All operations with a order will be routed by this controller.
 * <p/>
 */
@Tag(name = "Stock", description = "Stock management API")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

	private final OrderService ordersService;

	@Autowired
	public OrderController(final OrderService ordersService) {
		this.ordersService = ordersService;
	}

	@Operation( summary = "List all Stock data available for a product",
				description = "Check all available stock information for a provided product and creates a List<StockDTO>" )
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "A List of all Stock movement " )})
	@GetMapping
	public List<OrderDTO> listAll() {
		return ordersService.listAll();
	}

	@Operation( summary = "Retrieves a Order by ID",
				description = "Retrieves a Order by its ID")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "The Order"),
					@ApiResponse(responseCode = "404", description = "When the Order with the provided ID is not found", content = { @Content(schema = @Schema()) })})
	@GetMapping("/{orderId}")
	public OrderDTO getOrder(@PathVariable final long orderId) throws EntityNotFoundException, ServiceNotAvailableException {
		return ordersService.get(orderId);
	}

	@Operation( summary = "Creates a new Order",
			description = "Creates a new Order. Upon success registration it will search for pending orders and fill them")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "New Order created"),
					@ApiResponse(responseCode = "404", description = "When the Item with the provided ID is not found", content = { @Content(schema = @Schema()) })})
	@PostMapping
	public OrderDTO createOrder(@Valid @RequestBody final OrderDTO ordersDTO) throws EntityNotFoundException, ConstraintsViolationException, ServiceNotAvailableException {
		return ordersService.create(ordersDTO);
	}

	@Operation( summary = "Removes a Order by ID",
			description = "Removes a Order by its ID if the quantity available is the same as the quantity informed before")
	@ApiResponses({ @ApiResponse(responseCode = "203", description = "The Order was deleted"),
					@ApiResponse(responseCode = "404", description = "When the Order with the provided ID is not found", content = { @Content(schema = @Schema()) })})
	@DeleteMapping("/{orderId}")
	public ResponseEntity<ErrorResponse> deleteOrder(@PathVariable final long orderId) throws EntityNotFoundException, ServiceNotAvailableException {
		ordersService.delete(orderId);
		return new ResponseEntity<>(ErrorResponse.builder()
				.message("Order Successfully deleted")
				.build(), HttpStatus.ACCEPTED);
	}

	@Operation( summary = "Updates a Order by ID",
			description = "Update the information of a Order by its ID")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "The Order successfully updated"),
					@ApiResponse(responseCode = "404", description = "When the Order or the Item with the provided ID is not found", content = { @Content(schema = @Schema()) })})
	@PutMapping("/{orderId}")
	public OrderDTO update(@PathVariable final long orderId, @RequestBody final OrderDTO orderDTO)
			throws EntityNotFoundException, ServiceNotAvailableException {
		return ordersService.update(orderId, orderDTO);
	}

	@Operation( summary = "List all Order by ItemID",
			description = "List all Order that were created with the Item (ID) provided")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "The Order List<Order>"),
					@ApiResponse(responseCode = "404", description = "When the Item with the provided ID is not found", content = { @Content(schema = @Schema()) })})
	@GetMapping("/item/{itemId}")
	public List<OrderDTO> listByItem(@PathVariable final Long itemId) throws EntityNotFoundException, ServiceNotAvailableException {
		return ordersService.listByItem(itemId);
	}

	@Operation( summary = "List all Order by ItemID and Status",
			description = "List all Order that were created with the Item (ID) provided and have the Status provided")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "The Order List<Order>"),
					@ApiResponse(responseCode = "404", description = "When the Item with the provided ID is not found", content = { @Content(schema = @Schema()) })})
	@GetMapping("/item/{itemId}/status/{status}")
	public List<OrderDTO> listByItemAndStatus(@PathVariable final Long itemId, @PathVariable final OrderStatus status) throws EntityNotFoundException, ServiceNotAvailableException {
		return ordersService.listByItemAndStatus(itemId, status);
	}

	@Operation( summary = "List all Order by Status",
			description = "List all Order that were created with the Status provided")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "The Order List<Order>") })
	@GetMapping("/item/{itemId}/status/{status}")
	public List<OrderDTO> listByStatus(@PathVariable final OrderStatus status) throws EntityNotFoundException, ServiceNotAvailableException {
		return ordersService.listByStatus(status);
	}

	
	@Operation( summary = "List all Order by UserID",
				description = "List all Order that were created with the User (ID) provided")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "The Order List<Order>"),
					@ApiResponse(responseCode = "404", description = "When the User with the provided ID is not found", content = { @Content(schema = @Schema()) })})
	@GetMapping("/user/{userId}")
	public List<OrderDTO> listByUser(@PathVariable final Long userId) throws EntityNotFoundException, ServiceNotAvailableException {
		return ordersService.listByUser(userId);
	}

}
