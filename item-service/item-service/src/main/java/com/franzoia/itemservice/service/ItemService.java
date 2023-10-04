package com.franzoia.itemservice.service;

import com.franzoia.itemservice.model.Item;
import com.franzoia.itemservice.repository.ItemRepository;
import com.franzoia.itemservice.service.mapper.ItemMapper;
import com.franzoia.common.dto.ItemDTO;
import com.franzoia.common.exception.ConstraintsViolationException;
import com.franzoia.common.exception.EntityNotFoundException;
import com.franzoia.common.util.DefaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Predicate;


/**
 * Item Service class
 */
@Slf4j
@Service
public class ItemService extends DefaultService<ItemDTO, Item, Long, ItemMapper> {

	public ItemService(final ItemRepository itemRepository) {
		super(itemRepository, new ItemMapper());
	}

	@Transactional
	public ItemDTO create(ItemDTO dto) throws ConstraintsViolationException {
		log.info(String.format("create item %s", dto.toString()));
		if (hasDuplicateName(dto.name(), null)) {
			throw new ConstraintsViolationException("A item with the provided name already exists");
		}
		Item item = mapper.convertDtoToEntity(dto);
		return mapper.convertEntityToDTO(create(item));
	}

	/**
	 * Update a items information.
	 *
	 * @param itemId item id
	 * @throws EntityNotFoundException
	 */
	@Transactional
	public ItemDTO update(final Long itemId, final ItemDTO dto) throws EntityNotFoundException, ConstraintsViolationException {
		log.info(String.format("update item %s", dto.toString()));
		Item item = findByIdChecked(itemId);
		if (hasDuplicateName(dto.name(), item)) {
			throw new ConstraintsViolationException("A item with the provided name already exists");
		}
		item.setName(dto.name() != null? dto.name(): item.getName());
		item.getAudit().setUpdatedAt(ZonedDateTime.now());
		return mapper.convertEntityToDTO(repository.save(item));
	}

	private boolean hasDuplicateName(final String name, final Item item) {
		final Predicate<Item> equalName = c -> c.getName().equals(name);
		final Predicate<Item> sameItem = c -> item == null || !c.getId().equals(item.getId());
		List<Item> list = findAll().stream().filter(equalName.and(sameItem)).toList();
		return !list.isEmpty();
	}


}
