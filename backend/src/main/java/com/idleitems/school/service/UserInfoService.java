package com.idleitems.school.service;

import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserRepository userRepository;

    public void enrichItemWithSellerInfo(Item item, int sellerItemCount) {
        if (item == null) {
            return;
        }

        if (item.getUserId() == null) {
            return;
        }

        User user = userRepository.findById(item.getUserId()).orElse(null);
        if (user != null) {
            item.setSellerNickname(
                user.getNickname() != null && !user.getNickname().isEmpty()
                    ? user.getNickname()
                    : user.getUsername()
            );
            item.setSellerVerified(user.getVerified() != null ? user.getVerified() : false);
            item.setSellerRating(5.0);
            item.setSellerItemsCount(sellerItemCount);
        }
    }
}
