package com.franzoia.orderservice.service;

import com.franzoia.common.dto.*;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.InvalidRequestException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import com.franzoia.common.util.DefaultService;
import com.franzoia.common.util.ErrorResponse;
import com.franzoia.orderservice.config.ItemFeignClient;
import com.franzoia.orderservice.config.OrderStockMovementFeignClient;
import com.franzoia.orderservice.config.StockMovementFeignClient;
import com.franzoia.orderservice.config.UserFeignClient;
import com.franzoia.orderservice.model.Order;
import com.franzoia.orderservice.repository.OrderRepository;
import com.franzoia.orderservice.service.mapper.OrderMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;


/**
 * Services that handle Orders (and their associated items)
 * also, uses external item service
 */
@Slf4j
@Service
public class OrderService extends DefaultService<OrderDTO, Order, Long, OrderMapper> {

	@Autowired
	private ItemFeignClient itemFeignClient;

	@Autowired
	private UserFeignClient userFeignClient;

	@Autowired
	private StockMovementFeignClient stockMovementFeignClient;

	@Autowired
	private OrderStockMovementFeignClient orderStockMovementFeignClient;

	public OrderService(final OrderRepository orderRepository) {
		super(orderRepository, new OrderMapper());
	}

	public OrderDTO get(final Long orderId) throws EntityNotFoundException, ServiceNotAvailableException {
		log.info("retrieving order #{}", orderId);
		Order order = findByIdChecked(orderId);
		ItemDTO item = getItemDTO(order.getItemId());
		UserDTO user = getUserDTO(order.getUserId());
		return OrderDTO.builder()
				.id(order.getId())
				.creationDate(order.getCreationDate())
				.item(item)
				.quantityOrdered(order.getQuantityOrdered())
				.quantityFulfilled(order.getQuantityFulfilled())
				.user(user)
				.status(order.getStatus())
				.build();
	}

	private ItemDTO getItemDTO(final Long itemId) throws EntityNotFoundException, FeignException, ServiceNotAvailableException {
		try {
			return itemFeignClient.getByItemId(itemId);
		} catch (ServiceNotAvailableException | FeignException fe) {
			log.error("Item not available", fe);
			throw fe;
        }
    }

	private UserDTO getUserDTO(final Long userId) throws EntityNotFoundException, FeignException, ServiceNotAvailableException {
		try {
			return userFeignClient.getByUserId(userId);
		} catch (ServiceNotAvailableException | FeignException fe) {
			log.error("User not available", fe);
			throw fe;
		}
	}

	/**
	 * Creates a order
	 */
	@Transactional
	public OrderDTO create(OrderDTO dto) throws EntityNotFoundException, ConstraintsViolationException, ServiceNotAvailableException {
		log.info("creating order {}", dto);
		ItemDTO item = getItemDTO(dto.itemId());
		UserDTO user = getUserDTO(dto.userId());

		Order order = mapper.convertDtoToEntity(dto);
		order.setCreationDate(LocalDateTime.now());
		order.setQuantityFulfilled(0L);
		order.setStatus(OrderStatus.pending);

		// create the order as it is
		order = create(order);

		// update the stock and the order if possible
		order = tryToFulfillOrderAfterCreate(order);

		return OrderDTO.builder()
				.id(order.getId())
				.creationDate(order.getCreationDate())
				.item(item)
				.quantityOrdered(order.getQuantityOrdered())
				.quantityFulfilled(order.getQuantityFulfilled())
				.user(user)
				.status(order.getStatus())
				.build();
	}

	@Transactional
	public Order tryToFulfillOrderAfterCreate(Order order) throws ServiceNotAvailableException, EntityNotFoundException, ConstraintsViolationException {
		log.info("searching for available quantities in StockMovement");
		List<StockMovementDTO> availableStockMovements = stockMovementFeignClient.listNonZeroQuantityAvailable();
		for (StockMovementDTO stockMovementDTO : availableStockMovements) {
			log.info("\tUpdating order {} with available quantity from {}", order, stockMovementDTO);
			long quantityToTake;
			if (stockMovementDTO.quantityAvailable() >= order.getQuantityOrdered()) {
				quantityToTake = order.getQuantityOrdered();
			} else {
				quantityToTake = stockMovementDTO.quantityAvailable();
			}

			// now update the order
			order.setQuantityFulfilled(quantityToTake);
			order.setStatus(order.getQuantityOrdered().equals(order.getQuantityFulfilled())? OrderStatus.completed: OrderStatus.pending);
			order.getAudit().setUpdatedAt(ZonedDateTime.now());

			order = repository.save(order);

			if (order.getStatus().equals(OrderStatus.completed)) {
				log.info("Order #{} is completed, sending email to user {}",order.getId(), order.getUserId());
				final OrderDTO o = mapper.convertEntityToDTO(order);
				new Thread(() -> {
					try {
						ResponseEntity<ErrorResponse> response = userFeignClient.sendOrderCompletedEmail(o.userId(), o);
						log.info(response.getBody().toString());
					} catch (EntityNotFoundException | MailException exception) {
						log.error("Unable to send email to user: {}", exception.toString());
					}
				}).start();
			}

			var stockMovementUpdateRequest = StockMovementDTO.builder()
					.quantityAvailable(stockMovementDTO.quantityAvailable() - quantityToTake)
					.build();
			stockMovementFeignClient.update(stockMovementDTO.id(), stockMovementUpdateRequest);

			addToTrace(mapper.convertEntityToDTO(order), stockMovementDTO, quantityToTake);

			if (order.getQuantityFulfilled().equals(order.getQuantityOrdered())) {
				break;
			}
		}

		return order;
	}

	public void addToTrace(OrderDTO orderDTO, StockMovementDTO stockMovementDTO, Long quantity) throws ConstraintsViolationException {
		log.warn("\tAdding tracing for -> Order:{} | StockMovement:{} | Quantity:{}", orderDTO.id(), stockMovementDTO.id(), quantity);
		new Thread(() -> {
			try {
				OrderStockMovementKey key = new OrderStockMovementKey(orderDTO.id(), stockMovementDTO.id());
				orderStockMovementFeignClient
						.createOrderStockMovement(OrderStockMovementDTO.builder()
								.key(key)
								.quantityUsed(quantity)
								.build());
			} catch (ConstraintsViolationException exception) {
				log.error("Unable to add tracing, cause: {}", exception.toString());
			}
		}).start();
	}

	/**
	 * Update a order information.
	 */
	@Transactional
	public OrderDTO update(final Long orderId, final OrderDTO dto) throws EntityNotFoundException, ServiceNotAvailableException {
		log.info("updating order #{}{}",orderId, dto);
		Order order = findByIdChecked(orderId);

		if (order.getStatus().equals(OrderStatus.completed)) {
			throw new InvalidRequestException("Completed orders can't be changed");
		}

		if (dto.itemId() != null && !dto.itemId().equals(order.getItemId())) {
			// now update the order
			order.setItemId(dto.itemId());
		}

		if (dto.quantityFulfilled() != null) {
			order.setQuantityFulfilled(dto.quantityFulfilled());
		}

		order.setQuantityOrdered(dto.quantityOrdered() != null? dto.quantityOrdered(): order.getQuantityOrdered());
		order.setStatus(order.getQuantityOrdered().equals(order.getQuantityFulfilled())? OrderStatus.completed: OrderStatus.pending);
		order.getAudit().setUpdatedAt(ZonedDateTime.now());

		// save the updated order
		repository.save(order);

		if (order.getStatus().equals(OrderStatus.completed)) {
			log.info("Order #{} is completed, sending email to user {}",order.getId(), order.getUserId());
			final OrderDTO o = mapper.convertEntityToDTO(order);
			new Thread(() -> {
				try {
					ResponseEntity<ErrorResponse> response = userFeignClient.sendOrderCompletedEmail(o.userId(), o);
					log.info(response.getBody().toString());
				} catch (EntityNotFoundException | MailException exception) {
					log.error("Unable to send email to user: {}", exception.toString());
				}
			}).start();
		}

		return get(orderId);
	}

	/**
	 * delete an Order by ID and update the Stock available to the item
	 *
	 * @param orderId the id of the Order
	 * @throws EntityNotFoundException when the Order is not found
	 */
	@Transactional
	public void delete(final Long orderId) throws EntityNotFoundException {
		log.info("removing order #{}", orderId);
		Order order = findByIdChecked(orderId);

		if (order.getStatus().equals(OrderStatus.completed)) {
			throw new InvalidRequestException("Completed orders can't be deleted");
		}

		repository.delete(order);
	}

	/**
	 * List all orders
	 */
	public List<OrderDTO> listAll() {
		return createOrderList(findAll());
	}

	/**
	 * List all Order for a specific Item
	 *
	 * @param itemId the ID of the item
	 * @return List<Order> of Stock
	 *
	 */
	public List<OrderDTO> listByItem(final Long itemId) {
		// find all orders by item and return
		return createOrderList(((OrderRepository) repository).findAllByItemIdOrderByCreationDate(itemId));
	}

	/**
	 * List all orders for an Item with the Status
	 *
	 * @param itemId the id of the item
	 * @param status the status of the order
	 * @return List<OrderDTO>
	 */
	public List<OrderDTO> listByItemAndStatus(final long itemId, final OrderStatus status) {
		// find all orders by item and status
		return createOrderList(((OrderRepository) repository).findAllByItemIdAndStatusOrderByCreationDate(itemId, status));
	}

	/**
	 * List all orders with the Status
	 *
	 * @param status the status of the order
	 * @return List<OrderDTO>
	 */
	public List<OrderDTO> listByStatus(final OrderStatus status) {
		return createOrderList(((OrderRepository) repository).findAllByStatusOrderByCreationDate(status));
	}

		/**
	 * List all Order for a specific User
	 *
	 * @param userId the ID of the user
	 * @return List<Order> of Order
	 *
	 */
	public List<OrderDTO> listByUser(final Long userId) {
		// find all orders by item and return them grouping by User
		return createOrderList(((OrderRepository) repository).findAllByUserIdOrderByCreationDate(userId));
	}

	Map<Long, ItemDTO> itemsMap = null;
	Map<Long, UserDTO> usersMap = null;

	// fallback for item-service
	final Function<Long, ItemDTO> itemFinder = id -> {
		if (!getItemsMap().containsKey(id)) {
			return ItemDTO.builder()
					.name("Unavailable Item Data")
					.build();
		} else {
			return getItemsMap().get(id);
		}
	};

	// fallback for user-service
	final Function<Long, UserDTO> userFinder = id -> {
		if (!getUsersMap().containsKey(id)) {
			return UserDTO.builder()
					.name("Unavailable User Data")
					.build();
		} else {
			return getUsersMap().get(id);
		}
	};

	private List<OrderDTO> createOrderList(final List<Order> orders) {
		List<OrderDTO> list = new ArrayList<>();
		orders
			.forEach(o -> {
				OrderDTO dto = OrderDTO.builder()
						.id(o.getId())
						.creationDate(o.getCreationDate())
						.item(itemFinder.apply(o.getItemId()))
						.quantityOrdered(o.getQuantityOrdered())
						.quantityFulfilled(o.getQuantityFulfilled())
						.userId(o.getUserId())
						.user(userFinder.apply(o.getUserId()))
						.status(o.getStatus())
						.build();
				list.add(dto);
			});
		return list.stream().sorted(Comparator.comparing(OrderDTO::creationDate)).toList();
	}

	private Map<Long, ItemDTO> getItemsMap() {
		if (itemsMap == null || itemsMap.isEmpty()) {
			new Thread(() -> {
				try {
					itemsMap = itemFeignClient.listItems().stream()
							.collect(groupingBy(ItemDTO::id))
							.entrySet().stream()
							.collect(toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
				} catch (Throwable fe) {
					log.error("item-service status: {}", fe.getMessage());
					itemsMap = new TreeMap<>();
				}
			}).start();
		}
		return itemsMap;
	}
	
	private Map<Long, UserDTO> getUsersMap() {
		if (usersMap == null || usersMap.isEmpty()) {
			new Thread(() -> {
				try {
					usersMap = userFeignClient.listUsers()
							.stream()
							.collect(groupingBy(UserDTO::id))
							.entrySet().stream()
							.collect(toMap(Map.Entry::getKey, e -> e.getValue().get(0)));

				} catch (Throwable fe) {
					log.error("user-service status: {}", fe.getMessage());
					usersMap =  new TreeMap<>();
				}
			}).start();
		}
		return usersMap;
	}
}
