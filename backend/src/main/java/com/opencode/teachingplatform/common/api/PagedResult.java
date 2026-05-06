package com.opencode.teachingplatform.common.api;

import java.util.List;

public record PagedResult<T>(List<T> items, int page, int pageSize, long total) {
}
