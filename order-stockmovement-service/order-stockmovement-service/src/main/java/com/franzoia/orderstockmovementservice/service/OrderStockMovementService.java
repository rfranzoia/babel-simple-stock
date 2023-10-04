package com.franzoia.orderstockmovementservice.service;

import com.franzoia.common.dto.OrderStockMovementDTO;
import com.franzoia.common.dto.OrderStockMovementKey;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.util.DefaultService;
import com.franzoia.orderstockmovementservice.model.OrderStockMovement;
import com.franzoia.orderstockmovementservice.repository.OrderStockMovementRepository;
import com.franzoia.orderstockmovementservice.service.mapper.OrderStockMovementMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * OrderStockMovement Service class
 */
@Slf4j
@Service
public class OrderStockMovementService extends DefaultService<OrderStockMovementDTO, OrderStockMovement, OrderStockMovementKey, OrderStockMovementMapper> {

	public OrderStockMovementService(final OrderStockMovementRepository orderstockmovementRepository) {
		super(orderstockmovementRepository, new OrderStockMovementMapper());
	}

	public List<OrderStockMovementDTO> listByOrder(final Long orderId) {
		return ((OrderStockMovementRepository) repository).findAllByKeyOrderId(orderId)
					.stream()
					.map(mapper::convertEntityToDTO).toList();
	}

	public List<OrderStockMovementDTO> listByStockMovement(final Long stockMovementId) {
		return ((OrderStockMovementRepository) repository).findAllByKeyStockMovementId(stockMovementId)
				.stream()
				.map(mapper::convertEntityToDTO).toList();
	}

	@Transactional
	public OrderStockMovementDTO create(OrderStockMovementDTO dto) throws ConstraintsViolationException {
		log.info("create order-stock-movement: {}", dto);
		OrderStockMovement orderstockmovement = mapper.convertDtoToEntity(dto);
		return mapper.convertEntityToDTO(create(orderstockmovement));
	}

	@Transactional
	public void delete(final Long orderId, final Long stockMovementId) throws EntityNotFoundException {
		log.info("delete order-stock-movement: Order: {} StockMovement: {}", orderId, stockMovementId);
		delete(new OrderStockMovementKey(orderId, stockMovementId));
	}

}
