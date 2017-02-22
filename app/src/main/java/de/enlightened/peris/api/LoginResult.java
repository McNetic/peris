package de.enlightened.peris.api;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by Nicolai Ehemann on 20.02.2017.
 */
@Getter
@Builder
public class LoginResult {
  private boolean success;
  private String message;
}
