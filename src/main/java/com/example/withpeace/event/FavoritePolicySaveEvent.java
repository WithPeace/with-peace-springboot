package com.example.withpeace.event;

import com.example.withpeace.domain.Policy;
import com.example.withpeace.domain.User;

public record FavoritePolicySaveEvent(User user, Policy policy) {}