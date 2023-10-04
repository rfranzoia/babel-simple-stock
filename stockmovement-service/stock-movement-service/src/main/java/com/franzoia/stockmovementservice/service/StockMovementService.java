package com.franzoia.stockmovementservice.service;

import com.franzoia.common.dto.*;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.exception.ServiceNotAvailableException;
import com.franzoia.common.util.DefaultService;
import com.franzoia.stockmovementservice.config.ItemFeignClient;
import com.franzoia.stockmovementservice.config.OrderFeignClient;
import com.franzoia.stockmovementservice.config.OrderStockMovementFeignClient;
import com.franzoia.stockmovementservice.model.StockMovement;
import com.franzoia.stockmovementservice.repository.StockMovementRepository;
import com.franzoia.stockmovementservice.service.mapper.StockMovementMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;


/**
 * Services that handle StockMovements (and their associated items)
 * also, uses external item service
 */
@Slf4j
@Service
public class StockMovementService extends DefaultService<StockMovementDTO, StockMovement, Long, StockMovementMapper> {

	@Autowired
	private ItemFeignClient itemFeignClient;

	@Autowired
	private OrderFeignClient orderFeignClient;

	@Autowired
	private OrderStockMovementFeignClient orderStockMovementFeignClient;

	public StockMovementService(final StockMovementRepository stockmovementRepository) {
		super(stockmovementRepository, new StockMovementMapper());
	}

	public StockMovementDTO get(final Long stock_movementId) throws EntityNotFoundException, ServiceNotAvailableException {
		StockMovement stockmovement = findByIdChecked(stock_movementId);
		ItemDTO item = getItemDTO(stockmovement.getItemId());
		return StockMovementDTO.builder()
				.id(stockmovement.getId())
				.creationDate(stockmovement.getCreationDate())
				.item(item)
				.quantityInformed(stockmovement.getQuantityInformed())
				.quantityAvailable(stockmovement.getQuantityAvailable())
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

	/**
	 * Creates a stockmovement
	 */
	@Transactional
	public StockMovementDTO create(StockMovementDTO dto) throws EntityNotFoundException, ConstraintsViolationException, ServiceNotAvailableException {
		ItemDTO item = getItemDTO(dto.itemId());

		StockMovement stockmovement = mapper.convertDtoToEntity(dto);
		stockmovement.setQuantityAvailable(dto.quantityInformed());
		stockmovement.setCreationDate(LocalDateTime.now());

		stockmovement = create(stockmovement);

		StockMovementDTO newDTO = StockMovementDTO.builder()
				.id(stockmovement.getId())
				.creationDate(stockmovement.getCreationDate())
				.item(item)
				.quantityInformed(stockmovement.getQuantityInformed())
				.quantityAvailable(stockmovement.getQuantityAvailable())
				.build();

		// check pending orders to fulfill them
		checkPendingOrders(stockmovement);

		return newDTO;
	}

	@Transactional
	public void checkPendingOrders(StockMovement stockMovement) throws ServiceNotAvailableException, EntityNotFoundException, ConstraintsViolationException {
		List<OrderDTO> pendingOrders = orderFeignClient.listByItemAndStatus(stockMovement.getItemId(), OrderStatus.pending);

		// check all orders found trying to fulfill them
		for (OrderDTO o : pendingOrders) {
			log.warn("Pending order to Update: {}", o.toString());
			stockMovement = updateOrderAndStockMovement(stockMovement, o);

			// leave the loop if the available quantity for this StockMovement is already depleted
			if (stockMovement.getQuantityAvailable() == 0) {
				break;
			}
		}
	}

	@Transactional
	public StockMovement updateOrderAndStockMovement(StockMovement stockMovement, OrderDTO o) throws ServiceNotAvailableException, EntityNotFoundException, ConstraintsViolationException {
		log.info("updating order: {}", o);
		long quantity;
		long diff = o.quantityOrdered() - o.quantityFulfilled();

		if (diff <= stockMovement.getQuantityAvailable()) {
			quantity = diff;
		} else {
			quantity = stockMovement.getQuantityAvailable();
		}

		OrderDTO updateOrderRequest = OrderDTO.builder()
				.quantityFulfilled(o.quantityFulfilled() + quantity)
				.build();

		log.info("order update request: {}", updateOrderRequest);
		var updatedOrder = orderFeignClient.update(o.id(), updateOrderRequest);

		stockMovement.setQuantityAvailable(stockMovement.getQuantityAvailable() - quantity);
		stockMovement = repository.save(stockMovement);

		addToTrace(updatedOrder, mapper.convertEntityToDTO(stockMovement), quantity);

		return stockMovement;
	}

	public void addToTrace(OrderDTO orderDTO, StockMovementDTO stockMovementDTO, Long quantity) throws ConstraintsViolationException {
		log.warn("Order: {} | StockMovement: {} | Quantity: {}", orderDTO.id(), stockMovementDTO.id(), quantity);
		OrderStockMovementKey key = new OrderStockMovementKey(orderDTO.id(), stockMovementDTO.id());
		orderStockMovementFeignClient
				.createOrderStockMovement(OrderStockMovementDTO.builder()
					.key(key)
					.quantityUsed(quantity)
					.build());
	}

	/**
	 * Update a stockmovement information.
	 */
	@Transactional
	public StockMovementDTO update(final Long stock_movementId, final StockMovementDTO dto) throws EntityNotFoundException, ConstraintsViolationException, ServiceNotAvailableException {
		StockMovement stockmovement = findByIdChecked(stock_movementId);

		stockmovement.setItemId(dto.itemId() != null? dto.itemId(): stockmovement.getItemId());
		stockmovement.setQuantityInformed(dto.quantityInformed() != null? dto.quantityInformed(): stockmovement.getQuantityInformed());
		stockmovement.setQuantityAvailable(dto.quantityAvailable() != null? dto.quantityAvailable(): stockmovement.getQuantityAvailable());
		stockmovement.getAudit().setUpdatedAt(ZonedDateTime.now());

		repository.save(stockmovement);

		return get(stock_movementId);
	}

	/**
	 * delete an StockMovement by ID and update the Stock available to the item
	 *
	 * @param stock_movementId the id of the StockMovement
	 * @throws EntityNotFoundException when the StockMovement is not found
	 */
	@Transactional
	public void delete(final Long stock_movementId) throws EntityNotFoundException {
		StockMovement sm = findByIdChecked(stock_movementId);
		repository.delete(sm);
	}

	/**
	 * List all stockmovements
	 */
	public List<StockMovementDTO> listAll() {
		return createStockMovementList(findAll(), null);
	}

	/**
	 * List all StockMovement for a specific Item
	 *
	 * @param itemId the ID of the item
	 * @return List<StockMovement> of Stock
	 *
	 * @throws EntityNotFoundException when the Item is not found
	 * @throws ServiceNotAvailableException the Item-Service is not available to check to the item
	 */
	public List<StockMovementDTO> listByItem(final Long itemId) throws EntityNotFoundException, ServiceNotAvailableException {
		// implicit item check
		ItemDTO item = getItemDTO(itemId);

		// find all stock movements by item and return them grouping by Item
		return createStockMovementList(((StockMovementRepository) repository).findAllByItemIdOrderByCreationDate(itemId), item);
	}

	public List<StockMovementDTO> listByNonZeroQuantityAvailable() {
		return createStockMovementList(((StockMovementRepository) repository).findAllByQuantityAvailableGreaterThan(0L), null);
	}
	private List<StockMovementDTO> createStockMovementList(final List<StockMovement> stock_movements, final ItemDTO item) {
		Map<Long, List<ItemDTO>> itemMap = item == null? getItemMap(): null;

		// fallback for item-service
		final Function<Long, ItemDTO> itemFinder = id -> {
			if (itemMap == null || itemMap.isEmpty() || !itemMap.containsKey(id)) {
				return ItemDTO.builder()
						.name("Unavailable Item Data")
						.build();
			} else {
				return itemMap.get(id).get(0);
			}
		};

		List<StockMovementDTO> list = new ArrayList<>();
		stock_movements
			.forEach(sm -> {
				StockMovementDTO dto = StockMovementDTO.builder()
						.id(sm.getId())
						.creationDate(sm.getCreationDate())
						.item(item != null? item: itemFinder.apply(sm.getItemId()))
						.quantityInformed(sm.getQuantityInformed())
						.quantityAvailable(sm.getQuantityAvailable())
						.build();
				list.add(dto);
			});
		return list.stream().sorted(Comparator.comparing(StockMovementDTO::creationDate)).toList();
	}

	private Map<Long, List<ItemDTO>> getItemMap() {
		try {
			List<ItemDTO> items = itemFeignClient.listItems();
			return items.stream().collect(groupingBy(ItemDTO::id));
		} catch (Throwable fe) {
			log.error("item-service status: {}", fe.getMessage());
			return new TreeMap<>();
		}

	}
}
