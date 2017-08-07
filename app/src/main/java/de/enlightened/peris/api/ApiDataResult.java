package de.enlightened.peris.api;

import lombok.Builder;
import lombok.Getter;

/**
 * Copyright (C) 2017 Nicolai Ehemann
 * <p>
 * This file is part of Peris.
 * <p>
 * Peris is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Peris is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Peris.  If not, see <http://www.gnu.org/licenses/>.
 */

@Builder
public class ApiDataResult<T> {
  private final ApiResult apiResult;
  @Getter
  private final T data;

  public boolean isSuccess() {
    return this.apiResult.isSuccess();
  }

  public String getMessage() {
    return this.apiResult.getMessage();
  }
}
