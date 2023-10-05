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

	private Map<Long, List<ItemDTO>> itemsMap = new TreeMap<>();

	public StockMovementService(final StockMovementRepository stockmovementRepository) {
		super(stockmovementRepository, new StockMovementMapper());
	}

	public StockMovementDTO get(final Long stockMovementId) throws EntityNotFoundException, ServiceNotAvailableException {
		log.info("Retrieving StockMovement {}", stockMovementId);
		StockMovement stockmovement = findByIdChecked(stockMovementId);
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
		log.info("creating StockMovement {}", dto);
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
		log.info("Checking pending Orders ...");
		List<OrderDTO> pendingOrders = orderFeignClient.listByItemAndStatus(stockMovement.getItemId(), OrderStatus.pending);

		// check all orders found trying to fulfill them
		for (OrderDTO o : pendingOrders) {
			log.warn("Updating pending Order: {}", o);
			stockMovement = updateOrderAndStockMovement(stockMovement, o);

			// leave the loop if the available quantity for this StockMovement is already depleted
			if (stockMovement.getQuantityAvailable() == 0) {
				break;
			}
		}
	}

	@Transactional
	public StockMovement updateOrderAndStockMovement(StockMovement stockMovement, OrderDTO o) throws ServiceNotAvailableException, EntityNotFoundException, ConstraintsViolationException {
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

		log.info("\tcreating order update request: {}", updateOrderRequest);
		var updatedOrder = orderFeignClient.update(o.id(), updateOrderRequest);

		stockMovement.setQuantityAvailable(stockMovement.getQuantityAvailable() - quantity);
		stockMovement = repository.save(stockMovement);

		addToTrace(updatedOrder, mapper.convertEntityToDTO(stockMovement), quantity);

		return stockMovement;
	}

	public void addToTrace(OrderDTO orderDTO, StockMovementDTO stockMovementDTO, Long quantity) throws ConstraintsViolationException {
		log.warn("\tAdding tracing for -> Order: {} | StockMovement: {} | Quantity: {}", orderDTO.id(), stockMovementDTO.id(), quantity);
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
	 * Update a stockmovement information.
	 */
	@Transactional
	public StockMovementDTO update(final Long stockMovementId, final StockMovementDTO dto) throws EntityNotFoundException, ConstraintsViolationException, ServiceNotAvailableException {
		log.info("Updating StockMovement {}{}", stockMovementId, dto);
		StockMovement stockmovement = findByIdChecked(stockMovementId);

		stockmovement.setItemId(dto.itemId() != null? dto.itemId(): stockmovement.getItemId());
		stockmovement.setQuantityInformed(dto.quantityInformed() != null? dto.quantityInformed(): stockmovement.getQuantityInformed());
		stockmovement.setQuantityAvailable(dto.quantityAvailable() != null? dto.quantityAvailable(): stockmovement.getQuantityAvailable());
		stockmovement.getAudit().setUpdatedAt(ZonedDateTime.now());

		repository.save(stockmovement);

		return get(stockMovementId);
	}

	/**
	 * delete an StockMovement by ID and update the Stock available to the item
	 *
	 * @param stockMovementId the id of the StockMovement
	 * @throws EntityNotFoundException when the StockMovement is not found
	 */
	@Transactional
	public void delete(final Long stockMovementId) throws EntityNotFoundException {
		log.info("removing StockMovement {}", stockMovementId);
		StockMovement sm = findByIdChecked(stockMovementId);
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

	/**
	 * common method to map List<Order> into List<OrderDTO>
	 *
	 * @param stockMovements List of StockMovement
	 * @param item reference to ItemDTO (if available)
	 * @return List of StockMovementDTO
	 */
	private List<StockMovementDTO> createStockMovementList(final List<StockMovement> stockMovements, final ItemDTO item) {
		// fallback for item-service
		final Function<Long, ItemDTO> itemFinder = id -> {
			if (getItemsMap().containsKey(id)) {
				return ItemDTO.builder()
						.name("Unavailable Item Data")
						.build();
			} else {
				return getItemsMap().get(id).get(0);
			}
		};

		List<StockMovementDTO> list = new ArrayList<>();
		stockMovements
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

	private Map<Long, List<ItemDTO>> getItemsMap() {
		if (itemsMap == null || itemsMap.isEmpty()) {
			new Thread(() -> {
				synchronized (itemsMap) {
					try {
						itemsMap = itemFeignClient.listItems()
								.stream().collect(groupingBy(ItemDTO::id));
					} catch (Throwable fe) {
						log.error("item-service status: {}", fe.getMessage());
						itemsMap = new TreeMap<>();
					}
				}
			}).start();
		}
		return itemsMap;
	}
}
