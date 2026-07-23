package com.idleitems.school.module.admin.service;

import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.service.OrderAdminService;
import com.idleitems.school.module.system.service.DictService;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.user.repository.UserRepository;
import com.idleitems.school.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminBatchService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final OrderAdminService orderAdminService;
    private final UserService userService;
    private final DictService dictService;

    @Transactional
    public void batchApproveItems(List<Long> itemIds) {
        for (Long id : itemIds) {
            Item item = itemRepository.findById(id.longValue())
                    .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
            item.setStatus(Item.ItemStatus.ON_SALE);
            itemRepository.save(item);
        }
    }

    @Transactional
    public void batchRejectItems(List<Long> itemIds, String reason) {
        for (Long id : itemIds) {
            Item item = itemRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
            item.setStatus(Item.ItemStatus.REJECTED);
            item.setRejectReason(reason);
            itemRepository.save(item);
        }
    }

    @Transactional
    public void batchOffShelfItems(List<Long> itemIds, String reason) {
        for (Long id : itemIds) {
            Item item = itemRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
            item.setStatus(Item.ItemStatus.OFF_SHELF);
            if (reason != null) {
                item.setRejectReason(reason);
            }
            itemRepository.save(item);
        }
    }

    @Transactional
    public void batchUpdateUserStatus(List<Long> userIds, User.UserStatus status) {
        for (Long id : userIds) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
            user.setStatus(status);
            userRepository.save(user);
        }
    }

    @Transactional
    public void batchCancelOrders(List<Long> orderIds, String reason, Long adminId) {
        for (Long id : orderIds) {
            orderAdminService.adminCancelOrder(id, adminId, reason);
        }
    }

    @Transactional
    public void batchDeleteUsers(List<Long> userIds) {
        userService.deleteUsers(userIds);
    }
}
