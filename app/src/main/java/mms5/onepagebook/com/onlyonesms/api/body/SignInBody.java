package mms5.onepagebook.com.onlyonesms.api.body;

import java.util.HashMap;

public class SignInBody extends HashMap<String, String> {

  public SignInBody(String id, String pw, String num, String telecom, String model, String ver, String token) {
    put("id", id);
    put("pw", pw);
    put("num", num);
    put("telecom", telecom);
    put("model", model);
    put("ver", ver);
    put("pkey", token);
  }
}