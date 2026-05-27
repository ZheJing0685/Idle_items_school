package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.entity.Order;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "首页管理", description = "首页数据展示相关接口")
@RestController
@RequestMapping(ApiPaths.Home.BASE)
@RequiredArgsConstructor
public class HomeController {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    @Operation(summary = "获取首页统计", description = "获取首页展示的用户总数、物品总数和完成订单数等统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getHomeStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalItems", itemRepository.count());
        stats.put("completedOrders", orderRepository.countByOrderStatus(Order.OrderStatus.COMPLETED));
        return Result.success(stats);
    }
}
