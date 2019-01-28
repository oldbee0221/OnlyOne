package mms5.onepagebook.com.onlyonesms.model;

import io.realm.RealmObject;
import io.realm.annotations.Index;

public class Reservation extends RealmObject {
  @Index
  private String reqid; // 문자의 ID (고유해야함)
  private long delay; // 발송딜레이
  private int expiredTime; // 문자 발송 종료 시간 없을경우 기본 종료시간 21시.

  private String phoneNumber; // 발송번호
  private String title; // 문자 제목 (없을경우 아래 문자 본문중 최초 8자를 제목으로 보낸다.)
  private String body; // 발송번호
  private boolean isSent;
  private String idx;

  public String getReqid() {
    return reqid;
  }

  public void setReqid(String reqid) {
    this.reqid = reqid;
  }

  public long getDelay() {
    return delay;
  }

  public void setDelay(long delay) {
    this.delay = delay;
  }

  public int getExpiredTime() {
    return expiredTime;
  }

  public void setExpiredTime(int expiredTime) {
    this.expiredTime = expiredTime;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public boolean isSent() {
    return isSent;
  }

  public void setSent(boolean sent) {
    isSent = sent;
  }

  public String getIdx() {
    return idx;
  }

  public void setIdx(String idx) {
    this.idx = idx;
  }
}
