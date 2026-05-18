package com.seshop.identity.api;

import jakarta.validation.constraints.NotNull;

public record AssignLocationRequest(@NotNull Long locationId) {
}
