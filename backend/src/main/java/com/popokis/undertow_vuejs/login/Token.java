package com.popokis.undertow_vuejs.login;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = Token.TokenBuilder.class)
@AllArgsConstructor(staticName = "create")
public class Token {
  @NonNull String token;
}
