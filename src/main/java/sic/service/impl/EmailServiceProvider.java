package sic.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailServiceProvider {

  GMAIL("gmail"),
  RESEND("resend");

  private final String name;
}
